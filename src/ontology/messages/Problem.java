package ontology.messages;

import jade.content.Concept;
import jade.core.AID;
import jade.util.leap.List;

public class Problem implements Concept{
	
	private String _id;
	private AID _aid;
	private List  _agents;
	private List _data;
	private int _timeout;
	
	public void setAgents(List agents) {
		_agents=agents;
	}
	public List getAgents() {
		return _agents;
	}
	
	public void setData(List data) {
		_data=data;
	}
	public List getData() {
		return _data;
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
	public void setTimeout(int timeout) {
		_timeout = timeout;
	}
	public int getTimeout() {
		return _timeout;
	}
}
