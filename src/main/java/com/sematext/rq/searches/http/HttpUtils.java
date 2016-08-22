/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.sematext.rq.searches.common.Settings;

/**
 * HTTP related utility classes.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class HttpUtils {
  private HttpUtils() {
  }

  /**
   * Sends request using POST HTTP method.
   * 
   * @param httpClient
   *          HTTP client
   * @param host
   *          host
   * @param identifier
   *          document identifier
   * @param content
   *          content to send
   * @return <code>true</code> if sending was successful, <code>false</code> otherwise
   */
  public static boolean sendElasticSearchPostRequest(HttpClient httpClient, String host, String identifier,
      String content) throws IOException {
    boolean successful = true;
    HttpPost post = new HttpPost(host + Settings.PATH_ELEMENT_SEPARATOR + identifier);
    post.setEntity(new StringEntity(content, Settings.DEFAULT_CHARSET));
    HttpResponse response = httpClient.execute(post);
    int responseCode = response.getStatusLine().getStatusCode();
    if (responseCode < HttpStatus.SC_OK || responseCode > HttpStatus.SC_MULTI_STATUS) {
      successful = false;
    }
    post.releaseConnection();
    return successful;
  }
}
