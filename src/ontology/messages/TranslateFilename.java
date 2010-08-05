package ontology.messages;

import jade.content.onto.basic.Action;

public class TranslateFilename extends Action {

	private static final long serialVersionUID = 2577019954868509113L;
	
	private int userID;
	private String externalFilename;
	private String internalFilename;
	
	public String getInternalFilename() {
		return internalFilename;
	}

	public void setInternalFilename(String internalFilename) {
		this.internalFilename = internalFilename;
	}

	public int getUserID() {
		return userID;
	}
	
	public void setUserID(int userID) {
		this.userID = userID;
	}
	
	public String getExternalFilename() {
		return externalFilename;
	}
	
	public void setExternalFilename(String externalFilename) {
		this.externalFilename = externalFilename;
	}
	
	
	
}
