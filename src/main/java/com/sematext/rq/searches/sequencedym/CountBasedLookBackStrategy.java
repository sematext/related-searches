package com.sematext.rq.searches.sequencedym;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import com.sematext.rq.searches.parser.QueryLogEntry;

/**
 * Count based {@link LookBackStrategy}.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class CountBasedLookBackStrategy implements LookBackStrategy {
  private Queue<QueryLogEntry> previousQuery;
  private final int numberOfQueries;

  /**
   * Constructor.
   * 
   * @param numberOfQueries
   *          number of queries
   */
  public CountBasedLookBackStrategy(int numberOfQueries) {
    if (numberOfQueries <= 0) {
      throw new IllegalArgumentException("Count of previous queries must be greater than zero!");
    }
    this.numberOfQueries = numberOfQueries;
    this.previousQuery = new LinkedList<QueryLogEntry>();
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.sequencedym.LookBackStrategy#addEntry(com.sematext.rq.searches.parser.didyoumean.QueryLogEntry)
   */
  @Override
  public void addEntry(QueryLogEntry entry) {
    if (previousQuery.size() == numberOfQueries) {
      previousQuery.remove();
    }
    previousQuery.offer(entry);
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.sequencedym.LookBackStrategy#iterator(com.sematext.rq.searches.parser.didyoumean.QueryLogEntry)
   */
  @Override
  public Iterator<QueryLogEntry> iterator(QueryLogEntry entry) {
    return previousQuery.iterator();
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.sequencedym.LookBackStrategy#clear()
   */
  @Override
  public void clear() {
    this.previousQuery.clear();
  }

  /**
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CountBasedLookBack [NumberOfQueries=" + numberOfQueries + "]";
  }

}
