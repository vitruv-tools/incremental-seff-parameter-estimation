package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.test;

import org.junit.Test;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.UsageModelExtractor;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class UsageModelExtractorTest {

	@Test
	public void test() {
		Repository pcmModel = PcmUtils.loadModel("cocome/cocome.repository");
		UsageModel usage = PcmUtils.readFromFile("cocome/cocome.usagemodel", UsageModel.class);
		MonitoringDataSet data = new KiekerMonitoringReader(
				"monitoring-data/monitoring/kieker-20181211-155744-10181966974082-UTC--KIEKER");
		MonitoringDataMapping mapping = new MonitoringDataMapping();
		mapping.addParameterMapping("products.VALUE", "saleTO.NUMBER_OF_ELEMENTS");

		UsageModelExtractor extractor = new UsageModelExtractor(usage, mapping);
		extractor.update(pcmModel, data);
	}

}
