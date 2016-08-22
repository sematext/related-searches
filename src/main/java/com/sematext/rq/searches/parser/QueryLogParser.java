package com.sematext.rq.searches.parser;

import java.util.Iterator;

/** 
 * Interface for query log parsers. 
 *
 * @author sematext, http://www.sematext.com/
 */
public interface QueryLogParser {
  /**
   * Query log entries should be sorted first by <b>sessionId</b> and then by <b>order of appearance</b> (from oldest to
   * newest).
   * 
   * @return query log iterator
   */
  Iterator<QueryLogEntry> sortedQueryLogIterator();
}
