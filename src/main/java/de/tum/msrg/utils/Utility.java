package de.tum.msrg.utils;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.tum.msrg.message.Advertisement;
import de.tum.msrg.message.Publication;
import de.tum.msrg.message.Subscription;
import de.tum.msrg.overlay.BrokerBase;
import de.tum.msrg.overlay.OverlayLink;

public class Utility {

	public static void printMatrix(float[][] matrix, int rows, int cols) {
		System.out.println();
		for (int i = 0; i < rows; i++) {
			System.out.format("%3d", i);
			System.out.print(" [ ");
			for (int j = 0; j < cols; j++) {
				System.out.format("%10.5f ", matrix[i][j]);
			}
			System.out.println(" ]");
		}
		System.out.println();
		System.out.println();
	}

	public static void printMatrix(int[][] matrix, int rows, int cols) {
		for (int i = 0; i < rows; i++) {
			System.out.format("%3d", i);
			System.out.print(" [ ");
			for (int j = 0; j < cols; j++) {
				System.out.format("%3d ", matrix[i][j]);
			}
			System.out.println(" ]");
		}
		System.out.println();
		System.out.println();
	}

	public static void printSubscriptionSet(Set<Subscription> subscriptions) {
		for (Subscription sub : subscriptions) {
			System.out.println(sub);
		}
	}

	public static String getArrayString(float[] array, String format) {
		String outStr = "";
		for (float val : array) {
			outStr += String.format(format, val);
		}
		return outStr;
	}

	public static String getMatrixString(float[][] array, String format) {
		String outStr = "";
		for (float[] val : array) {
			outStr += getArrayString(val, format) + "\n";
		}
		return outStr.substring(0, outStr.length() - 1);
	}

	public static <T> void reverse(List<T> array) {
		int size = array.size();
		int midIndex = size / 2;
		for (int i = 0; i < midIndex; i++) {
			swap(array, i, (size - 1 - i));
		}
	}
	
	public static double average(List<Double> list) {
		double sum = 0;
		for(Double d: list)
			sum += d;
		return sum / list.size();
	}

	private static <T> void swap(List<T> array, int a, int b) {
		T tmp = array.get(a);
		array.set(a, array.get(b));
		array.set(b, tmp);
	}

	public static double calculateTotalCost(int[][] tree, float[][] weights, int nodeCount) {
		double sum = 0.0;
		for (int i = 0; i < nodeCount; i++) {
			for (int j = 0; j < nodeCount; j++) {
				sum = sum + (tree[i][j] * weights[i][j]);
			}
		}
		return sum;
	}

	public static float[][] generateRandomLinkWeights(int nodeCount) {
		Random rn = new Random();
		float[][] w = new float[nodeCount][nodeCount];

		for (int i = 0; i < nodeCount; i++) {
			for (int j = 0; j < nodeCount; j++) {
				if (i == j)
					continue;
				w[i][j] = rn.nextFloat();
			}
		}

		return w;
	}

	public static void writeAllSubscriptions(BrokerBase[] pNodes, String fileName) {
		List<Subscription> subs = null;
		try {
			PrintStream outStream = new PrintStream(fileName);
			for (BrokerBase pNode : pNodes) {
				subs = pNode.getPubSubLoad().getSubs();
				for (Subscription sub : subs) {
					outStream.printf("[%d] [%d] [%s]", pNode.getId(), sub.getID(),
							sub.toString());
					outStream.println();
				}
			}
			outStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("subscriptions file not found");
			System.exit(1);
		}
	}

	public static void writeAllPublications(BrokerBase[] pNodes, String fileName) {

		List<Publication> pubs = null;
		try {
			PrintStream outStream = new PrintStream(fileName);
			for (BrokerBase pNode : pNodes) {
				pubs = pNode.getPubSubLoad().getPubs();
				for (Publication pub : pubs) {
					outStream.printf("[%d] [%d] [%s]", pNode.getId(), pub.getId(), pub.toString());
					outStream.println();
				}
			}
			outStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("subscriptions file not found");
			System.exit(1);
		}
	}

	public static void writeAllAdvertisements(BrokerBase[] pNodes, String fileName) {

		List<Advertisement> advs = null;
		try {
			PrintStream outStream = new PrintStream(fileName);
			for (BrokerBase pNode : pNodes) {
				advs = pNode.getPubSubLoad().getAdvs();
				for (Advertisement adv : advs) {
					outStream.printf("[%d] [%d] [%s]", pNode.getId(), adv.getID(), adv.toString());
					outStream.println();
				}
			}
			outStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("subscriptions file not found");
			System.exit(1);
		}
	}

	public static void dumpNodeInfo(BrokerBase[] pNodes, String fileName) {
		List<OverlayLink> links = null;
		try {
			PrintStream outStream = new PrintStream(fileName);

			for (BrokerBase pNode : pNodes) {
				outStream.printf("%d, %s, %d", pNode.getId(), pNode.getUnderlayNode().toString(),
						pNode.getPubSubLoad().getPubRate());
				outStream.println();
				links = pNode.getOverlayLinks();
				for (OverlayLink link : links) {
					outStream.printf("%d, %f, %d, %d", pNode.getId(), link.getLatency(),
							link.getToNode().getId(), link.getFromNode().getId());
					outStream.println();
				}
			}
			outStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("subscriptions file not found");
			System.exit(1);
		}

	}

	public static float[][] normalizeMatrix(float[][] matrix, int rows, int cols) {

		float[][] norm = new float[rows][cols];

		// assumes 0 is the minimum in the matrix

		// find max value in matrix
		float max = Float.MIN_VALUE;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (matrix[i][j] > max) {
					max = matrix[i][j];
				}
			}
		}

		// let max value = 1. all other values are less than this amount
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				norm[i][j] = matrix[i][j] / max;
			}
		}

		return norm;
	}

	public static boolean isMatrixSymmetric(float[][] matrix, int rows, int cols) {
		boolean symmetric = true;

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (matrix[i][j] != matrix[j][i]) {
					symmetric = false;
					break;
				}
			}
		}
		return symmetric;
	}

	public static boolean isEqual(int[][] matrix1, int[][] matrix2, int rows, int cols) {

		boolean same = true;

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (matrix1[i][j] != matrix2[i][j]) {
					same = false;
					break;
				}
			}
		}

		return same;
	}
}
