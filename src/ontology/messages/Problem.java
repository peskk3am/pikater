package ontology.messages;

import jade.content.Concept;
import jade.core.AID;
import jade.util.leap.List;

public class Problem implements Concept{
	
	private String _id;
	private AID _aid;
	private List  _agents;
	private List _file_names;
	
	public void setAgents(List agents) {
		_agents=agents;
	}
	public List getAgents() {
		return _agents;
	}
	
	public void setFile_names(List file_names) {
		_file_names=file_names;
	}
	public List getFile_names() {
		return _file_names;
	}	
	public void setId(String id) {
		_id=id;
	}
	public String getId() {
		return _id;
	}	
	public void setAid(AID aid) {
		_aid=aid;
	}
	public AID getAid() {
		return _aid;
	}

}
