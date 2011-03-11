package pikater.ontology.messages;

import jade.content.Concept;

public class Method implements Concept {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9024769565945696142L;
	private String _name;
	private float _error_rate;
	private int _maximum_tries;

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public Float getError_rate() {
		return _error_rate;
	}

	public void setError_rate(Float error_rate) {
		_error_rate = error_rate;
	}

	public int getMaximum_tries() {
		return _maximum_tries;
	}

	public void setMaximum_tries(int maximum_tries) {
		_maximum_tries = maximum_tries;
	}
}
