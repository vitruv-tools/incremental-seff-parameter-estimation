package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel;

import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.session.UsageSession;

public class UsageModelExtractor {

	private UsageModel model;
	private Map<String, UsageSession> sessions;
	private MonitoringDataMapping mapping;

	public UsageModelExtractor(UsageModel initial, MonitoringDataMapping mapping) {
		this.model = initial;
		this.sessions = new HashMap<>();
		this.mapping = mapping;
	}

	public void update(Repository repo, MonitoringDataSet monitoringData) {
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
			sess.getValue().compress();
		});
	}

	public void set(UsageModel current) {
		this.model = current;
	}

	public UsageModel get() {
		return this.model;
	}

}
