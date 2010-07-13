package ontology.messages;

import weka.core.FastVector;
import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

public class Attribute implements Concept {
	private String name;
	private String type;//nominal/numeric/string
	private List values;
	private String date_format;
	/**
	 * @return the date_format
	 */
	public String getDate_format() {
		return date_format;
	}
	/**
	 * @param dataFormat the date_format to set
	 */
	public void setDate_format(String dateFormat) {
		date_format = dateFormat;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
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
	//=======================================
	public weka.core.Attribute toWekaAttribute(){
		if(values.size()>0){
			FastVector my_nominal_values = new FastVector();
			Iterator itr = values.iterator();
			while(itr.hasNext()){
				String val = (String)itr.next();
				my_nominal_values.addElement(val);
			}
			//co type???
			return new weka.core.Attribute(name, my_nominal_values);
		}else{
			if(type.equals("DATE")){
				return new weka.core.Attribute(name, date_format);
			}else if (type.equals("RELATIONAL")){
				//TODO: another instance
				weka.core.Instances winst= null;
				return new weka.core.Attribute(name, winst);
			}
			else
				return new weka.core.Attribute(name);
		}
	}
	public void fillWekaAttribute(weka.core.Attribute wattr){
		setName(wattr.name());
		switch(wattr.type()){
		case weka.core.Attribute.NUMERIC:
			setType("NUMERIC");
			break;
		case weka.core.Attribute.NOMINAL:
			setType("NOMINAL");
			break;
		case weka.core.Attribute.STRING:
			setType("STRING");
			break;
		case weka.core.Attribute.DATE:
			setType("DATE");
			setDate_format(wattr.getDateFormat());
			break;
		case weka.core.Attribute.RELATIONAL:
			setType("RELATIONAL");
			/*TODO: treating another table*/
			break;
		default:
			//TODO: error
		}
		List attr_values = new ArrayList();
		for(int j = 0; j < wattr.numValues(); j++){
			attr_values.add(wattr.value(j));
		}
		setValues(attr_values);
	}
	/*
	public void print() {
		// TODO Auto-generated method stub
		System.out.print(name+" "+type+" ");
		Iterator itr = values.iterator();
		while(itr.hasNext()){
			System.out.print((String)itr.next()+",");
		}
	}*/
}
