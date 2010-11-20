package pikater.ontology.messages;

import jade.content.onto.basic.Action;

public class SaveMetadata extends Action {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8885019280601751665L;
	private Metadata _metadata;

	public Metadata getMetadata() {
		return _metadata;
	}

	public void setMetadata(Metadata metadata) {
		_metadata = metadata;
	}
}