package com.sematext.rq.searches.eval;

import java.io.IOException;

/**
 * Queries evaluator interface.
 * 
 * @author sematext, http://www.sematext.com/
 */
public interface QueriesEvaluator {
  /**
   * Returns related queries as a string for a given query.
   * 
   * @param query
   *          query to return related searches for
   * @return related searches
   * @throws IOException
   *           thrown when I/O error occurs
   */
  String suggest(String query) throws IOException;

  /**
   * Initialize evaluator.
   * 
   * @throws IOException
   *           thrown when I/O error occurs
   */
  void init() throws IOException;

  /**
   * Destroy evaluator.
   * 
   * @throws IOException
   *           thrown when I/O error occurs
   */
  void destroy() throws IOException;

  /**
   * Returns evaluator description.
   * 
   * @return evaluator description
   */
  String describe();
}
