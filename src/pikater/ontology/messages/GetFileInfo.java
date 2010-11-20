package pikater.ontology.messages;

import jade.content.onto.basic.Action;

public class GetFileInfo extends Action {

	private static final long serialVersionUID = -5936031580984331462L;

	private int userID;

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

}
