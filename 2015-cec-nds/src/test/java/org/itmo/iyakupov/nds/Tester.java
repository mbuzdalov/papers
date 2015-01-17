package org.itmo.iyakupov.nds;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.itmo.iyakupov.nds.gen.*;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * The main class.
 * @author Ilya Yakupov
 * @author Maxim Buzdalov
 */
public class Tester {
	static int dim;
	public static void main(String[] args) {
		boolean validateAll = "Y".equals(System.getProperty("validate"));
		boolean validateFirst = "1".equals(System.getProperty("validate"));

		List<String[]> results = new ArrayList<String[]>();

		int maxDim = 10;
		try {
			maxDim = Integer.parseInt(System.getProperty("uniSquareTestDataDim"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		int nRuns = 1;
		try {
			nRuns = Integer.parseInt(System.getProperty("nRuns"));
		} catch (Exception e) {
			e.printStackTrace();
		}

        int[] dims = {250, 500, 1000, 2000, 4000};
		//final int step = Math.min(1000, maxDim);
		for (int dim_ : dims) {
		    dim = dim_;
			for (int i = 0; i < nRuns; ++i) {
				boolean validateNow = validateAll || (validateFirst && (i == 0));
                testRandParPer(results, i, validateNow);
				testRandSq(results, i, validateNow);
				testRandPf(results, i, validateNow);
//				testRandCircle(results, i, validateNow);
				testRandStripe(results, i, validateNow);
				testRandDiag(results, i, validateNow);
				testRandDiag2(results, i, validateNow);
				testRandDiag3(results, i, validateNow);
			}
		}

		try {
			saveToCsvFile("lastRun.csv", results);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static void testRandParPer(List<String[]> results, int runId, boolean validateNow) {
		testRandStupid(new Point2DParPerGenerator(), "parper", results, runId, validateNow);
	}

	private static void testRandDiag3(List<String[]> results, int runId, boolean validateNow) {
		testRandStupid(new Point2DDiagGeneratorRandom(), "diagRand", results, runId, validateNow);
	}

	private static void testRandDiag2(List<String[]> results, int runId, boolean validateNow) {
		testRandStupid(new Point2DDiag2Generator(), "diag2", results, runId, validateNow);
	}

	private static void testRandDiag(List<String[]> results, int runId, boolean validateNow) {
		testRandStupid(new Point2DDiagGenerator(), "diag", results, runId, validateNow);
	}

	private static void testRandPf(List<String[]> results, int runId, boolean validateNow) {
		testRandStupid(new Point2DParallelFrontsGenerator(), "parallel fronts", results, runId, validateNow);
	}

	private static void testRandCircle(List<String[]> results, int runId, boolean validateNow) {
		testRandStupid(new Point2DCircleFrontsGenerator(), "circle fronts", results, runId, validateNow);
	}

	private static void testRandSq(List<String[]> results, int runId, boolean validateNow) {
		testRandStupid(new Point2DUniSquareGen(), "square", results, runId, validateNow);
	}

	private static void testRandStupid(ITestDataGen<int[][]> gena, String name, List<String[]> results, int runId, boolean validateNow) {
		System.out.println("Test data (" + name + ") dimension: " + dim);

		int[][] testData = gena.generate(dim, dim);
		testGeneric(testData, name, results, runId, validateNow);
	}

	private static void testRandStripe(List<String[]> results, int runId, boolean validateNow) {
		System.out.println("Test data (stripe) dimension: " + dim);

		final int radius = 10;
		int[][] testData = new Point2DUniStrireXPlusGen(radius).generate(dim, dim);
		testGeneric(testData, "stripe", results, runId, validateNow);
	}

	public static void saveToCsvFile(String fileName, List<String[]> data) throws IOException {
		FileWriter fw = new FileWriter(fileName, false); //not append
 		Writer writer = new BufferedWriter(fw);
		CSVWriter csvWriter = new CSVWriter(writer, ';');
		csvWriter.writeAll(data);
		csvWriter.close();
		writer.close();
		fw.close();
	}

	private static void testGeneric(int[][] testData, String testName, List<String[]> results, int runId, boolean validate) {
		System.gc();
		System.gc();
		Population pop = new Population();
		long start = System.nanoTime();
		for (int[] toAdd : testData) {
			pop.addPoint(new Int2DIndividual(toAdd));
		}
		long finish = System.nanoTime();
		long comparsions = Int2DIndividual.dominationComparsionCount;
		printResults(results, "my", start, finish, comparsions, testName, runId);
		if (validate)
			pop.validate();
		Int2DIndividual.dominationComparsionCount = 0;

		System.gc();
		System.gc();
		DebENLU popEnlu = new DebENLU();
		start = System.nanoTime();
		for (int[] toAdd : testData) {
			popEnlu.addPoint(new Int2DIndividual(toAdd));
		}
		finish = System.nanoTime();
		comparsions = Int2DIndividual.dominationComparsionCount;
		printResults(results, "ENLU", start, finish, comparsions, testName, runId);
		if (validate)
			popEnlu.validate();
		Int2DIndividual.dominationComparsionCount = 0;

		System.gc();
		System.gc();
		DebNSGA2 deb = new DebNSGA2();
		start = System.nanoTime();
		for (int[] currTest: testData) {
			deb.addPoint(currTest);
		}
		comparsions = deb.sort();
		finish = System.nanoTime();
		if (validate)
			deb.validate();
		printResults(results, "NSGA2", start, finish, comparsions, testName, runId);
	}

	private static DecimalFormat df = new DecimalFormat("###0.0#####");
	private static void printResults(List<String[]> results, String algo, long start, long finish, long comparsions, String testName, int runId) {
		String runningTimeSecs = df.format((finish - start) / 1e9);
		System.out.println(algo + " running time (s): " + runningTimeSecs);
		System.out.println(algo + " comparsions count: " + comparsions);

		if (results.isEmpty()) {
			results.add(new String[] {"runId", "test name", "dim", "algo", "running time (s)", "comparsions count"});
		}
		results.add(new String[] {String.valueOf(runId), testName, String.valueOf(dim), algo, runningTimeSecs, String.valueOf(comparsions)});
	}
}
