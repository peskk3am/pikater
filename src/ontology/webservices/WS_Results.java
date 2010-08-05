package ontology.webservices;

import jade.content.Concept;
import jade.util.leap.List;

public class WS_Results implements Concept {
	
	private static final long serialVersionUID = 1439064992523900962L;
	
	private String options;
	private float errorRate;
	private float pctIncorrect;
	
	public String getOptions() {
		return options;
	}
	public void setOptions(String options) {
		this.options = options;
	}
	public float getErrorRate() {
		return errorRate;
	}
	public void setErrorRate(float errorRate) {
		this.errorRate = errorRate;
	}
	public float getPctIncorrect() {
		return pctIncorrect;
	}
	public void setPctIncorrect(float pctIncorrect) {
		this.pctIncorrect = pctIncorrect;
	}

}
