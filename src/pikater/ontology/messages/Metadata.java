package pikater.ontology.messages;

import jade.content.Concept;

public class Metadata implements Concept {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4618372245480479979L;
	private String _internal_name;
	private String _external_name;
	private int _number_of_instances = -1;
	private int _number_of_attributes = -1;
	private boolean _missing_values;
	private String _default_task; // Classification, Regression, Clustering
	private String _attribute_type; // Categorical, Numerical, Mixed
	private int _number_of_tasks_in_db;

	public int getNumber_of_instances() {
		return _number_of_instances;
	}

	public void setNumber_of_instances(int _number_of_instances) {
		this._number_of_instances = _number_of_instances;
	}

	public int getNumber_of_attributes() {
		return _number_of_attributes;
	}

	public void setNumber_of_attributes(int _number_of_attributes) {
		this._number_of_attributes = _number_of_attributes;
	}

	public boolean getMissing_values() {
		return _missing_values;
	}

	public void setMissing_values(boolean _missing_values) {
		this._missing_values = _missing_values;
	}

	public String getDefault_task() {
		return _default_task;
	}

	public void setDefault_task(String _default_task) {
		this._default_task = _default_task;
	}

	public String getAttribute_type() {
		return _attribute_type;
	}

	public void setAttribute_type(String _attribute_type) {
		this._attribute_type = _attribute_type;
	}

	public void setInternal_name(String _internal_name) {
		this._internal_name = _internal_name;
	}

	public String getInternal_name() {
		return _internal_name;
	}

	public void setExternal_name(String _external_name) {
		this._external_name = _external_name;
	}

	public String getExternal_name() {
		return _external_name;
	}

	public void setNumber_of_tasks_in_db(int number_of_tasks_in_db) {
		this._number_of_tasks_in_db = number_of_tasks_in_db;
	}

	public int getNumber_of_tasks_in_db() {
		return _number_of_tasks_in_db;
	}

}
