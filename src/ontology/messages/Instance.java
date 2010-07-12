package ontology.messages;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

public class Instance implements Concept {
	private List values;

	/**
	 * @return the values
	 */
	public List getValues() {
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(List values) {
		this.values = values;
	}
	/*
	public void print() {
		Iterator itr = values.iterator();
		while(itr.hasNext()){
			System.out.print((Double)itr.next()+" ");
		}
	}*/
	
}
