package pikater.ontology.messages;

import jade.content.Predicate;
import jade.util.leap.List;

public class PartialResults implements Predicate {
	/**
	 * 
	 */
	private static final long serialVersionUID = -707946881612003753L;
	// sent only with the first results
	private Task task;
	private String task_id;
	private List results;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getTask_id() {
		return task_id;
	}

	public void setTask_id(String taskId) {
		task_id = taskId;
	}

	public List getResults() {
		return results;
	}

	public void setResults(List results) {
		this.results = results;
	}
}