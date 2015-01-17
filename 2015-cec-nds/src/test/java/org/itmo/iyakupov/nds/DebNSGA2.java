package org.itmo.iyakupov.nds;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An implementation of Deb's fast non-dominated sorting.
 * @author Ilya Yakupov
 */
public class DebNSGA2 {
	protected static class Individual {
		int[] impl;
		int nDominating;
		int rank;
		List<Individual> dominated;

		public Individual(int[] fitnesses) {
			impl = fitnesses;
			nDominating = 0;
			rank = 0;
			dominated = new ArrayList<Individual>();
		}
	}

	protected List<Individual> pop = new ArrayList<Individual>();
	protected List<List<Individual>> fronts = new ArrayList<List<Individual>>();
	protected Random generator = new Random();
	protected int dominationComparsionCount = 0;

	public void addPoint(int[] fitnesses) {
		pop.add(new Individual(fitnesses));
	}

	public int sort() {
		dominationComparsionCount = 0;
		for (int i = 0; i < pop.size(); ++i) {
			for (int j = 0; j < pop.size(); ++j) {
				if (i != j) {
					DomStatus domStatus = dominates(pop.get(i).impl, pop.get(j).impl);
					if (domStatus == DomStatus.DOMINATES) {
						pop.get(i).dominated.add(pop.get(j));
					} else if (domStatus == DomStatus.DOMINATED) {
						pop.get(i).nDominating++;
					}  
				}
			}

			//System.err.println(pop.get(i).nDominating);
			if (pop.get(i).nDominating == 0) {
				addToFront(1, pop.get(i));
				pop.get(i).rank = 0;
			}
		}

		int currFront = 0;
		while (currFront < fronts.size()) {
			List<Individual> newFront = new ArrayList<Individual>();
			for (Individual fromCurrFront: fronts.get(currFront)) {
				for (Individual dominatedByCurr: fromCurrFront.dominated) {
					dominatedByCurr.nDominating--;
					if (dominatedByCurr.nDominating == 0) {
						newFront.add(dominatedByCurr);
						dominatedByCurr.rank = fronts.size();
					}
				}
			}
			if (newFront.size() > 0) {
				fronts.add(newFront);
				currFront++;
			} else {
			    break;
			}
		}
		return dominationComparsionCount;
	}

	private void addToFront(int i, Individual individual) {
		List<Individual> workingFront = null;
		if (i <= fronts.size()) {
			workingFront = fronts.get(i - 1);
		} else {
			workingFront = new ArrayList<Individual>();
			fronts.add(workingFront);
		}
		workingFront.add(individual);
	}

	public Result getRandomPoint() {
		int rank = generator.nextInt(fronts.size());
		int nInFront = generator.nextInt(fronts.get(rank).size());
		int[] res = fronts.get(rank).get(nInFront).impl;
		return new Result(rank + 1, res);
	}

	private DomStatus dominates(int[] p1, int[] p2) {
		dominationComparsionCount += 2;

		boolean lt = false;
		boolean gt = false;
		for (int i = 0; i < p1.length; ++i) {
		    // we count this as one comparison
			if (p1[i] > p2[i]) {
				gt = true;
			} else if (p1[i] < p2[i]) {
				lt = true;
			}
		}

		if (lt && !gt) { //p1 dominates p2
			return DomStatus.DOMINATES;
		} else if (!lt && gt) { //p1 dominates p2
			return DomStatus.DOMINATED;
		}

		return DomStatus.EQUALS;
	}


	public void validate() {
		for (int i = 0; i < fronts.size(); ++i) {
			for (Individual cInd: fronts.get(i)) {
				int cRankCalcd = 0;
				for (Individual compInd: pop) {
					if (compInd != cInd) {
						if (dominates(compInd.impl, cInd.impl) == DomStatus.DOMINATES) {
							cRankCalcd = Math.max(cRankCalcd, compInd.rank + 1);
						}
					}
				}
				if (cRankCalcd != i) {
					throw new RuntimeException("Population is sorted incorrectly");
				}
			}
		}
	}
}
