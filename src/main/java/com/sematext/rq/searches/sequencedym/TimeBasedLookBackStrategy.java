package com.sematext.rq.searches.sequencedym;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import com.sematext.rq.searches.parser.QueryLogEntry;

/**
 * Time based {@link LookBackStrategy} implementation.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class TimeBasedLookBackStrategy implements LookBackStrategy {
  private Queue<QueryLogEntry> previousQ;
  private final long maxTimeDiffMilliseconds;

  /**
   * Constructor.
   * 
   * @param maxTimeDiffMilliseconds
   *          maximum time difference between queries in milliseconds
   */
  public TimeBasedLookBackStrategy(long maxTimeDiffMilliseconds) {
    if (maxTimeDiffMilliseconds <= 0)
      throw new IllegalArgumentException("maxTimeDiffMilliseconds must be greater than zero!");
    this.maxTimeDiffMilliseconds = maxTimeDiffMilliseconds;
    this.previousQ = new LinkedList<QueryLogEntry>();
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.sequencedym.LookBackStrategy#addEntry(com.sematext.rq.searches.parser.didyoumean.QueryLogEntry)
   */
  @Override
  public void addEntry(QueryLogEntry e) {
    removeOld(e);
    previousQ.offer(e);
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.sequencedym.LookBackStrategy#iterator(com.sematext.rq.searches.parser.didyoumean.QueryLogEntry)
   */
  @Override
  public Iterator<QueryLogEntry> iterator(QueryLogEntry e) {
    removeOld(e);
    return previousQ.iterator();
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.sequencedym.LookBackStrategy#clear()
   */
  @Override
  public void clear() {
    this.previousQ.clear();
  }

  /**
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TimeBasedLookBack [maxTimeDiff=" + maxTimeDiffMilliseconds + "]";
  }

  private long timeDiff(QueryLogEntry eOld, QueryLogEntry eNew) {
    return Math.abs(eOld.timestamp.getTime() - eNew.timestamp.getTime());
  }

  private void removeOld(QueryLogEntry e) {
    while (!previousQ.isEmpty() && timeDiff(previousQ.peek(), e) > maxTimeDiffMilliseconds) {
      previousQ.remove();
    }
  }
}
