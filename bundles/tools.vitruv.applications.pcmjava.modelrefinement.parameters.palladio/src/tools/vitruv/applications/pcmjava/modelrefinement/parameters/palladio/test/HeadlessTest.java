package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.test;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.experimentautomation.application.tooladapter.simulizar.model.SimulizartooladapterPackage;
import org.palladiosimulator.experimentautomation.experiments.ExperimentRepository;
import org.palladiosimulator.experimentautomation.experiments.ExperimentsFactory;
import org.palladiosimulator.experimentautomation.experiments.ExperimentsPackage;
import org.palladiosimulator.pcm.PcmPackage;

import de.fakeller.palladio.environment.PalladioEclipseEnvironment;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.HeadlessExecutor;

public class HeadlessTest {

	public static void main(String[] args) throws CoreException {
		File usageModel = new File("simple2/default.usagemodel");
		File allocationModel = new File("simple2/default.allocation");

		ExperimentsPackage.eINSTANCE.eClass();
		SimulizartooladapterPackage.eINSTANCE.eClass();

		String experimentFile = "model/Experiments/Capacity.experiments";
		ExperimentRepository repoCloned = repoFromFile(experimentFile);

		ExperimentRepository repo = ExperimentsFactory.eINSTANCE.createExperimentRepository();
		PalladioEclipseEnvironment.INSTANCE.setup();
		HeadlessExecutor.run(repoCloned);

	}

	private static ExperimentRepository repoFromFile(String path) {
		PcmPackage.eINSTANCE.eClass();

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		URI filePathUri = org.eclipse.emf.common.util.URI.createFileURI(path);

		Resource resource = resourceSet.getResource(filePathUri, true);
		return (ExperimentRepository) resource.getContents().get(0);
	}

}
