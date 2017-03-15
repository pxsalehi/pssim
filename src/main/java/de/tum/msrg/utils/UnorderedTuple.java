package de.tum.msrg.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UnorderedTuple {

	private int i;
	private int j;
	
	public UnorderedTuple(int i, int j) {
		this.i = i;
		this.j = j;
	}
	
	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public int getJ() {
		return j;
	}

	public void setJ(int j) {
		this.j = j;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		result = prime * result + j;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnorderedTuple other = (UnorderedTuple) obj;
		if(i == other.i && j == other.j)
			return true;
		if(i == other.j && j == other.i)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "[" + i + "," + j + "]";
	}

	public static void main(String[] args) {
		HashMap<UnorderedTuple, Set<Integer>> tuples = new HashMap<UnorderedTuple, Set<Integer>>();
		Set<Integer> set = new HashSet<Integer>();
		set.add(1);
		tuples.put(new UnorderedTuple(1, 2), set);
		System.out.println(tuples.get(new UnorderedTuple(1, 2)));
		Set<Integer> res = tuples.get(new UnorderedTuple(2, 1));
		System.out.println((new UnorderedTuple(1, 2)).equals(new UnorderedTuple(2, 1)));
		if(res == null)
			res = tuples.get(new UnorderedTuple(1, 2));
		System.out.println(res);
	}

}
