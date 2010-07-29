package ontology.webservices;

import jade.util.leap.List;

public class GetAgents implements jade.content.Concept {

	private static final long serialVersionUID = 1099701794732531622L;
	private List agents;
	
	public void setAgents(List agents) {
		this.agents = agents;
	}
	
	public List getAgents() {
		return agents;
	}
}
