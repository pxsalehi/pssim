package de.tum.msrg.utils;

public class TimeMeasurement {

	long start;
	long end;
	String str;

	public TimeMeasurement(String label) {
		str = label;
	}

	public void startTime() {
		start = System.nanoTime();
	}

	public void endTime() {
		end = System.nanoTime();
	}

	public long getExecutionTimeInNano() {
		return end - start;
	}

	public float getExecutionTimeInMilli() {
		return ((end - start) / 1000000f);
	}

	public String getLabel() {
		return str;
	}
}
