package com.sematext.rq.searches.sequencedym;

/** 
 * Triple class holding three values.
 *
 * @author sematext, http://www.sematext.com/
 */
public class RQTriple<A,B,C> {
	public final A fst;
	public final B snd;
	public final C trd;
	
	public RQTriple(A fst, B snd, C trd) {
		this.fst = fst;
		this.snd = snd;
		this.trd = trd;
	}
}
