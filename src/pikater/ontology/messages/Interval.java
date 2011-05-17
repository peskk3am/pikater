package pikater.ontology.messages;

import jade.content.Concept;

public class Interval implements Concept {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1970673848789972695L;
	private Float _min;
	private Float _max;

	// Methods required to use this class to represent the TASK role
	public void setMin(Float min) {
		_min = min;
	}

	public Float getMin() {
		return _min;
	}

	public void setMax(Float max) {
		_max = max;
	}

	public Float getMax() {
		return _max;
	}

}
