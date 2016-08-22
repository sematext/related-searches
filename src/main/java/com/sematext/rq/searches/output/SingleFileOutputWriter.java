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
 * {@link OutputWriter} implementation that writes output to a given file.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class SingleFileOutputWriter extends AbstractOutputWriter {
  private BufferedWriter writer;

  /**
   * Constructor.
   * 
   * @param fileName
   *          name of the output file
   * @param prefix
   *          redis prefix
   * @throws IOException
   *           thrown when I/O error occurs
   */
  public SingleFileOutputWriter(String fileName, String prefix) throws IOException {
    super(prefix);
    writer = new BufferedWriter(new FileWriter(fileName));
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.output.OutputWriter#write(java.util.List)
   */
  @Override
  public void write(List<SegmentProcessorQueriesEvaluator> evaluations) throws IOException {
    for (SegmentProcessorQueriesEvaluator dym : evaluations) {
      writer.write("##");
      writer.write(dym.describe().replace("\n", " ").replace("\t", " "));
      writer.newLine();
      dym.init();
      writeQueries(writer, dym);
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
    writer.flush();
    writer.close();
  }
}
