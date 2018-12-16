package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage.AbstractUsageElement;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.session.UsageSession;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class UsageModelExtractor {

	private UsageModel model;
	private Map<String, UsageSession> sessions;
	private MonitoringDataMapping mapping;

	private UsageScenarioBehaviourBuilder builder;

	public UsageModelExtractor(UsageModel initial, MonitoringDataMapping mapping) {
		this.model = initial;
		this.sessions = new HashMap<>();
		this.mapping = mapping;
	}

	public void update(Repository repo, MonitoringDataSet monitoringData) {
		builder = new UsageScenarioBehaviourBuilder(repo, mapping);

		// collect service calls
		monitoringData.getServiceCalls().getServiceCalls().forEach(call -> {
			if (!sessions.containsKey(call.getSessionId())) {
				sessions.put(call.getSessionId(), new UsageSession());
			}
			sessions.get(call.getSessionId()).update(call);
		});

		// TODO cluster
		// build scenarios
		sessions.entrySet().forEach(sess -> {
			List<AbstractUsageElement> elements = sess.getValue().compress();
			ScenarioBehaviour behaviour = builder.buildBehaviour(elements);

			if (elements.size() >= 10) {
				PcmUtils.saveToFile(behaviour, "test.usagemodel");
			}
		});
	}

	public void set(UsageModel current) {
		this.model = current;
	}

	public UsageModel get() {
		return this.model;
	}

}
