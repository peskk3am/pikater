package ontology.webservices;

import jade.content.Concept;
import jade.util.leap.List;

public class Result implements Concept {
	
	private static final long serialVersionUID = 1439064992523900962L;
	
	private List options;
	private float errorRate;
	private float pctCorrect;
	
	public List getOptions() {
		return options;
	}
	public void setOptions(List options) {
		this.options = options;
	}
	public float getErrorRate() {
		return errorRate;
	}
	public void setErrorRate(float errorRate) {
		this.errorRate = errorRate;
	}
	public float getPctCorrect() {
		return pctCorrect;
	}
	public void setPctCorrect(float pctCorrect) {
		this.pctCorrect = pctCorrect;
	}

}
