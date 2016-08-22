/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.output;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

/**
 * Extension of {@link AbstractOutputWriter} for classes using HTTP protocol to write related queries result.
 * 
 * @author sematext, http://www.sematext.com/
 */
public abstract class AbstractHTTPOutputWriter extends AbstractOutputWriter {
  protected HttpClient httpClient;
  protected String host;

  /**
   * Constructor.
   * 
   * @param host
   *          host to connect to
   * @param prefix
   *          redis prefix
   */
  protected AbstractHTTPOutputWriter(String host, String prefix) {
    super(prefix);
    this.host = host;
    this.httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.output.OutputWriter#close()
   */
  @Override
  public void close() throws IOException {
    httpClient.getConnectionManager().shutdown();
  }
}
