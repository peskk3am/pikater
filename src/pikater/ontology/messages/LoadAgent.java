package pikater.ontology.messages;

import jade.content.onto.basic.Action;

public class LoadAgent extends Action {

	private static final long serialVersionUID = -2890249253440084L;

	private String filename;
	private Execute first_action = null;
	private byte [] object;
	
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setFirst_action(Execute first_action) {
		this.first_action = first_action;
	}
	public Execute getFirst_action() {
		return first_action;
	}

	public void setObject(byte [] object) {
		this.object = object;
	}
	public byte [] getObject() {
		return object;
	}
	
}
