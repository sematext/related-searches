package com.sematext.rq.searches.sequencedym;

import java.util.Iterator;

import com.sematext.rq.searches.parser.QueryLogEntry;

/**
 * Look back strategy.
 * 
 * @author sematext, http://www.sematext.com/
 */
public interface LookBackStrategy {
  /**
   * Adds entry.
   * 
   * @param entry
   *          log entry
   */
  void addEntry(QueryLogEntry entry);

  /**
   * Clears look back strategy.
   */
  void clear();

  /**
   * Returns iterator.
   * 
   * @param entry
   *          query log entry
   * @return iterator
   */
  Iterator<QueryLogEntry> iterator(QueryLogEntry entry);
}
