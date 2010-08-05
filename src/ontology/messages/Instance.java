package ontology.messages;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

public class Instance implements Concept {
	private List values;//Double[]
	private List missing;//Boolean[]

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

	public List getMissing() {
		return missing;
	}

	public void setMissing(List missing) {
		this.missing = missing;
	}
	
	public void setPrediction(double v){
		values.remove(values.size()-1);
		values.add(v);
	}
	 //---------------------
	public String toString(DataInstances _insts){
		if(values == null)
			return "\n";
		StringBuffer text = new StringBuffer();
		Iterator itrval = values.iterator();
		Iterator itrmis = missing.iterator();
		Iterator itratt = _insts.getAttributes().iterator();
		int i = 0;
		while (itrval.hasNext()){
			boolean missing = (Boolean)itrmis.next();
			double value = (Double)itrval.next();
			Attribute attr = (Attribute)itratt.next();
			if(i > 0)
				text.append(',');
			if(missing){
				text.append('?');
			}else{
				text.append(attr.stringValue(value));
			}
			i++;
		}
		return text.toString();
	}
	/*index-th value of instance as a string*/
	public String toString(DataInstances _insts, int index){
		if(values == null)
			return "";
		boolean miss = (Boolean) missing.get(index);
		double value = (Double) values.get(index);
		Attribute attr = (Attribute) _insts.getAttributes().get(index);
		if(miss)
			return "?";
		else
			return attr.stringValue(value);
	}
}
