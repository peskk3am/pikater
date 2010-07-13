package ontology.messages;

import jade.content.Concept;

public class Evaluation implements Concept {
	private float _error_rate;
	private float _pct_incorrect;
	private DataInstances data_table;
	
	public void setError_rate(float error_rate) {
		_error_rate=error_rate;
	}
	public float getError_rate() {
		return _error_rate;
	}
	public void setPct_incorrect(float pct_incorrect) {
		_pct_incorrect=pct_incorrect;
	}
	public float getPct_incorrect() {
		return _pct_incorrect;
	}
	public DataInstances getData_table() {
		return data_table;
	}
	public void setData_table(DataInstances dataTable) {
		data_table = dataTable;
	}

}
