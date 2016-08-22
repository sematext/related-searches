/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.data;

/**
 * Class representing relaed query.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class RelatedQuery {
  private String query;
  private Double value;

  public RelatedQuery(String query, Double value) {
    super();
    this.query = query;
    this.value = value;
  }

  public String getQuery() {
    return query;
  }

  public Double getValue() {
    return value;
  }
}
