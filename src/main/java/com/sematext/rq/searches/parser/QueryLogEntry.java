package com.sematext.rq.searches.parser;

import java.util.Date;

/**
 * Query log entry.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class QueryLogEntry {
  public final String query;
  public final String url;
  public final String sessionId;
  public final Date timestamp;
  public final boolean searchSuccessful;
  public final long numberOfHits;

  /**
   * Constructor.
   * 
   * @param query
   *          Search query.
   * @param url
   *          click URL
   * @param sessionId
   *          User search session ID. If two log entries have the same session ID, their queries are, most likely, made
   *          by the same user. SessionId must be greater than zero (0).
   * @param searchSuccessful
   *          True if user has clicked at least one search result.
   * @param timestamp
   *          query date and time
   * @param numberOfHits
   *          number do documents query returns
   */
  public QueryLogEntry(String query, String url, String sessionId, boolean searchSuccessful, Date timestamp,
      long numberOfHits) {
    this.query = query;
    this.url = url;
    this.sessionId = sessionId;
    this.searchSuccessful = searchSuccessful;
    this.timestamp = timestamp;
    this.numberOfHits = numberOfHits;
  }

  /**
   * Constructor.
   * 
   * @param query
   *          Search query.
   * @param url
   *          click URL
   * @param sessionId
   *          User search session ID. If two log entries have the same session ID, their queries are, most likely, made
   *          by the same user. SessionId must be greater than zero (0).
   * @param searchSuccessful
   *          True if user has clicked at least one search result.
   * @param timestamp
   *          query date and time
   */
  public QueryLogEntry(String query, String url, String sessionId, boolean searchSuccessful, Date timestamp) {
    this(query, url, sessionId, searchSuccessful, timestamp, 0);
  }

  /**
   * Constructor.
   * 
   * @param query
   *          Search query.
   * @param url
   *          click URL
   * @param sessionId
   *          User search session ID. If two log entries have the same session ID, their queries are, most likely, made
   *          by the same user. SessionId must be greater than zero (0).
   * @param numberOfHits
   *          number of hits
   */
  public QueryLogEntry(String query, String url, String sessionId, long numberOfHits) {
    this(query, url, sessionId, numberOfHits > 0 ? true : false, null, numberOfHits);
  }

  /**
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "QueryLogEntry [query=" + query + ", url=" + url + ", sessionId=" + sessionId + ", timestamp=" + timestamp
        + ", searchSuccessful=" + searchSuccessful + "]";
  }

}
