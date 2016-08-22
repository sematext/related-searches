package com.sematext.rq.searches.sequencedym;

/** 
 * Tuple class holding two values.
 * 
 * @author sematext, http://www.sematext.com/
 */
public class RQTuple<A,B> {
	public final A fst;
	public final B snd;
	
	public RQTuple(A fst, B snd) {
		this.fst = fst;
		this.snd = snd;
	}
}
