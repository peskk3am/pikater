package ontology.messages;
import jade.content.Concept;

public class Execute implements Concept{
	private Task _task;
	
	public void setTask(Task task) {
		_task=task;
	}
	public Task getTask() {
		return _task;
	}

}