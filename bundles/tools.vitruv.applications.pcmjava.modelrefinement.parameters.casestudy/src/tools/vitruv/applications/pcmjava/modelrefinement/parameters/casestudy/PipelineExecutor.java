package tools.vitruv.applications.pcmjava.modelrefinement.parameters.casestudy;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.RestApplication;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.RestInterface;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.iface.RestPipeline;
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

		ConfigurableApplicationContext ctx = SpringApplication.run(RestApplication.class, args);
		RestInterface iface = ctx.getBean(RestInterface.class);
		iface.setPipeline(new RestPipeline(iface, config));

	}

}
