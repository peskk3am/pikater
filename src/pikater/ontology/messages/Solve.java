package pikater.ontology.messages;

import jade.content.Concept;

public class Solve implements Concept {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1749701979992449877L;
	private Problem _problem;

	public void setProblem(Problem problem) {
		_problem = problem;
	}

	public Problem getProblem() {
		return _problem;
	}
}
