package ontology.messages;

import jade.content.Concept;

public class Metadata implements Concept{

	private int _number_of_instances;
	private int _number_of_attributes;
	private boolean _missing_values;
	private String _default_task;  // Classification, Regression, Clustering
	private String _attribute_type; // Categorical, Numerical, Mixed
	
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

}
