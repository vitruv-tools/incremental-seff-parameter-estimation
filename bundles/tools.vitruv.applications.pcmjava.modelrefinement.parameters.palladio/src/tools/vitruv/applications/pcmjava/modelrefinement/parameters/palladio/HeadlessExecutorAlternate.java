package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio;

import java.util.ArrayList;

import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.analyzer.workflow.blackboard.PCMResourceSetPartition;
import org.palladiosimulator.analyzer.workflow.configurations.AbstractPCMWorkflowRunConfiguration;
import org.palladiosimulator.pcm.core.entity.NamedElement;
import org.palladiosimulator.solver.models.PCMInstance;

import de.fakeller.palladio.analysis.pcm2lqn.runner.PcmLqnsAnalyzer;
import de.fakeller.palladio.analysis.pcm2lqn.runner.PcmLqnsAnalyzerContext;
import de.fakeller.palladio.environment.PalladioEclipseEnvironment;
import de.fakeller.performance.analysis.result.PerformanceResult;

public class HeadlessExecutorAlternate {

	public static void init() {
		PalladioEclipseEnvironment.INSTANCE.setup();
	}

	public static PerformanceResult<NamedElement> analyze(PCMInstance instance) {
		final PcmLqnsAnalyzer analyzer = new PcmLqnsAnalyzer();
		final PcmLqnsAnalyzerContext ctx = analyzer.setupAnalysis(instance);
		final PerformanceResult<NamedElement> result = ctx.analyze();

		ctx.untrace();

		return result;
	}

	public static PCMInstance buildPCMInstance(String usageModel, String allocationModel) {
		PCMResourceSetPartition rs = new PCMResourceSetPartition();
		rs.getResourceSet().setURIConverter(PalladioEclipseEnvironment.INSTANCE.getUriConverter());

		ArrayList<String> fileList = new ArrayList<String>();
		fileList.add(usageModel);
		fileList.add(allocationModel);

		rs.initialiseResourceSetEPackages(AbstractPCMWorkflowRunConfiguration.PCM_EPACKAGES);
		for (String modelFile : fileList) {
			rs.loadModel(URI.createFileURI(modelFile));
		}
		rs.resolveAllProxies();

		return new PCMInstance(rs);
	}

}
