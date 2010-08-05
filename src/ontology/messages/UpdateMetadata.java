package ontology.messages;

import jade.content.onto.basic.Action;

public class UpdateMetadata extends Action{
	
	private static final long serialVersionUID = 39194673393127712L;
	Metadata metadata;

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	

}
