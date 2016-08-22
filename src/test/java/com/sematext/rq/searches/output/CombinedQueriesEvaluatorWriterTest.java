/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.output;

import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

public class CombinedQueriesEvaluatorWriterTest extends TestCase {
  @Test
  public void testSplitQueries() throws Exception {
    CombinedQueriesEvaluatorFileWriter evaluator = new CombinedQueriesEvaluatorFileWriter();
    Map<String, Double> result = evaluator.splitQueries("w pierscieniu ognia (0.5),");
    assertEquals(1, result.size());
    assertTrue(result.containsKey("w pierscieniu ognia"));
    assertEquals(0.5, result.get("w pierscieniu ognia"));

    result = evaluator.splitQueries(
        "sold and saved (0.5), prisiner (0.5), kidnaped gerard way (0.5), killjoys (0.2692307692307692),");
    assertEquals(4, result.size());
    assertTrue(result.containsKey("sold and saved"));
    assertEquals(0.5, result.get("sold and saved"));
    assertTrue(result.containsKey("prisiner"));
    assertEquals(0.5, result.get("prisiner"));
    assertTrue(result.containsKey("kidnaped gerard way"));
    assertEquals(0.5, result.get("kidnaped gerard way"));
    assertTrue(result.containsKey("killjoys"));
    assertEquals(0.2692307692307692, result.get("killjoys"));
  }
}
