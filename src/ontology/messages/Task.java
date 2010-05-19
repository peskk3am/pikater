package ontology.messages;

import jade.content.Concept;
import jade.util.leap.List;

public class Task implements Concept {
	
	private String _problem_id;
	private Agent _agent;
	private String 	_data_file_name;

	
	// Methods required to use this class to represent the TASK role
	public void setAgent(Agent agent) {
		_agent=agent;
	}
	public Agent getAgent() {
		return _agent;
	}
	public void setData_file_name(String data_file_name) {
		_data_file_name=data_file_name;
	}
	public String getData_file_name() {
		return _data_file_name;
	}
	public void setProblem_id(String problem_id) {
		_problem_id = problem_id;
	}
	public String getProblem_id() {
		return _problem_id;
	}

	
	// Other application specific methods
}
