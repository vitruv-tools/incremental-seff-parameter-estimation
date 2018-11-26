package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.test;

import java.io.File;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.servicelevelobjective.ServiceLevelObjectiveRepository;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class CocomeExample {
	public static UsageModel usage = PcmUtils
			.loadUsageModel(new File("cocome_pcm/cocome.usagemodel").getAbsolutePath());
	public static Allocation allocation = PcmUtils
			.loadAllocationModel(new File("cocome_pcm/cocome.allocation").getAbsolutePath());
	public static Repository repo = PcmUtils.loadModel(new File("cocome_pcm/cocome.repository").getAbsolutePath());
	public static org.palladiosimulator.pcm.system.System sys = PcmUtils.readFromFile(
			new File("cocome_pcm/cocome.system").getAbsolutePath(), org.palladiosimulator.pcm.system.System.class);
	public static ResourceEnvironment env = PcmUtils.readFromFile(
			new File("cocome_pcm/cocome.resourceenvironment").getAbsolutePath(), ResourceEnvironment.class);
	public static ServiceLevelObjectiveRepository slo_repo = PcmUtils
			.readFromFile(new File("cocome_pcm/cocome.slo").getAbsolutePath(), ServiceLevelObjectiveRepository.class);
}
