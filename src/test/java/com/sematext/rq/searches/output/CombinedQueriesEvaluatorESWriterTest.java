/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.output;

import junit.framework.TestCase;

import org.junit.Test;

import com.sematext.rq.searches.data.QuerySuggestion;
import com.sematext.rq.searches.data.RelatedQuery;

public class CombinedQueriesEvaluatorESWriterTest extends TestCase {
  @Test
  public void testWriteSuggestions() throws Exception {
    QuerySuggestion suggestion = new QuerySuggestion("test_query");
    suggestion.getRelatedQueries().add(new RelatedQuery("related query", 12.1d));
    suggestion.getRelatedQueries().add(new RelatedQuery("related query 2", 1.1d));

    CombinedQueriesEvaluatorESWriter writer = new CombinedQueriesEvaluatorESWriter();
    String esDoc = writer.getESDocument(suggestion);
    assertEquals("{\"query\":\"test_query\",\"suggestions\":[{\"score\":12.1,\"related\":\"related query\"},"
        + "{\"score\":1.1,\"related\":\"related query 2\"}]}", esDoc);
  }
}
