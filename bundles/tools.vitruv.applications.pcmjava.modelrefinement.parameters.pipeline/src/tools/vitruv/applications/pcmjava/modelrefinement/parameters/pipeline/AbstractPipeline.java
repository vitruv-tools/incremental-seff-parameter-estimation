package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

import java.util.LinkedList;
import java.util.List;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public abstract class AbstractPipeline {
	private DataBlackboard blackboard;

	private List<AbstractPipelinePart> parts;

	protected AbstractPipeline() {
		this.parts = new LinkedList<>();
	}

	protected void addPart(AbstractPipelinePart part) {
		part.setBlackboard(blackboard);
		this.parts.add(part);
	}

	protected abstract void buildPipeline();

}
