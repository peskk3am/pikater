package ontology.webservices;

import ontology.messages.Problem;

public class SetProblem implements jade.content.Concept {

	private Problem prob;
	
	private static final long serialVersionUID = 1L;
	
	public void setTheProblem(Problem prob) {
		this.prob = prob;
	}

	public Problem getTheProblem() {
		return prob;
	}	
}
