package tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.PipelineState;

@RestController
public class RestInterface {

	private PipelineState currentState;
	private RestPipeline pipeline;

	@GetMapping("/state")
	public String getState() {
		return currentState.toString();
	}

	@GetMapping("/start")
	public String startPipeline() {
		if (pipeline != null) {
			pipeline.run();
		}
		return "";
	}

	public void setCurrentState(PipelineState state) {
		this.currentState = state;
	}

	public void setPipeline(RestPipeline pipeline) {
		this.pipeline = pipeline;
	}

}
