package ontology.messages;

import jade.content.onto.basic.Action;

public class GetFiles extends Action {
	
	private static final long serialVersionUID = -2890249253440086934L;
	
	private int userID;

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}
	
	

}
