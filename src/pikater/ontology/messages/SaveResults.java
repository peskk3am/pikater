package pikater.ontology.messages;

import jade.content.onto.basic.Action;

public class SaveResults extends Action {

	private static final long serialVersionUID = -7028457864866356063L;
	private Task task;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

}
