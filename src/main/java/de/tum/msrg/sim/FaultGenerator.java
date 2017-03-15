package de.tum.msrg.sim;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import de.tum.msrg.overlay.OverlayBase;
import de.tum.msrg.overlay.BrokerBase;

// actually this is a fault loader
public class FaultGenerator {
	public enum FAULTY_NODE_TYPE {ALL, NON_LEAVES}
	float maxFaultPercent;
	float minFaultLen;
	float maxFaultLen;
	long lastFaultTime;
	private long timeOffset = 0;
	
	public FaultGenerator() {
	}
	
	public <Node extends BrokerBase> void loadFromFile(OverlayBase<Node> overlay, String filename) {
		BufferedReader faultFile = null;
		try {
			faultFile = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = faultFile.readLine()) != null) {
				if(line.trim().isEmpty() || line.trim().startsWith("#"))
					continue;
				String[] toks = line.split("\t| ");
				if(toks.length != 3) {
					System.err.println("Line in faults file has wrong format (not 3 values)");
					continue;
				}
				int id = Integer.parseInt(toks[0]);
				long start = Long.parseLong(toks[1]);
				long end  = Long.parseLong(toks[2]);
				overlay.getBroker(id).addFailTrace(start + timeOffset, end + timeOffset);
			}
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				faultFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setTimeOffset(long startPublishTime) {
		timeOffset = startPublishTime;
	}
}
