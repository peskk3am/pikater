package ontology.messages;

import jade.content.Concept;

public class Compute implements Concept {
	private Computation _computation;

	
	// Methods required to use this class to represent the TASK role
	public void setComputation(Computation computation) {
		_computation=computation;
	}
	public Computation getComputation() {
		return _computation;
	}
	
}
