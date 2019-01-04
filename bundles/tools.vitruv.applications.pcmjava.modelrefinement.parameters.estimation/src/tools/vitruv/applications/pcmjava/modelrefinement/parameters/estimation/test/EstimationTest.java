package tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.test;

import java.io.File;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.IResourceDemandEstimator;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.estimation.parts.impl.ResourceDemandEstimator;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class EstimationTest {

	public static void main(String[] args) {
		PcmUtils.loadPCMModels();

		InMemoryPCM pcm = new InMemoryPCM();
		pcm.setRepository(PcmUtils.readFromFile(new File("pcm/cocome.repository").getAbsolutePath(), Repository.class));
		pcm.setSystem(PcmUtils.readFromFile(new File("pcm/cocome.system").getAbsolutePath(), System.class));
		pcm.setUsageModel(PcmUtils.readFromFile(new File("pcm/cocome.usagemodel").getAbsolutePath(), UsageModel.class));
		pcm.setAllocationModel(
				PcmUtils.readFromFile(new File("pcm/cocome.allocation").getAbsolutePath(), Allocation.class));
		pcm.setResourceEnvironmentModel(PcmUtils
				.readFromFile(new File("pcm/cocome.resourceenvironment").getAbsolutePath(), ResourceEnvironment.class));

		KiekerMonitoringReader reader = new KiekerMonitoringReader("monitoring/");

		IResourceDemandEstimator estimation = new ResourceDemandEstimator(pcm);
		estimation.prepare(reader);
		estimation.derive();
	}

}
