package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.iobserve;

import org.iobserve.model.correspondence.CorrespondenceModel;

public class CorrespondenceResult {
	private CorrespondenceModel model;
	private CorrespondenceMetadata metadata;

	public CorrespondenceResult(CorrespondenceModel model, CorrespondenceMetadata metadata) {
		super();
		this.model = model;
		this.metadata = metadata;
	}

	public CorrespondenceModel getModel() {
		return model;
	}

	public void setModel(CorrespondenceModel model) {
		this.model = model;
	}

	public CorrespondenceMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(CorrespondenceMetadata metadata) {
		this.metadata = metadata;
	}

}
