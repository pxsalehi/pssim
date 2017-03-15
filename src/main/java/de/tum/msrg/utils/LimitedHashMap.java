package de.tum.msrg.utils;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LimitedHashMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 1L;
	protected int maxSize;
	
	public LimitedHashMap (int maxSize) {
		this.maxSize = maxSize;
	}
	
	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return size() > maxSize;
	}

	// TODO: move test to TestLimitedHashMap
	public static void main(String[] args) {
		LimitedHashMap<Integer, String> map = new LimitedHashMap<Integer, String>(10);
		for(int i = 0; i < 1000; ++i) {
			map.put(i, i + "");
			System.out.print("MapSize=" + map.size() + "\t\t");
			if(i % 4 == 0)
				System.out.println();
		}
		System.out.println();
		for(int k: map.keySet())
			System.out.println(k + " --> " + map.get(k));
	}

}
