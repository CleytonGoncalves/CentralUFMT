package com.cleytongoncalves.centralufmt.util;


public final class Pair<P, S> {
	private final P item1;
	private final S item2;
	
	public Pair(P item1, S item2) {
		this.item1 = item1;
		this.item2 = item2;
	}
	
	public P getItem1() {
		return item1;
	}
	
	public S getItem2() {
		return item2;
	}
	
}
