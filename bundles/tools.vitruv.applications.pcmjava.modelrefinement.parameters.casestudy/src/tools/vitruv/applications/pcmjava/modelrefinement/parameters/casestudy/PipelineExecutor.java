package tools.vitruv.applications.pcmjava.modelrefinement.parameters.casestudy;

import java.io.File;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.ParameterEstimationPipeline;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.config.EPAPipelineConfiguration;

/**
 * Uses the CoCoME case study to evaluate the pipeline and the whole concept.
 * 
 * @author David Monschein
 *
 */
public class PipelineExecutor {

	public static void main(String[] args) {
		EPAPipelineConfiguration config = EPAPipelineConfiguration
				.fromFile(new File("casestudy-data/config/pipeline.config.json"));

		ParameterEstimationPipeline pipeline = new ParameterEstimationPipeline(config);
		pipeline.run();
	}

}
