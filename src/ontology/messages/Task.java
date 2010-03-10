package ontology.messages;

import jade.content.Concept;
import jade.util.leap.List;

public class Task implements Concept {

	private String 	_agent_name;
	private List  _options;
	private String 	_data_file_name;

	
	// Methods required to use this class to represent the TASK role
	public void setAgent_name(String agent_name) {
		_agent_name=agent_name;
	}
	public String getAgent_name() {
		return _agent_name;
	}
	public void setOptions(List options) {
		_options=options;
	}
	public List getOptions() {
		return _options;
	}
	public void setData_file_name(String data_file_name) {
		_data_file_name=data_file_name;
	}
	public String getData_file_name() {
		return _data_file_name;
	}
	
	// Other application specific methods
}
