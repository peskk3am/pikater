package ontology.messages;

import jade.content.Concept;

public class Solve implements Concept{
	private Problem _problem;
	
	public void setProblem(Problem problem) {
		_problem=problem;
	}
	public Problem getProblem() {
		return _problem;
	}
}
