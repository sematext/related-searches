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
import java.io.StringWriter;
import java.util.List;

import com.sematext.rq.searches.data.QuerySuggestion;
import com.sematext.rq.searches.data.RelatedQuery;

/**
 * {@link AbstractCombinedQueriesEvaluatorWriter} implementation that writes results to a given file.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class CombinedQueriesEvaluatorFileWriter extends AbstractCombinedQueriesEvaluatorWriter {
  private BufferedWriter writer;

  /**
   * Constructor.
   * 
   * @param fileName
   *          name of the file to write results to
   * @param boosts
   *          boosts list
   * @param prefixes
   *          list of prefixes
   * @param minQueryLength
   *          minimum query length
   * @throws IOException
   *           thrown when I/O exception occurs
   */
  public CombinedQueriesEvaluatorFileWriter(String fileName, List<Float> boosts, List<String> prefixes,
      int minQueryLength, boolean nonZeroHitsOnly) throws IOException {
    super(boosts, prefixes, minQueryLength, nonZeroHitsOnly);
    writer = new BufferedWriter(new FileWriter(fileName));
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.sematext.rq.searches.output.AbstractCombinedQueriesEvaluatorWriter#writeSuggestions(java.util.List)
   */
  @Override
  protected void writeSuggestions(List<QuerySuggestion> suggestions) throws IOException {
    for (QuerySuggestion suggestion : suggestions) {
      StringWriter temporaryWriter = new StringWriter();
      temporaryWriter.write(suggestion.getQuery());
      temporaryWriter.write(" -> ");
      for (RelatedQuery query : suggestion.getRelatedQueries()) {
        temporaryWriter.write(query.getQuery());
        temporaryWriter.write("(");
        temporaryWriter.write(DECIMAL_FORMAT.format(query.getValue()));
        temporaryWriter.write("),");
      }
      writer.write(temporaryWriter.toString());
      writer.newLine();
    }
  }

  /**
   * Constructor.
   */
  protected CombinedQueriesEvaluatorFileWriter() {
    super();
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
