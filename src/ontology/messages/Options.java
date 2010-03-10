package ontology.messages;

import jade.content.Concept;
import jade.util.leap.List;

public class Options implements Concept{
	private List _options;

	// Methods required to use this class to represent the OPTIONS role
	public void setOptions(List options) {
		_options=options;
	}
	public List getOptions() {
		return _options;
	}	
}
