package ontology.messages;

import weka.core.FastVector;
import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

public class DataInstances implements Concept {
	private List attributes;
	private List instances;
	private String name;
	
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
	 * @return the attributes
	 */
	public List getAttributes() {
		return attributes;
	}
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List attributes) {
		this.attributes = attributes;
	}
	/**
	 * @return the instaces
	 */
	public List getInstances() {
		return instances;
	}
	/**
	 * @param instaces the instaces to set
	 */
	public void setInstances(List instances) {
		this.instances = instances;
	}
	
	//=============================
	
	public weka.core.Instances toWekaInstances(){
		//attributes
		FastVector wattrs = new FastVector();
		Iterator itr = attributes.iterator();
		while (itr.hasNext()) {
			Attribute attr = (Attribute)itr.next();
			wattrs.addElement(attr.toWekaAttribute());
		}
		//data instances
		weka.core.Instances winsts = new weka.core.Instances(name, wattrs , instances.size());
		itr = instances.iterator();
		
		while (itr.hasNext()) {
			Instance inst = (Instance)itr.next();
			weka.core.Instance winst = new weka.core.Instance(wattrs.size());
			winst.setDataset(winsts);
			Iterator itr1 = inst.getValues().iterator();
			for(int i = 0; i < wattrs.size(); i++){
				winst.setValue((weka.core.Attribute)wattrs.elementAt(i), (Double)(itr1.next()));
			}
			winsts.add(winst);
		}
		return winsts;
	}
	
	public void fillWekaInstances(weka.core.Instances winsts){
		//set name
		setName(winsts.relationName());
		//set attributes
		List onto_attrs = new ArrayList();
		for(int i = 0; i < winsts.numAttributes(); i++){
			Attribute a = new Attribute();
			a.fillWekaAttribute(winsts.attribute(i));			
			onto_attrs.add(a);
		}
		setAttributes(onto_attrs);
		
		//set instances
		List onto_insts = new ArrayList();
		for(int i = 0; i < winsts.numInstances(); i++){
			Instance inst = new Instance();
			weka.core.Instance winst = winsts.instance(i);
			
			List instvalues = new ArrayList();
			for(int j = 0; j < winst.numValues(); j++){
				instvalues.add(new Double(winst.value(j)));
			}
			
			inst.setValues(instvalues);
			onto_insts.add(inst);
		}
		setInstances(onto_insts);
		
	}
	/*public void print(){
	System.out.println(name);
	System.out.println("Atributy:");
	Iterator itr = attributes.iterator();
	while(itr.hasNext()){
		Attribute attr = (Attribute)itr.next();
		attr.print();
		System.out.println();
	}
	System.out.println("Instance:");
	itr = instances.iterator();
	while(itr.hasNext()){
		Instance inst = (Instance)itr.next();
		inst.print();
		System.out.println();
	}
}*/
}
