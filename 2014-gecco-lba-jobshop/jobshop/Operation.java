package jobshop;

/**
 * Single operation of a job.
 * 
 * @author Arina Buzdalova
 */
class Operation implements Comparable<Operation> {
	private final int number;
	private final int job;
	private final int machine;
	private final int time;
	private int completionTime;
	
	/**
	 * Constructs the {@link Operation} with the specified parameters.
	 * 
	 * @param number the number of this operation in the corresponding job
	 * @param job the corresponding job
	 * @param machine the machine that should perform this operation
	 * @param time the performance time of this operation
	 */
	public Operation(int number, int job, int machine, int time) {
		this.number = number;
		this.job = job;
		this.machine = machine;
		this.time = time;
	}

	public int getCompletionTime() {
		return completionTime;
	}
	
	public void setCompletionTime(int completionTime) {
		this.completionTime = completionTime;
	}
	
	/**
	 * @return the number of this operation in the job
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @return the job
	 */
	public int getJob() {
		return job;
	}

	/**
	 * @return the machine that should perform this operation
	 */
	public int getMachine() {
		return machine;
	}

	/**
	 * @return the processing time of this operation
	 */
	public int getTime() {
		return time;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + job;
		result = prime * result + number;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Operation other = (Operation) obj;
        return job == other.job && number == other.number;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Operation o) {
		return getCompletionTime() - o.getCompletionTime();
	}	
	
}
