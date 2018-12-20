package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;

public class DataBlackboard {
	private MonitoringDataSet monitoringData;

	public MonitoringDataSet getMonitoringData() {
		return monitoringData;
	}

	public synchronized void setMonitoringData(MonitoringDataSet monitoringData) {
		this.monitoringData = monitoringData;
	}

}
