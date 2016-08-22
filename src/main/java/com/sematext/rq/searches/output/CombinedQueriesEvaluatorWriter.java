/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.JedisPool;

import com.sematext.rq.searches.common.Settings;
import com.sematext.rq.searches.eval.QueriesEvaluator;
import com.sematext.rq.searches.eval.SegmentProcessorQueriesEvaluator;

/**
 * Combines multiple run of {@link QueriesEvaluator} into a single list using provided boosts as suggestion score
 * modification.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class CombinedQueriesEvaluatorWriter extends AbstractProfanityRemovingOutputWriter {
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(Settings.DECIMAL_FORMAT);
  private BufferedWriter writer;
  private List<Float> boosts;
  private List<String> prefixes;
  private int minQueryLength;

  /**
   * Constructor.
   * 
   * @param fileName
   *          name of the file to write results to
   * @param boosts
   *          boosts list
   * @param prefixes
   *          list of prefixes
   * @param minQueryLength
   *          minimum query length
   * @throws IOException
   *           thrown when I/O exception occurs
   */
  public CombinedQueriesEvaluatorWriter(String fileName, List<Float> boosts, List<String> prefixes, int minQueryLength,
      boolean nonZeroHitsOnly) throws IOException {
    this();
    writer = new BufferedWriter(new FileWriter(fileName));
    this.boosts = boosts;
    this.prefixes = prefixes;
    this.minQueryLength = minQueryLength;
  }

  protected CombinedQueriesEvaluatorWriter() {
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

    // now write all common queries to file
    for (String commonQuery : commonQueries) {
      if (commonQuery.length() >= minQueryLength) {
        Map<String, Double> suggestions = queries.get(commonQuery);
        if (!suggestions.isEmpty()) {
          if (!isPhraseBanned(commonQuery)) {
            StringWriter temporaryWriter = new StringWriter();
            boolean hasQueries = false;
            temporaryWriter.write(commonQuery);
            temporaryWriter.write(" -> ");
            for (Map.Entry<String, Double> suggestionEntry : suggestions.entrySet()) {
              String suggestion = suggestionEntry.getKey();
              if (!isPhraseBanned(suggestion)) {
                temporaryWriter.write(suggestion);
                temporaryWriter.write("(");
                temporaryWriter.write(DECIMAL_FORMAT.format(suggestionEntry.getValue()));
                temporaryWriter.write("),");
                hasQueries = true;
              }
            }
            if (hasQueries) {
              writer.write(temporaryWriter.toString());
              writer.newLine();
            }
          }
        }
      }
    }
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
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.output.OutputWriter#close()
   */
  @Override
  public void close() throws IOException {
    writer.flush();
    writer.close();
  }
}
