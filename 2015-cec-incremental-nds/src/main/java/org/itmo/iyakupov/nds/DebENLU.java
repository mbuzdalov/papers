package org.itmo.iyakupov.nds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.itmo.iyakupov.nds.Population.IndWithRank;

/**
 * An implementation of Deb's ENLU approach.
 * @author Ilya Yakupov
 */
public class DebENLU {
	protected final Set<Int2DIndividual> individuals = new HashSet<Int2DIndividual>();
	protected final List<Set<Int2DIndividual>> ranks = new ArrayList<Set<Int2DIndividual>>();
	protected final Random random = new Random();

	public void addPoint(Int2DIndividual nInd) {
		if (individuals.contains(nInd)) {
			return;
		} else {
			individuals.add(nInd);
		}

		for (int i = 0; i < ranks.size(); ++i) {
			boolean dominates, dominated, nd;
			dominates = dominated = nd = false;
			Set<Int2DIndividual> dominatedSet = new HashSet<Int2DIndividual>();

			for (Int2DIndividual ind: ranks.get(i)) {
				int domComparsionResult = nInd.compareDom(ind);
				if (domComparsionResult == 0)
					nd = true;
				else if (domComparsionResult > 0) {
					dominated = true;
					break;
				} else {
					dominatedSet.add(ind);
					//ranks.get(i).remove(ind);
					dominates = true;
				}
			}

			if (dominated)
				continue;
			else if (!nd && dominates) {
				Set<Int2DIndividual> newRank = new HashSet<Int2DIndividual>();
				ranks.add(i, newRank);
				newRank.add(nInd);
				return;
			} else {
				ranks.get(i).removeAll(dominatedSet);
				ranks.get(i).add(nInd);
				update(dominatedSet, i + 1);
				return;
			}
		}

		Set<Int2DIndividual> newRank = new HashSet<Int2DIndividual>();
		ranks.add(newRank);
		newRank.add(nInd);
	}

	private void update(Set<Int2DIndividual> dominatedSet, int i) {
		if (i >= ranks.size()) {
			ranks.add(dominatedSet);
		} else {
			Set<Int2DIndividual> newDominatedSet = new HashSet<Int2DIndividual>();
			for (Int2DIndividual iNew : dominatedSet) {
				for (Int2DIndividual iOld : ranks.get(i)) {
					if (iNew.compareDom(iOld) < 0) {
						//ranks.get(i).remove(iOld);
						newDominatedSet.add(iOld);
					}
				}
				ranks.get(i).removeAll(newDominatedSet);
				ranks.get(i).addAll(dominatedSet);
			}
			if (!newDominatedSet.isEmpty())
				update(newDominatedSet, i + 1);
		}
	}

	public IndWithRank getRandWithRank() {
		if (individuals.size() == 0)
			throw new RuntimeException("Can't get random individual from empty population");
		Int2DIndividual randInd = (Int2DIndividual) individuals.toArray()[random.nextInt(individuals.size())];
		return new IndWithRank(randInd, detRankOfExPoint(randInd));
	}

	protected int detRankOfExPoint(Int2DIndividual ind) {
		for (int i = 0; i < ranks.size(); ++i) {
			if (ranks.get(i).contains(ind))
					return i;
		}
		throw new RuntimeException("Point not exists");
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ranks.size(); ++i) {
			sb.append(i);
			sb.append('\n');
			sb.append(ranks.get(i).toString());
			sb.append('\n');
		}
		return sb.toString();
	}

	public void validate() {
		for (int i = 0; i < ranks.size(); ++i) {
			for (Int2DIndividual ind : ranks.get(i)) {
				int rankCalcd = 0;
				Int2DIndividual determinator = null;
				for (Int2DIndividual compInd : individuals) {
					if (compInd != ind && compInd.compareDom(ind) < 0) {
						int compRank = detRankOfExPoint(compInd);
						if (compRank + 1 > rankCalcd) {
							rankCalcd = compRank + 1;
							determinator = compInd;
						}
					}
				}
				if (rankCalcd != i)
					throw new RuntimeException("Population is sorted incorrectly. Point = " + ind + 
							", rk = " + i + ", should be = " + rankCalcd + ", determinator = " + determinator);

			}
		}
	}
}
