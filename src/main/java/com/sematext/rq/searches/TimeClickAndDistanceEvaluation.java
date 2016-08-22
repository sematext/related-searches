/*
 *    Copyright (c) Sematext International
 *    All Rights Reserved
 *
 *    THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Sematext International
 *    The copyright notice above does not evidence any
 *    actual or intended publication of such source code.
 */
package com.sematext.rq.searches;

import org.apache.lucene.search.spell.LevensteinDistance;

import com.sematext.rq.searches.common.Settings;
import com.sematext.rq.searches.eval.SegmentProcessorQueriesEvaluator;
import com.sematext.rq.searches.output.CombinedQueriesEvaluatorESWriter;
import com.sematext.rq.searches.output.CombinedQueriesEvaluatorFileWriter;
import com.sematext.rq.searches.output.OutputWriter;
import com.sematext.rq.searches.sequencedym.LookBackTrigger;
import com.sematext.rq.searches.sequencedym.SimilarityCond;
import com.sematext.rq.searches.sequencedym.TimeBasedLookBackStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Time and distance based evaluator.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class TimeClickAndDistanceEvaluation {
  /**
   * Main class.
   * 
   * @param args
   *          arguments
   * @throws IOException
   *           throw when error occurs
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 11 && args.length != 12) {
      System.err.println("Usage:");
      System.err.println("java -jar RelatedQueries.jar <redis_port> <redis_port> <time_between_queries> "
          + "<suggestion_threshold> <similarity_threshold> <more_similar> <distance_boost> "
          + "<timebased_boost> <non_zero_hits_only> <query_log> <output_file>");
      System.err.println("java -jar RelatedQueries.jar <redis_port> <redis_port> <time_between_queries> "
          + "<suggestion_threshold> <similarity_threshold> <more_similar> <distance_boost> "
          + "<timebased_boost> <non_zero_hits_only> <query_log> <es_host> <es_index>");
      System.exit(-1);
    }

    String redisHost = args[0];
    int redisPort = Integer.parseInt(args[1]);
    int timeBetweenQueries = Integer.parseInt(args[2]);
    double suggestionThreshold = Double.parseDouble(args[3]);
    float similarityThreshold = Float.parseFloat(args[4]);
    boolean moreSimilar = Boolean.parseBoolean(args[5]);
    float distanceBoost = Float.parseFloat(args[6]);
    float timebasedBoost = Float.parseFloat(args[7]);
    boolean nonZeroHitsOnly = Boolean.parseBoolean(args[8]);
    String queryLogPath = args[9];
    String outputFile = args[10];
    String esIndex = null;
    if (args.length == 12) {
      esIndex = args[11];
    }

    List<Float> boosts = new ArrayList<Float>();
    boosts.add(distanceBoost);
    boosts.add(timebasedBoost);

    List<String> prefixes = new ArrayList<String>();
    prefixes.add(Settings.STRING_DISTANCE_PREFIX);
    prefixes.add(Settings.TIME_CLICK_PREFIX);

    LookBackTrigger lookBackTrigger = null;

    SegmentProcessorQueriesEvaluator stringDistanceApproach = new SegmentProcessorQueriesEvaluator(redisHost, redisPort,
        queryLogPath, new TimeBasedLookBackStrategy(60 * 1000), lookBackTrigger,
        new SimilarityCond(new LevensteinDistance(), similarityThreshold, moreSimilar), suggestionThreshold,
        Settings.STRING_DISTANCE_PREFIX, nonZeroHitsOnly);

    SegmentProcessorQueriesEvaluator timeBasedApproach = new SegmentProcessorQueriesEvaluator(redisHost, redisPort,
        queryLogPath, new TimeBasedLookBackStrategy(timeBetweenQueries * 1000), lookBackTrigger,
        new SimilarityCond(new LevensteinDistance(), 0.1f, true), suggestionThreshold, Settings.TIME_CLICK_PREFIX,
        nonZeroHitsOnly);

    List<SegmentProcessorQueriesEvaluator> approachesList = new ArrayList<SegmentProcessorQueriesEvaluator>();
    approachesList.add(stringDistanceApproach);
    approachesList.add(timeBasedApproach);

    OutputWriter writer = null;
    boolean isEsEnabled = Boolean.parseBoolean(System.getProperty(Settings.ES_OUTPUT_ENABLED) != null
        ? System.getProperty(Settings.ES_OUTPUT_ENABLED) : "false");
    if (isEsEnabled) {
      writer = new CombinedQueriesEvaluatorESWriter(outputFile, esIndex, boosts, prefixes, 2, nonZeroHitsOnly);
    } else {
      writer = new CombinedQueriesEvaluatorFileWriter(outputFile, boosts, prefixes, 2, nonZeroHitsOnly);
    }

    writer.write(approachesList);
    writer.close();
  }
}
