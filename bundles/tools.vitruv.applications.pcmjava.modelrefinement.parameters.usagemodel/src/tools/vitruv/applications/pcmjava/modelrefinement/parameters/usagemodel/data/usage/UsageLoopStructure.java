package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Loop;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.UsageScenarioBehaviourBuilder;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.util.IntDistribution;

public class UsageLoopStructure extends AbstractUsageElement {
	private List<AbstractUsageElement> childs;
	private IntDistribution iterations;

	public UsageLoopStructure(int iterations) {
		this.childs = new ArrayList<>();
		this.iterations = new IntDistribution();
		this.iterations.push(iterations);
	}

	public List<AbstractUsageElement> getChilds() {
		return childs;
	}

	public IntDistribution getIterations() {
		return this.iterations;
	}

	public void addChild(AbstractUsageElement child) {
		this.childs.add(child);
	}

	@Override
	public void merge(AbstractUsageElement other) {
		if (other instanceof UsageLoopStructure) {
			iterations.push(((UsageLoopStructure) other).getIterations());

			for (int i = 0; i < childs.size(); i++) {
				this.childs.get(i).merge(((UsageLoopStructure) other).getChilds().get(i));
			}
		}
	}

	@Override
	public AbstractUserAction toUserAction(Repository repo, MonitoringDataMapping mapping) {
		Loop loop = UsagemodelFactory.eINSTANCE.createLoop();
		loop.setLoopIteration_Loop(iterations.toStochasticExpression());

		UsageScenarioBehaviourBuilder innerBuilder = new UsageScenarioBehaviourBuilder(repo, mapping);
		loop.setBodyBehaviour_Loop(innerBuilder.buildBehaviour(childs));

		return loop;
	}
}
