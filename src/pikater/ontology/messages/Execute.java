package pikater.ontology.messages;

import jade.content.onto.basic.Action;

public class Execute extends Action {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7170953913186078035L;
	private Task _task;

	public void setTask(Task task) {
		_task = task;
	}

	public Task getTask() {
		return _task;
	}

}