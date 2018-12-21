package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results.PalladioAnalysisResults;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.LocalFilesystemPCM;

public class DataBlackboard {
	private MonitoringDataSet monitoringData;

	private InMemoryPCM loadedPcm;
	private LocalFilesystemPCM filesystemPcm;

	private PalladioAnalysisResults analysisResults;

	public MonitoringDataSet getMonitoringData() {
		return monitoringData;
	}

	public synchronized void setMonitoringData(MonitoringDataSet monitoringData) {
		this.monitoringData = monitoringData;
	}

	public InMemoryPCM getLoadedPcm() {
		return loadedPcm;
	}

	public synchronized void setLoadedPcm(InMemoryPCM loadedPcm) {
		this.loadedPcm = loadedPcm;
	}

	public LocalFilesystemPCM getFilesystemPcm() {
		return filesystemPcm;
	}

	public synchronized void setFilesystemPcm(LocalFilesystemPCM filesystemPcm) {
		this.filesystemPcm = filesystemPcm;
	}

	public synchronized void persistInMemoryPCM() {
		getLoadedPcm().saveToFilesystem(getFilesystemPcm());
	}

	public PalladioAnalysisResults getAnalysisResults() {
		return analysisResults;
	}

	public synchronized void setAnalysisResults(PalladioAnalysisResults analysisResults) {
		this.analysisResults = analysisResults;
	}

}
