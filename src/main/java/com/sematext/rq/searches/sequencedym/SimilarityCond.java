package com.sematext.rq.searches.sequencedym;

import org.apache.lucene.search.spell.StringDistance;

import com.sematext.rq.searches.parser.QueryLogEntry;

/**
 * Similarity condition.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class SimilarityCond implements EntryAcceptCond {
  private StringDistance sd;
  private float threshold;
  private boolean moreSimilar;

  /**
   * Constructor.
   * 
   * @param sd
   *          string distance
   * @param threshold
   *          distance threshold
   * @param moreSimilar
   *          <code>true</code> if more similar should be only accepted
   */
  public SimilarityCond(StringDistance sd, float threshold, boolean moreSimilar) {
    this.sd = sd;
    this.threshold = threshold;
    this.moreSimilar = moreSimilar;
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.sequencedym.EntryAcceptCond#accept(com.sematext.rq.searches.parser.didyoumean.QueryLogEntry,
   *      com.sematext.rq.searches.parser.didyoumean.QueryLogEntry)
   */
  @Override
  public boolean accept(QueryLogEntry base, QueryLogEntry newEntry) {
    float dist = sd.getDistance(base.query, newEntry.query);
    if (moreSimilar) {
      return (dist >= threshold);
    } else {
      return (dist <= threshold);
    }
  }

  /**
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SimilarityCond\n\t[sd=" + sd + ",\n\t threshold=" + threshold + ",\n\t moreSimilar=" + moreSimilar + "]";
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.apache.lucene.didyoumean.sequencedym.EntryAcceptCond#genFilename()
   */
  public String genFilename() {
    return "SimilarityCond(sd=" + sd + ", threshold=" + threshold + ", moreSimilar=" + moreSimilar + ")";
  }

}
