package pikater.ontology.webservices;

import jade.content.Concept;

public class GetOptions implements Concept {

	private static final long serialVersionUID = 2278335649591377336L;
	private String agentName;

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

}
