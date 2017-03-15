package de.tum.msrg.utils;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class LimitedHashSet<E> extends AbstractSet<E> implements Set<E> {
	
	private transient LimitedHashMap<E, Object> map;
	private static final Object PRESENT = new Object();
	
	public LimitedHashSet(int maxSize) {
		map = new LimitedHashMap<E, Object>(maxSize);
	}

	@Override
	public boolean add(E e) {
		return map.put(e, PRESENT)==null;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o)==PRESENT;
	}

	@Override
	public int size() {
		return map.size();
	}

	// TODO: move to testLimitedHashSet
	public static void main(String[] args) {
		LimitedHashSet<Integer> set = new LimitedHashSet<Integer>(1000000000);
		for(int i = 0; i < 1000; ++i) {
			set.add(i);
			System.out.print("SetSize=" + set.size() + "\t\t");
			if(i % 4 == 0)
				System.out.println();
		}
		System.out.println();
		for(int k: set)
			System.out.print(k + ", ");
	}

}
