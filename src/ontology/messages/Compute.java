package ontology.messages;

import jade.content.Concept;

public class Compute implements Concept {
	private Task _task;

	
	// Methods required to use this class to represent the TASK role
	public void setTask(Task task) {
		_task=task;
	}
	public Task getTask() {
		return _task;
	}
	
}
