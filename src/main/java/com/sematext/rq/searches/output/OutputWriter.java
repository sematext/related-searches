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
import java.util.List;

import com.sematext.rq.searches.eval.SegmentProcessorQueriesEvaluator;

/**
 * Output writer interface that allows writing output to file.
 * 
 * @author sematext, http://www.sematext.com/
 */
public interface OutputWriter {
  /**
   * Writes given evaluation.
   * 
   * @param evaluations
   *          evaluations to write
   * @throws IOException
   *           thrown when error during write occurs
   */
  void write(List<SegmentProcessorQueriesEvaluator> evaluations) throws IOException;

  /**
   * Closes writer.
   * 
   * @throws IOException
   *           thrown when error during closing occurs
   */
  void close() throws IOException;
}
