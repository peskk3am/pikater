package pikater.ontology.webservices;

import jade.util.leap.List;

public class SetProblem implements jade.content.Concept {

	private List agentDescriptions;
	private List fileNames;

	private static final long serialVersionUID = 1L;

	public void setAgentDescriptions(List agentDescriptions) {
		this.agentDescriptions = agentDescriptions;
	}

	public List getAgentDescriptions() {
		return agentDescriptions;
	}

	public List getFileNames() {
		return fileNames;
	}

	public void setFileNames(List fileNames) {
		this.fileNames = fileNames;
	}
}
