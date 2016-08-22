/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.output;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sematext.rq.searches.common.Settings;
import com.sematext.rq.searches.data.QuerySuggestion;
import com.sematext.rq.searches.data.RelatedQuery;
import com.sematext.rq.searches.http.HttpUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Implementation of {@link AbstractCombinedQueriesEvaluatorWriter} that writes results to ES.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class CombinedQueriesEvaluatorESWriter extends AbstractCombinedQueriesEvaluatorWriter {
  protected HttpClient httpClient;
  private String host;

  /**
   * Constructor.
   */
  protected CombinedQueriesEvaluatorESWriter() {
  }

  /**
   * Constructor.
   * 
   * @param host
   *          ElasticSearch host
   * @param index
   *          ElasticSearch index
   * @param boosts
   *          boosts list
   * @param prefixes
   *          list of prefixes
   * @param minQueryLength
   *          minimum query length
   * @throws IOException
   *           thrown when I/O exception occurs
   */
  public CombinedQueriesEvaluatorESWriter(String host, String index, List<Float> boosts, List<String> prefixes,
      int minQueryLength, boolean nonZeroHitsOnly) throws IOException {
    super(boosts, prefixes, minQueryLength, nonZeroHitsOnly);
    this.host = host + Settings.PATH_ELEMENT_SEPARATOR + index + Settings.PATH_ELEMENT_SEPARATOR
        + Settings.ES_TYPE_NAME;
    this.httpClient = new DefaultHttpClient();
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.sematext.rq.searches.output.AbstractCombinedQueriesEvaluatorWriter#writeSuggestions(java.util.List)
   */
  @Override
  protected void writeSuggestions(List<QuerySuggestion> suggestions) throws IOException {
    for (QuerySuggestion suggestion : suggestions) {
      String esDocument = getESDocument(suggestion);
      try {
        HttpUtils.sendElasticSearchPostRequest(httpClient, host,
            URLEncoder.encode(suggestion.getQuery().toLowerCase(), "UTF-8"), esDocument);
      } catch (Exception ex) {
        System.err.println("Error indexing query: " + ex.getMessage());
      }
    }
  }

  /**
   * Returns Elasticsearch document for the given suggestion.
   * 
   * @param suggestion
   *          suggestion
   * @return Elasticsearch document
   */
  protected String getESDocument(QuerySuggestion suggestion) {
    JSONObject document = new JSONObject();
    document.put(Settings.ES_QUERY_SECTION, suggestion.getQuery());
    JSONArray suggestions = new JSONArray();
    for (int i = 0; i < suggestion.getRelatedQueries().size(); i++) {
      RelatedQuery relatedQuery = suggestion.getRelatedQueries().get(i);
      JSONObject esSuggestion = new JSONObject();
      esSuggestion.put(Settings.ES_RELATED_QUERY, relatedQuery.getQuery());
      esSuggestion.put(Settings.ES_RELATED_QUERY_SCORE, relatedQuery.getValue());
      suggestions.put(esSuggestion);
    }
    document.put(Settings.ES_RELATED_QUERIES_SECTION, suggestions);
    return document.toString();
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.sematext.rq.searches.output.OutputWriter#close()
   */
  @Override
  public void close() throws IOException {
    httpClient.getConnectionManager().shutdown();
  }
}
