package ontology;

import jade.content.Concept;
import jade.core.AID;

public class ComputingAgent implements Concept {
	private String 	_name;
	private AID     _AID;
	
	// Methods required to use this class to represent the ADDRESS role
	public void setName(String name) {
		_name=name;
	}
	public String getName() {
		return _name;
	}
	public void setAID(AID AID) {
		_AID=AID;
	}
	public AID getAID() {
		return _AID;
	}

}
