/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.output.elasticsearch;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.sematext.rq.searches.common.Settings;
import com.sematext.rq.searches.eval.SegmentProcessorQueriesEvaluator;
import com.sematext.rq.searches.http.HttpUtils;
import com.sematext.rq.searches.output.AbstractHTTPOutputWriter;
import com.sematext.rq.searches.output.OutputWriter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;
import redis.clients.jedis.JedisPool;

/**
 * {@link OutputWriter} implementation that stores results in Elasticsearch.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class ElasticsearchHTTPOutputWriter extends AbstractHTTPOutputWriter {
  private static Logger LOG = Logger.getLogger(ElasticsearchHTTPOutputWriter.class);

  /**
   * Constructor.
   * 
   * @param host
   *          host
   * @param index
   *          index name
   * @param prefix
   *          Redis prefix
   */
  public ElasticsearchHTTPOutputWriter(String host, String index, String prefix) {
    super(host + Settings.PATH_ELEMENT_SEPARATOR + index + Settings.PATH_ELEMENT_SEPARATOR + Settings.ES_TYPE_NAME,
        prefix);
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.output.OutputWriter#write(java.util.List)
   */
  @Override
  public void write(List<SegmentProcessorQueriesEvaluator> evaluations) throws IOException {
    for (SegmentProcessorQueriesEvaluator dym : evaluations) {
      dym.init();
      sendQueries(dym);
      dym.destroy();
    }
  }

  /**
   * Constructs and send queries to ElasticSearch.
   * 
   * @param dym
   *          evaluation
   * @throws IOException
   *           thrown when I/O error occurs
   */
  protected void sendQueries(SegmentProcessorQueriesEvaluator dym) throws IOException {
    JedisPool pool = dym.getSeqDym().getPool();
    Set<String> keys = pool.getResource().keys(getPrefix() + "*");
    for (String key : keys) {
      String query = key.substring(getPrefix().length());
      String suggestion = dym.suggest(query);
      if (suggestion != null && !suggestion.isEmpty()) {
        JSONObject json = new JSONObject();
        json.put(Settings.ES_QUERY_SECTION, query);
        json.put(Settings.ES_RELATED_QUERY, suggestion);
        try {
          HttpUtils.sendElasticSearchPostRequest(httpClient, host, URLEncoder.encode(query, Settings.DEFAULT_CHARSET),
              json.toString());
        } catch (Exception ex) {
          LOG.error("Error while writing data to Elasticsearch", ex);
          throw ex;
        }
      }
    }
  }
}
