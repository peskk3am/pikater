package pikater.ontology.messages;

import jade.content.onto.basic.Action;

public class LoadAgent extends Action {

	private static final long serialVersionUID = -2890249253440084L;

	private String filename;
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
}
