package jobshop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.Arrays;

/**
 * Some utilities for the Job Shop Problem.
 * 
 * @author Arina Buzdalova
 */
public class JobShopUtils {
	/**
	 * Generates a population of random job shop individuals, 
	 * which are permutations of jobs with {@code machines} repetitions.
	 * @param size the number of individuals in the population
	 * @param jobs the number of jobs
	 * @param machines the number of machines, which is equal to jobs repetitions in an individual
	 * @param rng the source of randomness
	 * @return the randomly generated population
	 */
	public static List<List<Integer>> generateRandomPopulation(int size, int jobs, int machines, Random rng) {
		List<List<Integer>> population = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			population.add(generateRandomIndividual(jobs, machines, rng));
		}
		return population;
	}
	
	public static List<Integer> generateRandomIndividual(int jobs, int machines, Random rng) {
		List<Integer> donor = new ArrayList<>();
		for (int i = 0; i < jobs; i++) {
			for (int j = 0; j < machines; j++) {
				donor.add(i);
			}
		}
        Collections.shuffle(donor, rng);
		return donor;
	}
	
	public static List<Operation>[] createJobsList(int[][] times, int[][] machines) {
		int n = times.length;
		int m = times[0].length;
		@SuppressWarnings("unchecked")
        List<Operation>[] list = new List[n];
		for (int job = 0; job < n; job++) {
			list[job] = new ArrayList<>();
			for (int op = 0; op < m; op++) {
				list[job].add(new Operation(op, job, machines[job][op], times[job][op]));
			}
		}
		return list;
	}
	
	public static int sumTimes(int[][] times) {
		int sum = 0;
		for (int[] t : times) {
			for (int v : t) {
				sum += v;
			}
		}
		return sum;
	}
	
	public static int calculateMinTime(int[][] times) {
		int min = times[0][0];
		for (int[] t: times) {
			for (int v : t) {
				min = Math.min(min, v);
			}
		}
		return min;
	}
	
	/**
	 * Evaluates flow times for each job according to the 
	 * schedule built using Giffler & Thompson algorithm.
	 * 
	 * @param individual	the individual that encodes priorities for schedule building.
	 * 						It should be a permutation of job numbers with repetitions.
	 * @param jobs array of all jobs, each job consists of operations
	 * @return times of completion for each job
	 */
	public static int[] evalFlowTimes(List<Integer> individual, List<Operation>[] jobs) {
		int n = jobs.length;
		int m = jobs[0].size();
	
		int[] jobStops = new int[n];
		int[] machineStops = new int[m];
		
		Arrays.fill(jobStops, 0);
		Arrays.fill(machineStops, 0);
		
		Map<Operation, Integer> positions = getPositions(individual, jobs);
 		
		List<Operation> A = new ArrayList<>();

        for (List<Operation> job : jobs) {
            Operation first = job.get(0);
            first.setCompletionTime(first.getTime());
            A.add(first);
        }
		
		while (!A.isEmpty()) {
			Operation earliest = Collections.min(A);
			int machine = earliest.getMachine();
			int completion = earliest.getCompletionTime();
			
			Operation selected = earliest;
			
			for (Operation op : A) {
				if (op.getMachine() != machine) continue;				
				if (startTime(op, jobStops, machineStops) >= completion) continue;				
				if (positions.get(op) > positions.get(selected)) continue;				
				selected = op;
			}
			
//			System.out.println(String.format("(J=%d, N=%d, M=%d, T=%d) %d",
//                    selected.getJob(),
//                    selected.getNumber(),
//                    selected.getMachine(),
//                    selected.getTime(),
//                    startTime(selected, jobStops, machineStops)));

			machineStops[selected.getMachine()] = selected.getCompletionTime();
			jobStops[selected.getJob()] = selected.getCompletionTime();
			A.remove(selected);

//            System.out.println("Jobs: " + Arrays.toString(jobStops));
//            System.out.println("Machines: " + Arrays.toString(machineStops));

            if (selected.getNumber() + 1 < m) {
                A.add(jobs[selected.getJob()].get(selected.getNumber() + 1));
			}
            for (Operation op : A) {
                op.setCompletionTime(startTime(op, jobStops, machineStops) + op.getTime());
            }
		}

//        System.out.println("===");

		return jobStops;
	}
	
	private static int startTime(Operation op, int[] jobStops, int[] machineStops) {
		return Math.max(jobStops[op.getJob()], machineStops[op.getMachine()]);
	}
	
	private static Map<Operation, Integer> getPositions(List<Integer> individual, List<Operation>[] jobs) {
		Map<Operation, Integer> indexes = new HashMap<>();
		int[] number = new int[jobs.length];
		Arrays.fill(number, 0);
		for (int i = 0; i < individual.size(); i++) {
			int job = individual.get(i);
			indexes.put(jobs[job].get(number[job]), i);
			number[job]++;
		}
		return indexes;
	}
}
