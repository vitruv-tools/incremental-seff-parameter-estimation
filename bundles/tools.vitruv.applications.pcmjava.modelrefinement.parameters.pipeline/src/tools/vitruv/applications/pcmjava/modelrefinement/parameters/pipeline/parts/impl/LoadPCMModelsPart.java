package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.impl;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.InMemoryPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.data.LocalFilesystemPCM;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.parts.AbstractPipelinePart;

public class LoadPCMModelsPart extends AbstractPipelinePart {

	private LocalFilesystemPCM pcm;

	public LoadPCMModelsPart(LocalFilesystemPCM pcm) {
		super(true);
		this.pcm = pcm;
	}

	@Override
	protected void execute() {
		logger.info("Loading PCM models from file.");

		getBlackboard().setFilesystemPcm(pcm);
		getBlackboard().setLoadedPcm(InMemoryPCM.createFromFilesystem(pcm));
	}

}
