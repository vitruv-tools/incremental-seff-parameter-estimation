package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.iobserve.model.correspondence.ComponentEntry;
import org.iobserve.model.correspondence.CorrespondenceFactory;
import org.iobserve.model.correspondence.CorrespondenceModel;
import org.iobserve.model.correspondence.OperationEntry;
import org.iobserve.model.correspondence.Part;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class CorrespondenceTransformer {

	public CorrespondenceTransformer() {
	}

	public void saveToFile(CorrespondenceModel model, String path) {
		URI writeModelURI = URI.createFileURI(path);

		final Resource.Factory.Registry resourceRegistry = Resource.Factory.Registry.INSTANCE;
		final Map<String, Object> map = resourceRegistry.getExtensionToFactoryMap();
		map.put("*", new XMIResourceFactoryImpl());

		final ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.setResourceFactoryRegistry(resourceRegistry);

		final Resource resource = resourceSet.createResource(writeModelURI);
		resource.getContents().add(model);
		try {
			resource.save(null);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public CorrespondenceResult transform(Repository repo, MonitoringDataSet data) {
		CorrespondenceModel output = CorrespondenceFactory.eINSTANCE.createCorrespondenceModel();
		CorrespondenceMetadata meta = new CorrespondenceMetadata();

		// part parent
		Part parent = CorrespondenceFactory.eINSTANCE.createPart();
		parent.setModelType(repo);

		// add entries
		for (ServiceCall call : data.getServiceCalls().getServiceCalls()) {
			ResourceDemandingSEFF seff = this.getSeffById(repo, call.getServiceId());
			BasicComponent comp = seff.getBasicComponent_ServiceEffectSpecification();

			if (seff.getDescribedService__SEFF() instanceof OperationSignature) {
				// additional mapping
				meta.mapServiceComponent(call.getServiceId(), comp.getId());

				// create entry for component
				if (!meta.isMappedComponent(comp.getId())) {
					ComponentEntry ce = CorrespondenceFactory.eINSTANCE.createComponentEntry();

					ce.setImplementationId(meta.mapComponent(comp.getId()));
					ce.setComponent(comp);

					parent.getEntries().add(ce);
				}

				// create entry for operation
				if (!meta.isMappedMethod(call.getServiceId())) {
					OperationEntry entry = CorrespondenceFactory.eINSTANCE.createOperationEntry();

					entry.setImplementationId(meta.mapOperation(call.getServiceId()));
					entry.setOperation((OperationSignature) seff.getDescribedService__SEFF());

					parent.getEntries().add(entry);
				}
			}
		}

		// add and return
		output.getParts().add(parent);
		return new CorrespondenceResult(output, meta);
	}

	private ResourceDemandingSEFF getSeffById(Repository repo, String id) {
		List<ResourceDemandingSEFF> seffs = PcmUtils.getObjects(repo, ResourceDemandingSEFF.class);
		return seffs.stream().filter(seff -> seff.getId().equals(id)).findFirst().orElse(null);
	}

}
