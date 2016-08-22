package com.sematext.rq.searches.sequencedym;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.pool.impl.GenericObjectPool.Config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import com.sematext.rq.searches.parser.QueryLogEntry;
import com.sematext.rq.searches.parser.QueryLogParser;

/**
 * Query segment processor.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class QuerySeqProcessorDym {
  private String queryPrefix;
  private String countPrefix;
  private String hitsPrefix;
  private JedisPool pool;
  private EntryAcceptCond acceptCond;
  private LookBackTrigger lookBack;
  private LookBackStrategy lookBackStrategy;
  private double suggestionThreshold;
  private boolean nonZeroHitsOnly;

  /**
   * Constructor.
   * 
   * @param redisHost
   *          Redis host
   * @param redisPort
   *          Redis port
   * @param lookBackStrategy
   *          look back strategy
   * @param suggestionThreshold
   *          suggestion score threshold
   * @param prefix
   *          count prefix
   * @param nonZeroHitsOnly
   *          hits prefix
   */
  public QuerySeqProcessorDym(String redisHost, int redisPort, LookBackStrategy lookBackStrategy,
      double suggestionThreshold, String prefix, boolean nonZeroHitsOnly) {
    this(redisHost, redisPort, lookBackStrategy, null, null, suggestionThreshold, prefix, nonZeroHitsOnly);
  }

  /**
   * Constructor.
   * 
   * @param redisHost
   *          Redis host
   * @param redisPort
   *          Redis port
   * @param lookBackStrategy
   *          look back strategy
   * @param lookBack
   *          look back trigger
   * @param acceptCond
   *          entry acceptance condition
   * @param suggestionThreshold
   *          suggestion score threshold
   * @param prefix
   *          count prefix
   * @param nonZeroHitsOnly
   *          hits prefix
   */
  public QuerySeqProcessorDym(String redisHost, int redisPort, LookBackStrategy lookBackStrategy,
      LookBackTrigger lookBack, EntryAcceptCond acceptCond, double suggestionThreshold, String prefix,
      boolean nonZeroHitsOnly) {
    pool = new JedisPool(new Config(), redisHost, redisPort, 0);
    this.acceptCond = acceptCond;
    this.lookBack = lookBack;
    this.lookBackStrategy = lookBackStrategy;
    this.suggestionThreshold = suggestionThreshold;
    this.nonZeroHitsOnly = nonZeroHitsOnly;
    this.queryPrefix = prefix;
    this.countPrefix = "cnt_" + prefix;
    this.hitsPrefix = "hits_" + prefix;
  }

  /**
   * Prepares DB with queries.
   * 
   * @param parser
   *          query log parser
   */
  public void prepareDB(QueryLogParser parser) {
    String curSessionId = "";
    Stack<QueryLogEntry> badQueries = new Stack<QueryLogEntry>();

    final Jedis jedis = pool.getResource();
    QueryLogEntry entry = null;
    Iterator<QueryLogEntry> logIter = parser.sortedQueryLogIterator();
    try {
      while (logIter.hasNext()) {
        try {
          entry = logIter.next();
          jedis.incr(enc(countPrefix + entry.query.toLowerCase()));

          if (nonZeroHitsOnly) {
            // add information about number of hits
            // we only need information if # of results > 0
            byte[] queryKey = enc(hitsPrefix + entry.query.toLowerCase());
            if (entry.numberOfHits > 0) {
              jedis.incr(queryKey);
            } else {
              // we have a query with 0 hits, we need to remove such from Redis if present
              if (jedis.exists(queryKey)) {
                jedis.del(queryKey);
              }
            }
          }

          if (curSessionId.compareTo(entry.sessionId) != 0) {
            lookBackStrategy.clear();
            curSessionId = entry.sessionId;
          }

          if (lookBack == null || lookBack.doLookBack(entry)) {
            badQueries.clear();
            String queryLc = entry.query.toLowerCase();
            byte[] entryQEnc = enc(queryLc);

            Iterator<QueryLogEntry> iterPrev = lookBackStrategy.iterator(entry);
            while (iterPrev.hasNext()) {
              QueryLogEntry prevQ = iterPrev.next();
              if (acceptCond == null || acceptCond.accept(entry, prevQ)) {
                badQueries.push(prevQ);
              }
            }

            while (!badQueries.empty()) {
              QueryLogEntry badQ = badQueries.pop();
              jedis.zincrby(enc(queryPrefix + badQ.query.toLowerCase()), 1.0, entryQEnc);
            }

            lookBackStrategy.addEntry(entry);
          }
        } catch (Exception e) {
          System.err.println(e.getMessage());
          System.err.println("Current entry: " + entry);
        }
      }
    } finally {
      pool.returnResource(jedis);
    }
  }

  /**
   * Returns suggestions for a given query.
   * 
   * @param query
   *          query
   * @return list of suggestions
   */
  public RQTuple<Long, List<Suggestion>> suggest(String query) {
    Jedis jedis = pool.getResource();
    List<Suggestion> suggestions = new ArrayList<QuerySeqProcessorDym.Suggestion>(10);

    byte[] queryEnc = enc(queryPrefix + query);
    byte[] queryCntEnc = enc(countPrefix + query);
    long qCnt = -1;
    try {
      if (jedis.exists(queryEnc)) {
        qCnt = toLong(jedis.get(queryCntEnc));
        Set<Tuple> resp = jedis.zrevrangeWithScores(queryEnc, 0, 40);
        Iterator<Tuple> iter = resp.iterator();
        while (iter.hasNext()) {
          Tuple cur = iter.next();
          String curElem = cur.getElement();
          long curCnt = toLong(jedis.get(enc(countPrefix + curElem)));
          // score formula: (11-score{suggest}/10) * #{suggest}/(#{query}+#{suggest})
          double curScore = cur.getScore() / (cur.getScore() + curCnt);
          if (curScore >= suggestionThreshold) {
            if (!query.equals(curElem)) {
              if (nonZeroHitsOnly) {
                // check if at least one query increased the counter
                // which means at least one query returned non-zero results
                long countMarker = toLong(jedis.get(enc(hitsPrefix + curElem)));
                if (countMarker > 0) {
                  suggestions.add(new Suggestion(curElem, curScore));
                }
              } else {
                suggestions.add(new Suggestion(curElem, curScore));
              }
            }
          }
        }
      }
    } finally {
      pool.returnResource(jedis);
    }
    Collections.sort(suggestions);
    return new RQTuple<Long, List<Suggestion>>(qCnt, suggestions);
  }

  /**
   * Returns suggestions as string.
   * 
   * @param query
   *          query
   * @return suggestions
   */
  public String suggestString(String query) {
    List<Suggestion> suggestions = suggest(query).snd;
    StringBuffer sb = new StringBuffer();
    for (Suggestion s : suggestions) {
      sb.append(s).append(", ");
    }
    return sb.toString();
  }

  /**
   * Returns suggestions.
   * 
   * @param query
   *          query
   * @return suggestions
   */
  public RQTuple<Long, String> suggestTuple(String query) {
    RQTuple<Long, List<Suggestion>> suggTpl = suggest(query);
    List<Suggestion> suggestions = suggTpl.snd;
    Long qCnt = suggTpl.fst;
    StringBuffer sb = new StringBuffer();
    for (Suggestion s : suggestions) {
      sb.append(s).append(", ");
    }
    return new RQTuple<Long, String>(qCnt, sb.toString());
  }

  /**
   * Cleans Jedis pool.
   */
  public void clean() {
    pool.destroy();
  }

  /**
   * Flushes data.
   */
  public void flush() {
    Jedis jedis = pool.getResource();
    try {
      jedis.flushAll();
    } catch (Exception e) {
      System.err.println("Exception while flushing!");
      e.printStackTrace();
    } finally {
      pool.returnResource(jedis);
    }
  }

  private long toLong(byte[] binCnt) {
    if (binCnt == null)
      return 0;
    String cnt;
    try {
      cnt = new String(binCnt, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    return Long.parseLong(cnt);
  }

  private static byte[] enc(String s) {
    try {
      return s.getBytes("UTF-8");
    } catch (UnsupportedEncodingException ignorable) {
      return null;
    }
  }

  /**
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "QuerySeqProcessorDym\n[acceptCond=" + acceptCond + ",\n lookBack=" + lookBack + ",\n lookBackStrategy="
        + lookBackStrategy + "]";
  }

  public String genFilename() {
    return "QuerySeqProcessorDym - acceptCond=" + (acceptCond == null ? "null" : acceptCond.genFilename())
        + ", lookBack=" + lookBack + ", lookBackStrategy=" + lookBackStrategy;
  }

  public JedisPool getPool() {
    return pool;
  }

  private class Suggestion implements Comparable<Suggestion> {
    public final String suggestion;
    public final double cos; // certainty of suggestion
    private final double EPS = 1e-9; // FP error

    public Suggestion(String suggestion, double cos) {
      this.suggestion = suggestion;
      this.cos = cos;
    }

    @Override
    public int compareTo(Suggestion o) {
      double diff = cos - o.cos;
      if (Math.abs(diff) < EPS)
        return 0;
      else if (diff > 0)
        return -1; // NOTE: desc sort!
      else
        return 1;
    }

    @Override
    public String toString() {
      return suggestion + " (" + cos + ")";
    }
  }
}
