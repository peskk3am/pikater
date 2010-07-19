package ontology.webservices;

import jade.util.leap.List;

public class GetResults implements jade.content.Concept {

	private static final long serialVersionUID = -6974771821464788543L;
	
	private List results;

	public List getResults() {
		return results;
	}

	public void setResults(List results) {
		this.results = results;
	}
	
}
