package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class CrossValidationPart extends AbstractPipelinePart {

	public CrossValidationPart() {
		super(false);
	}

	@Override
	protected void execute() {
		logger.info("Comparing the Simucom Analysis results with the monitoring data.");

		// TODO
	}

}
