package com.sematext.rq.searches.sequencedym;

import com.sematext.rq.searches.parser.QueryLogEntry;

/**
 * {@link LookBackTrigger} implementation based on click information.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class ClickLookBackTrigger implements LookBackTrigger {
  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.sequencedym.LookBackTrigger#doLookBack(com.sematext.rq.searches.parser.didyoumean.QueryLogEntry)
   */
  @Override
  public boolean doLookBack(QueryLogEntry currentEntry) {
    return currentEntry.searchSuccessful;
  }

  /**
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ClickLookBackTrigger";
  }
}
