package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.util;

import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

public class IntDistribution {

	private Map<Integer, Long> distribution;

	public IntDistribution() {
		this.distribution = new HashMap<>();
	}

	public PCMRandomVariable toStochasticExpression() {
		if (distribution.size() == 1) {
			return buildIntLiteral(distribution.entrySet().stream().findFirst().get().getKey());
		}
		return null;
	}

	public void push(int iterations) {
		this.push(iterations, 1L);
	}

	public void push(int iterations, long value) {
		if (distribution.containsKey(iterations)) {
			distribution.put(iterations, distribution.get(iterations) + value);
		} else {
			distribution.put(iterations, value);
		}
	}

	public void push(IntDistribution iterations) {
		iterations.distribution.entrySet().forEach(it -> {
			this.push(it.getKey(), it.getValue());
		});
	}

	private PCMRandomVariable buildIntLiteral(int value) {
		PCMRandomVariable ret = CoreFactory.eINSTANCE.createPCMRandomVariable();
		ret.setSpecification(String.valueOf(value));
		return ret;
	}

}
