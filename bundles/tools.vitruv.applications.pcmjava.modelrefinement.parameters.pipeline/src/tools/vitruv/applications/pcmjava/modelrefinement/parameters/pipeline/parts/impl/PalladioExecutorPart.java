package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import java.io.File;
import java.io.IOException;

import org.palladiosimulator.experimentautomation.experiments.ExperimentRepository;
import org.palladiosimulator.servicelevelobjective.ServiceLevelObjectiveRepository;
import org.palladiosimulator.servicelevelobjective.ServicelevelObjectiveFactory;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.ExperimentBuilder;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.HeadlessExecutor;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results.PalladioAnalysisResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class PalladioExecutorPart extends AbstractPipelinePart {
	private HeadlessExecutor executor;

	public PalladioExecutorPart(String javaPath, String eclipsePath) {
		super(false);

		executor = new HeadlessExecutor(javaPath, eclipsePath);
		System.out.println(javaPath);
	}

	@Override
	protected void execute() {
		logger.info("Starting PCM Simucom Analysis.");

		// create temp file containing simple slos
		File tempSloFile;
		try {
			tempSloFile = File.createTempFile("temp", "slo");
		} catch (IOException e1) {
			logger.warn("Failed to create temporary file.");
			return;
		}
		PcmUtils.saveToFile(ServicelevelObjectiveFactory.eINSTANCE.createServiceLevelObjectiveRepository(),
				tempSloFile.getAbsolutePath());

		// build experiments repo
		InMemoryPCM currentPCM = getBlackboard().getLoadedPcm();
		// TODO also variables not hardcoded maybe
		ExperimentRepository exec = ExperimentBuilder.create().experiment().desc("Automatic Palladio Execution")
				.name("EPS").reps(3).simucom(50).allocation(currentPCM.getAllocationModel())
				.usagemodel(currentPCM.getUsageModel()).repository(currentPCM.getRepository())
				.system(currentPCM.getSystem()).env(currentPCM.getResourceEnvironmentModel()).measurementtime(360000)
				.slos(PcmUtils.readFromFile(tempSloFile.getAbsolutePath(), ServiceLevelObjectiveRepository.class))
				.finish().build();

		// perform the analysis
		try {
			PalladioAnalysisResults res = executor.run(exec);
			getBlackboard().setAnalysisResults(res);
		} catch (IOException e) {
			logger.warn("Failed to perform the Analysis correctly.");
		}

	}

}
