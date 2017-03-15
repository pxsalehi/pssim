package de.tum.msrg.sim;

import java.util.Random;

import de.tum.msrg.message.Attribute;
import de.tum.msrg.message.Attribute.Operator;
import de.tum.msrg.sim.DistSampler.DistType;
import de.tum.msrg.utils.ZipfGenerator;

public class AttributeGenerator {
	private Random randGen;
	private DistType attDistOverSubs;
	private int attribStdDevFactor = 3;
	private ZipfGenerator zipfGen;
	private float[] attributeMeans;
	
	public AttributeGenerator(Random randGen, ZipfGenerator zipfGen, DistType attributeDistOverSubs) {
		this.randGen = randGen;
		this.zipfGen = zipfGen;
		this.attDistOverSubs = attributeDistOverSubs;
		initAttributeMeans();
	}
	
	private void initAttributeMeans() {
		attributeMeans = new float[Attribute.noOfAttributes];
		for (int i = 0; i < Attribute.noOfAttributes; i++) {
			attributeMeans[i] = Attribute.highRange * randGen.nextFloat();
		}
	}

	public Attribute[] generateDontCareAttributeSet() {
		Attribute[] attributes = new Attribute[Attribute.noOfAttributes];
		for (int j = 0; j < Attribute.noOfAttributes; j++)
			attributes[j] = Attribute.star();
		return attributes;
	}

	public Attribute[] generateAttributeSet() {
		// randomly set attribute values
		// Random attributeRn = new Random(seedRandom.nextInt());
		Attribute[] attributes = new Attribute[Attribute.noOfAttributes];
		int attribSetCount = 0;
		for (int j = 0; j < Attribute.noOfAttributes; j++) {
			// 50% of the attributes are don't care
			int skip = randGen.nextInt(2);
			if (skip == 0) {
				if (attDistOverSubs == DistType.ZIPF) {
					attributes[j] = getRandAttributeZ(j);
				} else {
					attributes[j] = getRandAttributeG(j, randGen);
				}
				attribSetCount++;
			} else {
				// set the range of the don't care attributes to full
				attributes[j] = new Attribute(Operator.RANGE,
						Attribute.lowRange, Attribute.highRange);
			}
		}
		// at least one attribute should not be a "don't care"
		if (attribSetCount == 0) {
			int attribID = randGen.nextInt(Attribute.noOfAttributes);
			attributes[attribID] = getRandAttributeG(attribID, randGen);
		}
		return attributes;
	}

	private Attribute getRandAttributeZ(int attribID) {
		// - attribute center is gaussian with particular mean
		// - attribute lengths are zipf distributed
		float center = getNextGaussianInRange(attribID);
		float minlength = 10.0f;
		float length = zipfGen.next() + minlength;
		float lowVal = center - (length / 2.0f);
		float highVal = center + (length / 2.0f);
		Operator operator = Operator.RANGE;
		if (lowVal < Attribute.lowRange) {
			lowVal = Attribute.lowRange;
		}
		if (highVal > Attribute.highRange) {
			highVal = Attribute.highRange;
		}
		Attribute a = new Attribute(operator, lowVal, highVal);
		return a;
	}

	// gaussian generated attributes
	private Attribute getRandAttributeG(int attribID, Random randGen) {
		// get limits of the attribute value range
		float lowVal = getNextGaussianInRange(attribID);
		float highVal = getNextGaussianInRange(attribID);
		if (lowVal > highVal) {
			float tmpVal = highVal;
			highVal = lowVal;
			lowVal = tmpVal;
		}
		// get operator and reset the limits accordingly
		// TODO this code is repeated above; put in function
		int opChoise = randGen.nextInt(3);
		Operator operator = Operator.values()[opChoise];
		if (operator == Operator.GREATER_THAN) {
			highVal = Attribute.highRange;
		} else if (operator == Operator.LESS_THAN) {
			lowVal = Attribute.lowRange;
		}
		return new Attribute(operator, lowVal, highVal);
	}

	private float getNextGaussianInRange(int attributeID) {
		float value = -1;
		float mean = attributeMeans[attributeID];
		float stdDeviation = mean > Attribute.highRange / 2.0 
				             ? (Attribute.highRange - mean)
				             : (mean - Attribute.lowRange);
		stdDeviation /= attribStdDevFactor;
		do {
			value = (float) randGen.nextGaussian() * stdDeviation + mean;
		} while ((value <= Attribute.lowRange) || (value >= Attribute.highRange));

		return value;
	}
}
