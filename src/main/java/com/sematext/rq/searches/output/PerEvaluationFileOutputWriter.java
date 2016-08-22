/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.sematext.rq.searches.eval.SegmentProcessorQueriesEvaluator;

/**
 * {@link OutputWriter} implementation that writes output to a different file for each calculated evaluation.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class PerEvaluationFileOutputWriter extends AbstractOutputWriter {
  /**
   * Constructor.
   * 
   * @param prefix
   *          redis prefix
   */
  public PerEvaluationFileOutputWriter(String prefix) {
    super(prefix);
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.output.OutputWriter#write(java.util.List)
   */
  @Override
  public void write(List<SegmentProcessorQueriesEvaluator> evaluations) throws IOException {
    for (SegmentProcessorQueriesEvaluator dym : evaluations) {
      BufferedWriter writer = new BufferedWriter(new FileWriter(dym.genFilename()));
      writer.write(dym.describe());
      writer.newLine();
      dym.init();
      writeQueries(writer, dym);
      writer.flush();
      writer.close();
      dym.destroy();
    }
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.output.OutputWriter#close()
   */
  @Override
  public void close() throws IOException {
  }
}
