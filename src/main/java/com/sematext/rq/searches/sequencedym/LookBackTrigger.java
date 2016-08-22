package com.sematext.rq.searches.sequencedym;

import com.sematext.rq.searches.parser.QueryLogEntry;

/**
 * Look back trigger.
 * 
 * @author sematext, http://www.sematext.com/
 */
public interface LookBackTrigger {
  /**
   * Triggers look back.
   * 
   * @param currentEntry
   *          current entry
   * @return <code>true</code> if look back is successful
   */
  boolean doLookBack(QueryLogEntry currentEntry);
}
