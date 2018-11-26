package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.types.Commandline;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.edp2.models.ExperimentData.ExperimentGroup;
import org.palladiosimulator.edp2.models.ExperimentData.ExperimentRun;
import org.palladiosimulator.edp2.models.ExperimentData.ExperimentSetting;
import org.palladiosimulator.edp2.models.ExperimentData.Measurement;
import org.palladiosimulator.edp2.models.ExperimentData.MeasurementRange;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringpointPackage;
import org.palladiosimulator.experimentautomation.abstractsimulation.AbstractsimulationFactory;
import org.palladiosimulator.experimentautomation.abstractsimulation.FileDatasource;
import org.palladiosimulator.experimentautomation.application.tooladapter.simucom.model.SimuComConfiguration;
import org.palladiosimulator.experimentautomation.application.tooladapter.simulizar.model.SimuLizarConfiguration;
import org.palladiosimulator.experimentautomation.experiments.ExperimentRepository;
import org.palladiosimulator.pcmmeasuringpoint.PcmmeasuringpointPackage;

public class HeadlessExecutor {
	private static final String[] STATIC_ARGS = new String[] { "-XstartOnFirstThread",
			"-Declipse.p2.data.area=@config.dir/p2", "-Declipse.pde.launch=true", "-Dfile.encoding=UTF-8" };

	// very static maybe exchange this with dynamic search of equinox
	private static final String EQUINOX_OFFSET = "plugins/org.eclipse.equinox.launcher_1.4.0.v20161219-1356.jar";
	private static final String EQUINOX_MAIN = "org.eclipse.equinox.launcher.Main";
	private static final String PCM_APPLICATION = "org.palladiosimulator.experimentautomation.application";

	// OS DEPENDENT STRING -> BUILD THIS AUTOMATICALLY (windows: -os win32 -ws win32
	// -arch x86_64 ..
	private static final String ECL_APP_STATIC = "-os macosx -ws cocoa -arch x86_64 -nl de_DE -consoleLog -clean ";

	private String javaPath;
	private String eclipsePath;

	public HeadlessExecutor(String javaPath, String eclipsePath) {
		this.javaPath = javaPath;
		this.eclipsePath = eclipsePath;
	}

	public void run(ExperimentRepository repository) throws IOException {
		// create experiment result folder
		Path directory = Files.createTempDirectory("result");
		modifyDatasources(repository, directory);

		// create experiment file
		File temp = File.createTempFile("exp", ".experiment");
		saveToFile(repository, temp.getAbsolutePath());

		// build command
		String fullCommand = buildCommand(temp);

		// execute command
		if (executeCommandBlocking(fullCommand)) {
			// parse results
			parseResults(directory);
		}

		// finally delete temp files
		temp.delete();
		FileUtils.deleteDirectory(directory.toFile());
	}

	private void parseResults(Path directory) {
		MeasuringpointPackage.eINSTANCE.eClass();
		PcmmeasuringpointPackage.eINSTANCE.eClass();

		File[] dict = directory.toFile().listFiles(file -> FilenameUtils.getExtension(file.getName()).equals("edp2"));
		if (dict.length == 1) {
			ExperimentGroup group = readFromFile(dict[0].getAbsolutePath(), ExperimentGroup.class);
			// TODO do something with the data
			ExperimentSetting setting = group.getExperimentSettings().get(0);
			for (ExperimentRun run : setting.getExperimentRuns()) {
				for (Measurement msm : run.getMeasurement()) {
					for (MeasurementRange range : msm.getMeasurementRanges()) {
						// TODO
					}
				}
			}
		}
	}

	private void modifyDatasources(ExperimentRepository repository, Path directory) {
		// modify all data sources to file because we want to parse them
		FileDatasource ds = AbstractsimulationFactory.eINSTANCE.createFileDatasource();
		ds.setLocation(directory.toString() + File.separator);

		repository.eAllContents().forEachRemaining(obj -> {
			if (obj instanceof SimuLizarConfiguration) {
				SimuLizarConfiguration config = (SimuLizarConfiguration) obj;
				config.setDatasource(ds);
			} else if (obj instanceof SimuComConfiguration) {
				SimuComConfiguration config = (SimuComConfiguration) obj;
				config.setDatasource(ds);
			}
		});
	}

	private String buildCommand(File expFile) {
		// build parts of command
		String commandJava = javaPath + " " + String.join(" ", STATIC_ARGS);
		String commandEclipse = "-classpath \"" + eclipsePath + EQUINOX_OFFSET + "\" " + EQUINOX_MAIN;
		commandEclipse += " -application " + PCM_APPLICATION;

		String commandExperiments = ECL_APP_STATIC + expFile.getAbsolutePath();

		// full command
		return commandJava + " " + commandEclipse + " " + commandExperiments;
	}

	private boolean executeCommandBlocking(String command) {
		ProcessBuilder builder = new ProcessBuilder(splitCommand(command));
		builder.redirectOutput(Redirect.INHERIT);

		try {
			builder.start().waitFor();
			return true;
		} catch (InterruptedException | IOException e) {
			return false;
		}
	}

	private String[] splitCommand(String cmd) {
		return Commandline.translateCommandline(cmd);
	}

	private <T> T readFromFile(String path, Class<T> clazz) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		URI filePathUri = org.eclipse.emf.common.util.URI.createFileURI(path);

		Resource resource = resourceSet.getResource(filePathUri, true);
		return clazz.cast(resource.getContents().get(0));
	}

	private void saveToFile(EObject model, String path) {
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

}
