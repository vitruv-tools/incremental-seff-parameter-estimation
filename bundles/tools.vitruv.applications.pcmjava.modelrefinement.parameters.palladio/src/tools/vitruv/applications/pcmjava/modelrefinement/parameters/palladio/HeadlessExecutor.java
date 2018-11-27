package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.types.Commandline;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.edp2.dao.BinaryMeasurementsDao;
import org.palladiosimulator.edp2.dao.MeasurementsDaoFactory;
import org.palladiosimulator.edp2.dao.exception.DataNotAccessibleException;
import org.palladiosimulator.edp2.models.ExperimentData.DataSeries;
import org.palladiosimulator.edp2.models.ExperimentData.DoubleBinaryMeasurements;
import org.palladiosimulator.edp2.models.ExperimentData.ExperimentGroup;
import org.palladiosimulator.edp2.models.ExperimentData.LongBinaryMeasurements;
import org.palladiosimulator.edp2.models.ExperimentData.Measurement;
import org.palladiosimulator.edp2.models.ExperimentData.MeasurementRange;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringpointPackage;
import org.palladiosimulator.edp2.repository.local.dao.LocalDirectoryMeasurementsDaoFactory;
import org.palladiosimulator.experimentautomation.abstractsimulation.AbstractsimulationFactory;
import org.palladiosimulator.experimentautomation.abstractsimulation.FileDatasource;
import org.palladiosimulator.experimentautomation.application.tooladapter.simucom.model.SimuComConfiguration;
import org.palladiosimulator.experimentautomation.application.tooladapter.simulizar.model.SimuLizarConfiguration;
import org.palladiosimulator.experimentautomation.experiments.ExperimentRepository;
import org.palladiosimulator.pcmmeasuringpoint.PcmmeasuringpointPackage;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results.PalladioAnalysisResults;

public class HeadlessExecutor {
	private static final String[] STATIC_ARGS = new String[] { "-XstartOnFirstThread",
			"-Declipse.p2.data.area=@config.dir/p2", "-Declipse.pde.launch=true", "-Dfile.encoding=UTF-8" };

	// very static maybe exchange this with dynamic search of equinox
	private static final String EQUINOX_OFFSET = "plugins" + File.separator + "org.eclipse.equinox.launcher_1.4.0.v20161219-1356.jar";
	private static final String EQUINOX_MAIN = "org.eclipse.equinox.launcher.Main";
	private static final String PCM_APPLICATION = "org.palladiosimulator.experimentautomation.application";

	// OS DEPENDENT STRING -> BUILD THIS AUTOMATICALLY (windows: -os win32 -ws win32
	// -arch x86_64 ..
	//private static final String ECL_APP_STATIC = "-os macosx -ws cocoa -arch x86_64 -nl de_DE -consoleLog -clean ";
	private static final String ECL_APP_STATIC = "-os win32 -ws win32 -arch x86_64 -nl de_DE -consoleLog -clean ";

	private String javaPath;
	private String eclipsePath;

	public HeadlessExecutor(String javaPath, String eclipsePath) {
		this.javaPath = javaPath;
		this.eclipsePath = eclipsePath;
	}

	public PalladioAnalysisResults run(ExperimentRepository repository) throws IOException {
		// init results
		PalladioAnalysisResults results = null;

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
			results = parseResults(directory);
		}

		// finally delete temp files
		temp.delete();
		FileUtils.deleteDirectory(directory.toFile());

		// return the results of the analysis
		return results;
	}

	private PalladioAnalysisResults parseResults(Path directory) {
		PalladioAnalysisResults results = new PalladioAnalysisResults();

		MeasuringpointPackage.eINSTANCE.eClass();
		PcmmeasuringpointPackage.eINSTANCE.eClass();

		File[] dict = directory.toFile().listFiles(file -> FilenameUtils.getExtension(file.getName()).equals("edp2"));
		if (dict.length == 1) {
			boolean repoOpened = true;

			ExperimentGroup group = readFromFile(dict[0].getAbsolutePath(), ExperimentGroup.class);
			if (repoOpened) {
				loadMeasurements(group.getExperimentSettings().get(0).getExperimentRuns().get(0).getMeasurement(),
						results, trimURI(directory));
			}
		}

		return results;
	}

	private void loadMeasurements(List<Measurement> measurements, PalladioAnalysisResults result, URI directoryURI) {
		MeasurementsDaoFactory fact = LocalDirectoryMeasurementsDaoFactory.getRegisteredFactory(directoryURI);
		if (fact == null) {
			fact = new LocalDirectoryMeasurementsDaoFactory(directoryURI);
		}

		for (Measurement measurement : measurements) {
			MeasuringPoint belongingPoint = measurement.getMeasuringType().getMeasuringPoint();

			for (MeasurementRange range : measurement.getMeasurementRanges()) {
				for (DataSeries series : range.getRawMeasurements().getDataSeries()) {
					if (series instanceof LongBinaryMeasurements) {
						result.addLongs(belongingPoint, getLongMeasures(fact, series.getValuesUuid()));
					} else if (series instanceof DoubleBinaryMeasurements) {
						result.addDoubles(belongingPoint, getDoubleMeasures(fact, series.getValuesUuid()));
					}
				}
			}
		}
	}

	private List<Measure<Double, Duration>> getDoubleMeasures(MeasurementsDaoFactory fact, String uuid) {
		BinaryMeasurementsDao<Double, Duration> dao = fact.createDoubleMeasurementsDao(uuid, SI.SECOND);
		List<Measure<Double, Duration>> ret = null;
		try {
			dao.open();
			ret = new ArrayList<>(dao.getMeasurements());
			dao.close();
		} catch (DataNotAccessibleException e) {
			// TODO log maybe
		}
		return ret;
	}

	private List<Measure<Long, Duration>> getLongMeasures(MeasurementsDaoFactory fact, String uuid) {
		BinaryMeasurementsDao<Long, Duration> dao = fact.createLongMeasurementsDao(uuid, SI.SECOND);
		List<Measure<Long, Duration>> ret = null;
		try {
			dao.open();
			ret = new ArrayList<>(dao.getMeasurements());
			dao.close();
		} catch (DataNotAccessibleException e) {
			// TODO log maybe
		}
		return ret;
	}

	private URI trimURI(Path dict) {
		URI directoryURI = URI.createURI(dict.toUri().toString());
		if (directoryURI.hasTrailingPathSeparator()) {
			return directoryURI.trimSegments(1);
		}
		return directoryURI;
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
