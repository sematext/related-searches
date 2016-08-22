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
import java.io.IOException;
import java.util.Set;

import redis.clients.jedis.JedisPool;

import com.sematext.rq.searches.eval.SegmentProcessorQueriesEvaluator;

/**
 * Abstract base class for {@link OutputWriter} implementations.
 * 
 * @author sematext, http://www.sematext.com/
 */
public abstract class AbstractOutputWriter implements OutputWriter {
  private String prefix;

  /**
   * Constructor called by children classes.
   * 
   * @param prefix
   *          prefix
   */
  protected AbstractOutputWriter(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Writes queries to given writer.
   * 
   * @param writer
   *          writer
   * @param dym
   *          evaluation
   * @throws IOException
   *           thrown when I/O error occurs
   */
  protected void writeQueries(BufferedWriter writer, SegmentProcessorQueriesEvaluator dym) throws IOException {
    // hack
    // get all keys that are related to query
    // and run suggest method with that query writing it to file
    JedisPool pool = dym.getSeqDym().getPool();
    Set<String> keys = pool.getResource().keys(prefix + "*");
    for (String key : keys) {
      String query = key.substring(prefix.length());
      String suggestion = dym.suggest(query);
      if (suggestion != null && !suggestion.isEmpty()) {
        writer.write(query);
        writer.write(" -> ");
        writer.write(suggestion);
        writer.newLine();
      }
    }
  }

  public String getPrefix() {
    return prefix;
  }
}
