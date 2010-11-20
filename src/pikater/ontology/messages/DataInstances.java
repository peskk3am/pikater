package pikater.ontology.messages;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import weka.core.FastVector;

public class DataInstances implements Concept {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4166896666680482675L;
	private List attributes;
	private List instances;
	private String name;
	private int class_index;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
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
	 * @param attributes
	 *            the attributes to set
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
	 * @param instaces
	 *            the instaces to set
	 */
	public void setInstances(List instances) {
		this.instances = instances;
	}

	// =============================

	public int getClass_index() {
		return class_index;
	}

	public void setClass_index(int classIndex) {
		class_index = classIndex;
	}

	public weka.core.Instances toWekaInstances() {
		// attributes
		FastVector wattrs = new FastVector();
		Iterator itr = attributes.iterator();
		while (itr.hasNext()) {
			Attribute attr = (Attribute) itr.next();
			wattrs.addElement(attr.toWekaAttribute());
		}
		// data instances
		weka.core.Instances winsts = new weka.core.Instances(name, wattrs,
				instances.size());
		itr = instances.iterator();

		while (itr.hasNext()) {
			Instance inst = (Instance) itr.next();
			Iterator itrval = inst.getValues().iterator();
			Iterator itrmis = inst.getMissing().iterator();
			double[] vals = new double[wattrs.size()];
			for (int i = 0; i < wattrs.size(); i++) {
				if ((Boolean) itrmis.next()) {
					vals[i] = weka.core.Instance.missingValue();
				} else {
					vals[i] = (Double) itrval.next();
				}
			}
			weka.core.Instance winst = new weka.core.Instance(1, vals);
			winst.setDataset(winsts);
			winsts.add(winst);
		}
		winsts.setClassIndex(this.class_index);
		return winsts;
	}

	public void fillWekaInstances(weka.core.Instances winsts) {
		// set name
		setName(winsts.relationName());
		// set attributes
		List onto_attrs = new ArrayList();
		for (int i = 0; i < winsts.numAttributes(); i++) {
			Attribute a = new Attribute();
			a.fillWekaAttribute(winsts.attribute(i));
			onto_attrs.add(a);
		}
		setAttributes(onto_attrs);

		// set instances
		List onto_insts = new ArrayList();
		for (int i = 0; i < winsts.numInstances(); i++) {
			Instance inst = new Instance();
			weka.core.Instance winst = winsts.instance(i);

			List instvalues = new ArrayList();
			List instmis = new ArrayList();
			for (int j = 0; j < winst.numValues(); j++) {
				if (winst.isMissing(j)) {
					instvalues.add(new Double(0.0));
					instmis.add(new Boolean(true));
				} else {
					instvalues.add(new Double(winst.value(j)));
					instmis.add(new Boolean(false));
				}
			}

			inst.setValues(instvalues);
			inst.setMissing(instmis);
			onto_insts.add(inst);
		}
		setInstances(onto_insts);
		setClass_index(winsts.classIndex());

	}

	/*
	 * returns all instances as a multi-line string
	 */
	@Override
	public String toString() {
		if (instances == null) {
			return "";
		}
		StringBuffer text = new StringBuffer();
		Iterator institr = instances.iterator();
		while (institr.hasNext()) {
			Instance inst = (Instance) institr.next();
			text.append(inst.toString(this));
			text.append('\n');
		}
		return text.toString();
	}

	/* returns a value in the table on the row and index */
	public String toString(int row, int index) {
		if (instances == null) {
			return "";
		}
		Instance inst = (Instance) instances.get(row);
		return inst.toString(this, index);
	}
	/*
	 * public void print(){ } System.out.println(name);
	 * System.out.println("Atributy:"); Iterator itr = attributes.iterator();
	 * while(itr.hasNext()){ Attribute attr = (Attribute)itr.next();
	 * attr.print(); System.out.println(); } System.out.println("Instance:");
	 * itr = instances.iterator(); while(itr.hasNext()){ Instance inst =
	 * (Instance)itr.next(); inst.print(); System.out.println(); } }
	 */
}
