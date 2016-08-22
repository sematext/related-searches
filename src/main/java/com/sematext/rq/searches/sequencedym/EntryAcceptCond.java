package com.sematext.rq.searches.sequencedym;

import com.sematext.rq.searches.parser.QueryLogEntry;

/**
 * Query entry acceptance condition interface.
 * 
 * @author sematext, http://www.sematext.com/
 */
public interface EntryAcceptCond {
  /**
   * Checks if the query suggestion can be accepted.
   * 
   * @param base
   *          base query
   * @param newEntry
   *          suggestion query
   * @return <code>true</code> if the query was accepted
   */
  boolean accept(QueryLogEntry base, QueryLogEntry newEntry);

  /**
   * Generates file name.
   * 
   * @return file name
   */
  String genFilename();
}
