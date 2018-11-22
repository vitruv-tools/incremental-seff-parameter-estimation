package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel;

import java.util.HashMap;
import java.util.Map;

public class CorrespondenceMetadata {
	private long methodId;
	private long classId;

	private Map<String, String> mappingOperation;
	private Map<String, String> mappingComponent;
	private Map<String, String> mapServiceComponent;

	public CorrespondenceMetadata() {
		this.methodId = 0;

		this.mappingOperation = new HashMap<>();
		this.mappingComponent = new HashMap<>();
		this.mapServiceComponent = new HashMap<>();
	}

	public String getOperation(String service) {
		return mappingOperation.get(service);
	}

	public String getComponent(String service) {
		return mappingComponent.get(service);
	}

	public String getComponentByService(String service) {
		return mapServiceComponent.get(service);
	}

	public void mapServiceComponent(String service, String component) {
		mapServiceComponent.put(service, component);
	}

	public String mapComponent(String comp) {
		if (isMappedComponent(comp)) {
			return getComponent(comp);
		}
		return nextClass(comp);
	}

	public String mapOperation(String service) {
		if (isMappedMethod(service)) {
			return mappingOperation.get(service);
		}
		return nextMethod(service);
	}

	public boolean isMappedMethod(String service) {
		return mappingOperation.containsKey(service);
	}

	public boolean isMappedComponent(String component) {
		return mappingComponent.containsKey(component);
	}

	private String nextMethod(String service) {
		mappingOperation.put(service, String.valueOf(methodId));
		return String.valueOf(methodId++);
	}

	private String nextClass(String component) {
		mappingComponent.put(component, String.valueOf(classId));
		return String.valueOf(classId++);
	}

}
