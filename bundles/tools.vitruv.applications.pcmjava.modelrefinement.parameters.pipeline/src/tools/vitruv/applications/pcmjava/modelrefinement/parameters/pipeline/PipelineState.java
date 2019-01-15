package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline;

public enum PipelineState {
	STARTED("STARTED"), FINISHED("FINISHED");

	private String value;

	private PipelineState(String val) {
		this.value = val;
	}

	@Override
	public String toString() {
		return value;
	}
}
