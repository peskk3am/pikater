package ontology.messages;

import java.util.Date;

import weka.core.FastVector;
import weka.core.Utils;
import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

public class Attribute implements Concept {
	public final static String NUMERIC_TYPE = "NUMERIC";
	public final static String NOMINAL_TYPE = "NOMINAL";
	public final static String STRING_TYPE = "STRING";
	public final static String DATE_TYPE = "DATE";
	public final static String RELATIONAL_TYPE = "RELATIONAL";
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
		if(values!= null && values.size()>0){
			FastVector my_nominal_values = new FastVector();
			Iterator itr = values.iterator();
			while(itr.hasNext()){
				String val = (String)itr.next();
				my_nominal_values.addElement(val);
			}
			//co type???
			return new weka.core.Attribute(name, my_nominal_values);
		}else{
			if(type.equals(DATE_TYPE)){
				return new weka.core.Attribute(name, date_format);
			}else if (type.equals(RELATIONAL_TYPE)){
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
			setType(NUMERIC_TYPE);
			break;
		case weka.core.Attribute.NOMINAL:
			setType(NOMINAL_TYPE);
			break;
		case weka.core.Attribute.STRING:
			setType(STRING_TYPE);
			break;
		case weka.core.Attribute.DATE:
			setType(DATE_TYPE);
			setDate_format(wattr.getDateFormat());
			break;
		case weka.core.Attribute.RELATIONAL:
			setType(RELATIONAL_TYPE);
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
	
	String stringValue(double _dval){
		if(values!= null && values.size()>0){
			return Utils.quote((String)values.get((int)_dval));
		}else
		if(type.equals(NUMERIC_TYPE)){
			return Utils.doubleToString(_dval, 6);
		}else if(type.equals(DATE_TYPE)){
			Date d = new Date((long) _dval);
			return Utils.quote(d.toString());//TODO: normalized date format SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
		}
		//TODO: relational
		return null;
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
