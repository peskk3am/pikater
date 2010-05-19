package ontology.messages;

import jade.content.Concept;
import jade.util.leap.List;

public class Agent implements Concept{
	private String _name;
	private List _options;
	

	// Methods required to use this class to represent the OPTIONS role
	public void setOptions(List options) {
		_options=options;
	}
	public List getOptions() {
		return _options;
	}	
	public void setName(String name) {
		_name=name;
	}
	public String getName() {
		return _name;
	}

}
