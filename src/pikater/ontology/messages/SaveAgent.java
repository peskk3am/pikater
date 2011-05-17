package pikater.ontology.messages;

import jade.content.onto.basic.Action;

public class SaveAgent extends Action {

	private static final long serialVersionUID = -2890249253440084L;

	private int userID = 1;
	private Agent agent;
	private byte [] object;
	
	public Agent getAgent() {
		return agent;
	}
	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	
	public void setObject(byte [] object) {
		this.object = object;
	}
	public byte [] getObject() {
		return object;
	}

}