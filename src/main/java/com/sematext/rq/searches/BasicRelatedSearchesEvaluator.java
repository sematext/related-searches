package com.sematext.rq.searches;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.search.spell.LevensteinDistance;

import com.sematext.rq.searches.common.Settings;
import com.sematext.rq.searches.eval.SegmentProcessorQueriesEvaluator;
import com.sematext.rq.searches.output.OutputWriter;
import com.sematext.rq.searches.output.PerEvaluationFileOutputWriter;
import com.sematext.rq.searches.output.SingleFileOutputWriter;
import com.sematext.rq.searches.output.elasticsearch.ElasticsearchHTTPOutputWriter;
import com.sematext.rq.searches.sequencedym.EntryAcceptCond;
import com.sematext.rq.searches.sequencedym.LookBackStrategy;
import com.sematext.rq.searches.sequencedym.LookBackTrigger;
import com.sematext.rq.searches.sequencedym.SimilarityCond;
import com.sematext.rq.searches.sequencedym.TimeBasedLookBackStrategy;

/**
 * Initial class for related searches.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class BasicRelatedSearchesEvaluator {
  /**
   * Main class.
   * 
   * @param args
   *          arguments
   * @throws IOException
   *           throw when error occurs
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 7 || args.length > 9) {
      System.err.println("Usage:");
      System.err.println("java -jar RelatedQueries.jar <redis_port> <redis_port> <time_between_queries> "
          + "<suggestion_threshold> <similarity_threshold> <more_similar> <query_log>");
      System.err.println("java -jar RelatedQueries.jar <redis_port> <redis_port> <time_between_queries> "
          + "<suggestion_threshold> <similarity_threshold> <more_similar> <query_log> <output_file>");
      System.err.println("java -jar RelatedQueries.jar <redis_port> <redis_port> <time_between_queries> "
          + "<suggestion_threshold> <similarity_threshold> <more_similar> <query_log> <elasticsearch_address> <index_name>");
      System.exit(-1);
    }

    String redisHost = args[0];
    int redisPort = Integer.parseInt(args[1]);
    int timeBetweenQueries = Integer.parseInt(args[2]);
    double suggestionThreshold = Double.parseDouble(args[3]);
    float similarityThreshold = Float.parseFloat(args[4]);
    boolean moreSimilar = Boolean.parseBoolean(args[5]);
    String queryLogPath = args[6];
    String outputFile = null;
    String host = null;
    String index = null;
    if (args.length == 8) {
      outputFile = args[7];
    } else if (args.length == 9) {
      host = args[7];
      index = args[8];
    }

    List<SegmentProcessorQueriesEvaluator> sys = new LinkedList<SegmentProcessorQueriesEvaluator>();

    EntryAcceptCond conds[] = { new SimilarityCond(new LevensteinDistance(), similarityThreshold, moreSimilar) };

    // FIXME: extract to configuration
    LookBackStrategy strategies[] = { new TimeBasedLookBackStrategy(timeBetweenQueries * 1000) };

    // FIXME: extract to configuration
    LookBackTrigger triggers[] = { null };

    // initialize
    for (int i = 0; i < triggers.length; i++) {
      for (int j = 0; j < strategies.length; j++) {
        for (int k = 0; k < conds.length; k++) {
          sys.add(new SegmentProcessorQueriesEvaluator(redisHost, redisPort, queryLogPath, strategies[j], triggers[i],
              conds[k], suggestionThreshold, Settings.REDIS_PREFIX, false));
        }
      }
    }

    OutputWriter outputWriter;
    if (outputFile != null) {
      outputWriter = new SingleFileOutputWriter(outputFile, Settings.REDIS_PREFIX);
    } else if (host != null && index != null) {
      outputWriter = new ElasticsearchHTTPOutputWriter(host, index, Settings.REDIS_PREFIX);
    } else {
      outputWriter = new PerEvaluationFileOutputWriter(Settings.REDIS_PREFIX);
    }
    outputWriter.write(sys);
    outputWriter.close();
  }
}
