package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.SeffParameterEstimation;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class ParameterEstimationPart extends AbstractPipelinePart {

	public ParameterEstimationPart() {
		super(false);
	}

	@Override
	protected void execute() {
		logger.info("Using the parameter estimation.");
		// update parameters
		SeffParameterEstimation estimation = new SeffParameterEstimation();
		estimation.update(getBlackboard().getLoadedPcm().getRepository(), getBlackboard().getMonitoringData());

		// save it
		getBlackboard().persistInMemoryPCM();
	}

}
