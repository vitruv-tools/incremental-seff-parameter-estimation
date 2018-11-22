package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio;

import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.experimentautomation.application.config.ExperimentAutomationConfiguration;
import org.palladiosimulator.experimentautomation.application.jobs.RunExperimentAutomationJob;
import org.palladiosimulator.experimentautomation.experiments.Experiment;
import org.palladiosimulator.experimentautomation.experiments.ExperimentRepository;
import org.palladiosimulator.pcm.allocation.util.AllocationResourceFactoryImpl;
import org.palladiosimulator.pcm.core.composition.util.CompositionResourceFactoryImpl;
import org.palladiosimulator.pcm.core.entity.util.EntityResourceFactoryImpl;
import org.palladiosimulator.pcm.core.util.CoreResourceFactoryImpl;
import org.palladiosimulator.pcm.parameter.util.ParameterResourceFactoryImpl;
import org.palladiosimulator.pcm.protocol.util.ProtocolResourceFactoryImpl;
import org.palladiosimulator.pcm.qosannotations.qos_performance.util.QosPerformanceResourceFactoryImpl;
import org.palladiosimulator.pcm.qosannotations.qos_reliability.util.QosReliabilityResourceFactoryImpl;
import org.palladiosimulator.pcm.qosannotations.util.QosannotationsResourceFactoryImpl;
import org.palladiosimulator.pcm.reliability.util.ReliabilityResourceFactoryImpl;
import org.palladiosimulator.pcm.repository.util.RepositoryResourceFactoryImpl;
import org.palladiosimulator.pcm.resourceenvironment.util.ResourceenvironmentResourceFactoryImpl;
import org.palladiosimulator.pcm.resourcetype.util.ResourcetypeResourceFactoryImpl;
import org.palladiosimulator.pcm.seff.seff_performance.util.SeffPerformanceResourceFactoryImpl;
import org.palladiosimulator.pcm.seff.seff_reliability.util.SeffReliabilityResourceFactoryImpl;
import org.palladiosimulator.pcm.seff.util.SeffResourceFactoryImpl;
import org.palladiosimulator.pcm.subsystem.util.SubsystemResourceFactoryImpl;
import org.palladiosimulator.pcm.system.util.SystemResourceFactoryImpl;
import org.palladiosimulator.pcm.usagemodel.util.UsagemodelResourceFactoryImpl;
import org.palladiosimulator.pcm.util.PcmResourceFactoryImpl;

import de.uka.ipd.sdq.workflow.BlackboardBasedWorkflow;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.MDSDBlackboard;

public class HeadlessExecutor {

	public static void run(ExperimentRepository repository) {
		// load experiments
		final List<Experiment> experiments = repository.getExperiments();
		final ExperimentAutomationConfiguration experimentAutomationConfiguration = new ExperimentAutomationConfiguration();
		experimentAutomationConfiguration.setExperiments(experiments);
		experimentAutomationConfiguration.setLoadModels(false);
		experimentAutomationConfiguration.setAttributes(new HashMap<String, Object>());

		// run experiments via blackboard-based workflow
		final MDSDBlackboard blackboard = new MDSDBlackboard();
		final BlackboardBasedWorkflow<MDSDBlackboard> workflow = new BlackboardBasedWorkflow<MDSDBlackboard>(
				new RunExperimentAutomationJob(experimentAutomationConfiguration), blackboard);
		workflow.run();
	}

	public static void init() {
		// register factories:
		// http://wiki.eclipse.org/EMF/FAQ#How_do_I_use_EMF_in_standalone_applications_.28such_as_an_ordinary_main.29.3F
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("pcm", new PcmResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("core", new CoreResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("entity", new EntityResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("composition",
				new CompositionResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("usagemodel",
				new UsagemodelResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("repository",
				new RepositoryResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("resourcetype",
				new ResourcetypeResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("protocol",
				new ProtocolResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("parameter",
				new ParameterResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("reliability",
				new ReliabilityResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("seff", new SeffResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("seffperformance",
				new SeffPerformanceResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("seffreliability",
				new SeffReliabilityResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("qosannotations",
				new QosannotationsResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("qosperformance",
				new QosPerformanceResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("qosreliability",
				new QosReliabilityResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("system", new SystemResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("resourceenvironment",
				new ResourceenvironmentResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("allocation",
				new AllocationResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("subsystem",
				new SubsystemResourceFactoryImpl());
	}

}
