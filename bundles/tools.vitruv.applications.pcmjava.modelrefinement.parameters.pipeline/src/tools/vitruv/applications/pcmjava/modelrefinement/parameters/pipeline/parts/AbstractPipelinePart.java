package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.DataBlackboard;

public abstract class AbstractPipelinePart {
	private boolean singleExecution;
	private int executions;

	private DataBlackboard blackboard;

	public void setBlackboard(DataBlackboard blackboard) {
		this.blackboard = blackboard;
	}

	public boolean active() {
		return !singleExecution || executions == 0;
	}

	public void run() {
		this.execute();
		this.executions++;
	}

	protected abstract void execute();

	protected AbstractPipelinePart(boolean singleExecution) {
		this.singleExecution = singleExecution;
		this.executions = 0;
	}

	protected DataBlackboard getBlackboard() {
		return blackboard;
	}

}
