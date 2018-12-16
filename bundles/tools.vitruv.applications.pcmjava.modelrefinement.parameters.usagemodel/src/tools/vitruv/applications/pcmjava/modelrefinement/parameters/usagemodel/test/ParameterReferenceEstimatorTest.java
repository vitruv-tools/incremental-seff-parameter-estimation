package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.test;

import java.util.stream.Collectors;

import org.junit.Test;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.ParameterRelevanceEstimator;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class ParameterReferenceEstimatorTest {

	@Test
	public void test() {
		Repository pcmModel = PcmUtils.loadModel("cocome/cocome.repository");
		UsageModel usage = PcmUtils.readFromFile("cocome/cocome.usagemodel", UsageModel.class);

		ResourceDemandingSEFF seff = PcmUtils.resolveSEFF(pcmModel, "bookSale");

		ParameterRelevanceEstimator relEst = new ParameterRelevanceEstimator();
		System.out.println(relEst.getRelevantParameters(seff).stream().map(para -> para.getParameterName())
				.collect(Collectors.toList()));
	}

}
