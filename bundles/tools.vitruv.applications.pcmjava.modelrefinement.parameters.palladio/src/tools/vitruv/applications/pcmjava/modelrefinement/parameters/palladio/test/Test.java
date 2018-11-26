package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.test;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.experimentautomation.application.tooladapter.simucom.model.SimucomtooladapterPackage;
import org.palladiosimulator.experimentautomation.application.tooladapter.simulizar.model.SimulizartooladapterPackage;
import org.palladiosimulator.experimentautomation.experiments.ExperimentRepository;
import org.palladiosimulator.experimentautomation.experiments.ExperimentsPackage;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.ExperimentBuilder;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.HeadlessExecutor;

public class Test {
	// TODO exchange this with certain machine settings
	private static final String java = "/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/Home/bin/java";
	private static final String eclipse = "";

	private static HeadlessExecutor executor;

	@org.junit.BeforeClass
	public static void init() {
		ExperimentsPackage.eINSTANCE.eClass();
		SimucomtooladapterPackage.eINSTANCE.eClass();
		SimulizartooladapterPackage.eINSTANCE.eClass();

		executor = new HeadlessExecutor(java, eclipse);
	}

	@org.junit.Test
	public void test() throws IOException {
		String expPath = "/Users/david/git/incremental-seff-parameter-estimation/bundles/tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio/model/Experiments/Capacity.experiments";
		executor.run(loadRepo(expPath));
	}

	@org.junit.Test
	public void test2() throws IOException {
		ExperimentRepository exec = ExperimentBuilder.create().experiment().desc("Cocome Execution").measurements(1)
				.name("Cocome").reps(1).simucom(1).allocation(CocomeExample.allocation).usagemodel(CocomeExample.usage)
				.repository(CocomeExample.repo).system(CocomeExample.sys).env(CocomeExample.env)
				.slos(CocomeExample.slo_repo).finish().build();

		executor.run(exec);
	}

	private ExperimentRepository loadRepo(String path) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		URI filePathUri = org.eclipse.emf.common.util.URI.createFileURI(path);

		Resource resource = resourceSet.getResource(filePathUri, true);
		return (ExperimentRepository) resource.getContents().get(0);
	}

}
