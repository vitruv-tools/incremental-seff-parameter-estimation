package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel;

import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;

public class BehaviourAdjuster {

	private UsageModel current;

	public BehaviourAdjuster(UsageModel initial) {
		this.current = initial;
	}

	public void update(MonitoringDataSet data) {
	}

	public UsageModel getCurrentModel() {
		return current;
	}

}
