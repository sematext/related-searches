package com.sematext.rq.searches.eval;

import java.io.IOException;

import com.sematext.rq.searches.parser.QueryLogParser;
import com.sematext.rq.searches.parser.aol.AOLIterableQueryLogParser;
import com.sematext.rq.searches.sequencedym.EntryAcceptCond;
import com.sematext.rq.searches.sequencedym.LookBackStrategy;
import com.sematext.rq.searches.sequencedym.LookBackTrigger;
import com.sematext.rq.searches.sequencedym.QuerySeqProcessorDym;
import com.sematext.rq.searches.sequencedym.RQTuple;

/**
 * Queries evaluator.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class SegmentProcessorQueriesEvaluator implements QueriesEvaluator {
  private QuerySeqProcessorDym seqDym;
  private String queryLogPath;

  /**
   * Constructor.
   * 
   * @param redisHost
   *          Redis host
   * @param redisPort
   *          Redis port
   * @param queryLogPath
   *          path to query log file
   * @param lookBackStrategy
   *          look back strategy
   * @param trigger
   *          trigger
   * @param accCond
   *          accept condition
   * @param suggestionThreshold
   *          suggestion score threshold
   * @param prefix
   *          redis prefix
   * @param nonZeroHitsOnly
   *          should only queries with no zero results be returned
   */
  public SegmentProcessorQueriesEvaluator(String redisHost, int redisPort, String queryLogPath,
      LookBackStrategy lookBackStrategy, LookBackTrigger trigger, EntryAcceptCond accCond, double suggestionThreshold,
      String prefix, boolean nonZeroHitsOnly) {
    seqDym = new QuerySeqProcessorDym(redisHost, redisPort, lookBackStrategy, trigger, accCond, suggestionThreshold,
        prefix, nonZeroHitsOnly);
    this.queryLogPath = queryLogPath;
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.QueriesEvaluator.eval.DymSystemEval#init()
   */
  public void init() throws IOException {
    QueryLogParser parser = new AOLIterableQueryLogParser(queryLogPath);
    seqDym.prepareDB(parser);
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.QueriesEvaluator.eval.DymSystemEval#suggest(java.lang.String)
   */
  @Override
  public String suggest(String query) throws IOException {
    return seqDym.suggestString(query);
  }

  /**
   * Suggest tuple for the given query.
   * 
   * @param query
   *          query to suggest for
   * @return suggested tuple
   * @throws IOException
   *           thrown when error occurs
   */
  public RQTuple<Long, String> suggestTuple(String query) throws IOException {
    return seqDym.suggestTuple(query);
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.QueriesEvaluator.eval.DymSystemEval#describe()
   */
  @Override
  public String describe() {
    return seqDym.toString();
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.QueriesEvaluator.eval.DymSystemEval#destroy()
   */
  @Override
  public void destroy() throws IOException {
    seqDym.flush();
    seqDym.clean();
  }

  public String genFilename() {
    return seqDym.genFilename();
  }

  public QuerySeqProcessorDym getSeqDym() {
    return seqDym;
  }
}
