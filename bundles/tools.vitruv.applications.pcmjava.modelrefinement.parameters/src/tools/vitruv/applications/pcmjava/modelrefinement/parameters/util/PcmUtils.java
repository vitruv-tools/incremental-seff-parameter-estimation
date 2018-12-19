package tools.vitruv.applications.pcmjava.modelrefinement.parameters.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.pcm.PcmPackage;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.core.entity.InterfaceProvidingEntity;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import de.uka.ipd.sdq.identifier.Identifier;

/**
 * PCM specific utility functions.
 * 
 * @author JP
 *
 */
public class PcmUtils {

	/**
	 * Gets all objects in a {@link Repository} of a specific type.
	 * 
	 * @param <T>
	 *            The type of the objects to find.
	 * @param pcmModel
	 *            The repository which is searched.
	 * @param type
	 *            The type of the objects to find.
	 * @return A list of all found objects or an empty list.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EObject> List<T> getObjects(final EObject pcmModel, final Class<T> type) {
		List<T> results = new ArrayList<>();
		TreeIterator<EObject> it = pcmModel.eAllContents();
		while (it.hasNext()) {
			EObject eo = it.next();
			if (type.isInstance(eo)) {
				results.add((T) eo);
			}
		}
		return results;
	}

	/**
	 * Loads a {@link Repository} form a file.
	 * 
	 * @param filePath
	 *            The repository file.
	 * @return The loaded repository.
	 */
	public static Repository loadModel(final String filePath) {
		// Initialize package.
		PcmPackage.eINSTANCE.eClass();

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		URI filePathUri = org.eclipse.emf.common.util.URI.createFileURI(filePath);

		Resource resource = resourceSet.getResource(filePathUri, true);
		return (Repository) resource.getContents().get(0);
	}

	public static UsageModel loadUsageModel(final String filePath) {
		PcmPackage.eINSTANCE.eClass();

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		URI filePathUri = org.eclipse.emf.common.util.URI.createFileURI(filePath);

		Resource resource = resourceSet.getResource(filePathUri, true);
		return (UsageModel) resource.getContents().get(0);
	}

	public static Allocation loadAllocationModel(final String filePath) {
		PcmPackage.eINSTANCE.eClass();

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		URI filePathUri = org.eclipse.emf.common.util.URI.createFileURI(filePath);

		Resource resource = resourceSet.getResource(filePathUri, true);
		return (Allocation) resource.getContents().get(0);
	}

	/**
	 * Saves the repository into a file.
	 * 
	 * @param filePath
	 *            The file for the repository.
	 * @param repository
	 *            The repository which will be saved.
	 */
	public static void saveModel(final String filePath, final Repository repository) {
		try {
			Files.deleteIfExists(Paths.get(filePath));
		} catch (IOException e1) {
		}
		// Initialize package.
		PcmPackage.eINSTANCE.eClass();

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		URI filePathUri = URI.createFileURI(filePath);
		Resource resource = resourceSet.createResource(filePathUri);
		resource.getContents().add(repository);
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T readFromFile(String path, Class<T> clazz) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		URI filePathUri = org.eclipse.emf.common.util.URI.createFileURI(path);

		Resource resource = resourceSet.getResource(filePathUri, true);
		return clazz.cast(resource.getContents().get(0));
	}

	public static <T extends EObject> void saveToFile(T model, String path) {
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

	public static ServiceEffectSpecification getSeffByProvidedRoleAndSignature(System system, OperationSignature sig,
			OperationProvidedRole role) {
		ProvidedDelegationConnector innerDelegator = getObjects(system, ProvidedDelegationConnector.class).stream()
				.filter(del -> {
					return del.getOuterProvidedRole_ProvidedDelegationConnector().getId().equals(role.getId());
				}).findFirst().orElse(null);

		if (innerDelegator != null) {
			InterfaceProvidingEntity innerEntity = innerDelegator.getInnerProvidedRole_ProvidedDelegationConnector()
					.getProvidingEntity_ProvidedRole();

			return getObjects(innerEntity, ServiceEffectSpecification.class).stream()
					.filter(seff -> seff.getDescribedService__SEFF().getId().equals(sig.getId())).findFirst()
					.orElse(null);
		}

		return null;
	}

	public static Set<OperationSignature> getProvidedOperations(System system) {
		return getObjects(system, OperationProvidedRole.class).stream()
				.map(role -> role.getProvidedInterface__OperationProvidedRole().getSignatures__OperationInterface())
				.flatMap(list -> list.stream()).collect(Collectors.toSet());
	}

	public static ResourceDemandingSEFF resolveSEFF(Repository repo, String seffId) {
		return getElementById(repo, ResourceDemandingSEFF.class, seffId);
	}

	public static <T extends Identifier> T getElementById(EObject obj, Class<T> clazz, String id) {
		return getObjects(obj, clazz).stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
	}

	public static OperationProvidedRole getProvidedRole(System sys, ServiceEffectSpecification seff) {
		String seffSigId = seff.getDescribedService__SEFF().getId();
		return getObjects(sys, OperationProvidedRole.class).stream().filter(role -> {
			return role.getProvidedInterface__OperationProvidedRole().getSignatures__OperationInterface().stream()
					.anyMatch(sig -> sig.getId().equals(seffSigId));
		}).findFirst().orElse(null);
	}
}
