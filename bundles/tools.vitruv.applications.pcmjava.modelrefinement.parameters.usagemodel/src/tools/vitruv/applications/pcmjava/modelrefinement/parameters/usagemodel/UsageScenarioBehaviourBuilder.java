package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel;

import java.util.List;

import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage.AbstractUsageElement;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;

public class UsageScenarioBehaviourBuilder {

	private Repository repository;
	private MonitoringDataMapping mapping;

	public UsageScenarioBehaviourBuilder(Repository repository, MonitoringDataMapping mapping) {
		this.repository = repository;
		this.mapping = mapping;
	}

	public ScenarioBehaviour buildBehaviour(List<AbstractUsageElement> structure) {
		ScenarioBehaviour back = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();

		Start start = UsagemodelFactory.eINSTANCE.createStart();
		AbstractUserAction current = start;
		back.getActions_ScenarioBehaviour().add(start);

		for (AbstractUsageElement element : structure) {
			AbstractUserAction nAction = element.toUserAction(repository, mapping);
			back.getActions_ScenarioBehaviour().add(nAction);

			current.setSuccessor(nAction);
			nAction.setPredecessor(current);
			current = nAction;
		}

		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		back.getActions_ScenarioBehaviour().add(stop);
		stop.setPredecessor(current);
		current.setSuccessor(stop);

		return back;
	}

}
