package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.test;

import org.eclipse.core.runtime.CoreException;
import org.palladiosimulator.pcm.repository.Repository;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.CorrespondenceResult;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.CorrespondenceTransformer;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.KiekerLogTransformer;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.ToolAdapterIObserve;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class TestExecutor {

	public static void main(String[] args) throws CoreException {
		ToolAdapterIObserve.run("", "");
		System.exit(0);

		MonitoringDataSet reader = new KiekerMonitoringReader("./test-data/simple2", "session-1");
		Repository pcmModel = PcmUtils.loadModel("./test-data/simple2/default.repository");

		CorrespondenceTransformer transf = new CorrespondenceTransformer();
		CorrespondenceResult res = transf.transform(pcmModel, reader);

		KiekerLogTransformer logtrf = new KiekerLogTransformer();
		logtrf.transform(reader, res.getMetadata(), "transformed/");

		transf.saveToFile(res.getModel(), "test.xml");
	}

}
