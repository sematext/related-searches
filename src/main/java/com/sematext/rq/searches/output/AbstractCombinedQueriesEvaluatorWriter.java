/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.output;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.JedisPool;

import com.sematext.rq.searches.common.Settings;
import com.sematext.rq.searches.data.QuerySuggestion;
import com.sematext.rq.searches.data.RelatedQuery;
import com.sematext.rq.searches.eval.QueriesEvaluator;
import com.sematext.rq.searches.eval.SegmentProcessorQueriesEvaluator;

/**
 * Combines multiple run of {@link QueriesEvaluator} into a single list using provided boosts as suggestion score
 * modification.
 * 
 * @author sematext, http://www.sematext.com/
 */
public abstract class AbstractCombinedQueriesEvaluatorWriter extends AbstractProfanityRemovingOutputWriter {
  public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(Settings.DECIMAL_FORMAT);
  private List<Float> boosts;
  private List<String> prefixes;
  private int minQueryLength;

  /**
   * Constructor.
   * 
   * @param boosts
   *          boosts list
   * @param prefixes
   *          list of prefixes
   * @param minQueryLength
   *          minimum query length
   * @throws IOException
   *           thrown when I/O exception occurs
   */
  public AbstractCombinedQueriesEvaluatorWriter(List<Float> boosts, List<String> prefixes, int minQueryLength,
      boolean nonZeroHitsOnly) throws IOException {
    this();
    this.boosts = boosts;
    this.prefixes = prefixes;
    this.minQueryLength = minQueryLength;
  }

  /**
   * Constructor.
   */
  protected AbstractCombinedQueriesEvaluatorWriter() {
    super();
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.output.OutputWriter#write(java.util.List)
   */
  @Override
  public void write(List<SegmentProcessorQueriesEvaluator> evaluations) throws IOException {
    Map<String, Map<String, Double>> queries = new HashMap<String, Map<String, Double>>();
    Set<String> commonQueries = new HashSet<String>();
    // process all evaluators
    processEvaluators(queries, evaluations, commonQueries);
    // final suggestions list
    List<QuerySuggestion> querySuggestions = new ArrayList<QuerySuggestion>();
    // prepare queries for writing
    for (String commonQuery : commonQueries) {
      if (commonQuery.length() >= minQueryLength) {
        Map<String, Double> suggestions = queries.get(commonQuery);
        if (!suggestions.isEmpty()) {
          if (!isPhraseBanned(commonQuery)) {
            QuerySuggestion newSuggestion = new QuerySuggestion(commonQuery);
            for (Map.Entry<String, Double> suggestionEntry : suggestions.entrySet()) {
              String suggestion = suggestionEntry.getKey();
              if (!isPhraseBanned(suggestion)) {
                newSuggestion.getRelatedQueries().add(new RelatedQuery(suggestion, suggestionEntry.getValue()));
              }
            }
            if (newSuggestion.getRelatedQueries().size() > 0) {
              querySuggestions.add(newSuggestion);
            }
          }
        }
      }
    }
    // write to output
    writeSuggestions(querySuggestions);
  }

  /**
   * Processes evaluators.
   * 
   * @param queries
   *          queries
   * @param evaluations
   *          evaluations
   * @param commonQueries
   *          common queries
   * @throws IOException
   *           thrown when error occurs
   */
  protected void processEvaluators(Map<String, Map<String, Double>> queries,
      List<SegmentProcessorQueriesEvaluator> evaluations, Set<String> commonQueries) throws IOException {
    for (int i = 0; i < evaluations.size(); i++) {
      SegmentProcessorQueriesEvaluator evaluator = evaluations.get(i);
      evaluator.init();
      float boost = boosts.get(i);
      String prefix = prefixes.get(i);
      JedisPool pool = evaluator.getSeqDym().getPool();
      Set<String> keys = pool.getResource().keys(prefix + "*");
      for (String key : keys) {
        String query = key.substring(prefix.length());
        String suggestion = evaluator.suggest(query);
        if (suggestion != null && !suggestion.isEmpty()) {
          Map<String, Double> querySuggestions = splitQueries(suggestion);
          // add queries to the main one
          if (queries.containsKey(query)) {
            // query is already present - we can merge
            // first - put the query in the common queries
            commonQueries.add(query);
            Map<String, Double> existingQueries = queries.get(query);
            for (Map.Entry<String, Double> entry : querySuggestions.entrySet()) {
              String entryKey = entry.getKey();
              // multiply score by the given boost
              Double entryScore = entry.getValue() * boost;
              if (existingQueries.containsKey(entryKey)) {
                // suggestion exist, sum the score
                Double newScore = existingQueries.get(entryKey) + entryScore;
                existingQueries.put(entryKey, newScore);
              } else {
                // suggestion doesn't exist, put it to the map
                existingQueries.put(entryKey, entryScore);
              }
            }
          } else {
            // query is not present
            if (i == 0) {
              // it is the first pass, put the query to result
              queries.put(query, querySuggestions);
            } else {
              // don't put it in the result and remove from common queries
              commonQueries.remove(query);
            }
          }
        }
      }
      evaluator.destroy();
    }
  }

  /**
   * Splits queries that are in a single line to map.
   * 
   * @param suggestion
   *          suggestions to split
   * @return map with split queries
   */
  protected Map<String, Double> splitQueries(String suggestion) {
    Map<String, Double> queries = new HashMap<String, Double>();
    if (suggestion != null && !suggestion.isEmpty()) {
      String[] splited = suggestion.split(",");
      if (splited.length > 0) {
        for (String split : splited) {
          String[] parts = split.split("\\(");
          if (parts.length == 2) {
            try {
              queries.put(parts[0].trim(), Double.parseDouble(parts[1].substring(0, parts[1].length() - 1)));
            } catch (NumberFormatException nfe) {
              // do nothing, just skip the query
            }
          }
        }
      }
    }
    return queries;
  }

  /**
   * Writes suggestions to given output.
   * 
   * @param suggestions
   *          list of suggestions
   * @throws IOException
   *           thrown when I/O error occurs
   */
  protected abstract void writeSuggestions(List<QuerySuggestion> suggestions) throws IOException;
}
