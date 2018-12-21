package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class LoadMonitoringDataPart extends AbstractPipelinePart {

	private String monitoringDataPath;

	public LoadMonitoringDataPart(String monitoringDataPath) {
		super(false); // always executed
		this.monitoringDataPath = monitoringDataPath;
	}

	@Override
	protected void execute() {
		logger.info("Loading monitoring data.");

		this.getBlackboard().setMonitoringData(new KiekerMonitoringReader(monitoringDataPath));
	}

}