/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.data;

import java.util.ArrayList;
import java.util.List;

/** 
 * Class representing query suggestion.
 *
 * @author sematext, http://www.sematext.com/
 */
public class QuerySuggestion {
  private String query;
  private List<RelatedQuery> relatedQueries;
  
  public QuerySuggestion(String query) {
    super();
    this.query = query;
    this.relatedQueries = new ArrayList<RelatedQuery>();
  }
  
  public String getQuery() {
    return query;
  }
  
  public List<RelatedQuery> getRelatedQueries() {
    return relatedQueries;
  }
}
