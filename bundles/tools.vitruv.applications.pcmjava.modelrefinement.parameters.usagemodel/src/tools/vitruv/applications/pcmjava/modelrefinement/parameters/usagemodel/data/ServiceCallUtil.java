package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;

public class ServiceCallUtil {

	public static ServiceCall merge(ServiceCall a, ServiceCall b) {
		if (a.getServiceId().equals(b.getServiceId())) {
			return a;
		} else {
			System.out.println(a.getServiceId() + " <> " + b.getServiceId());
			throw new RuntimeException();
		}
	}

	public static PCMRandomVariable buildIntLiteral(int value) {
		PCMRandomVariable ret = CoreFactory.eINSTANCE.createPCMRandomVariable();
		ret.setSpecification(String.valueOf(value));
		return ret;
	}

}
