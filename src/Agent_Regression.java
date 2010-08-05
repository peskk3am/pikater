import jade.util.leap.ArrayList;
import jade.util.leap.List;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import ontology.messages.DataInstances;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.RBFNetwork;
import weka.core.Instances;


public class Agent_Regression extends Agent_ComputingAgent {
	Vector<Double> coefs = new Vector<Double>();
	@Override
	public String getAgentType() {
		return "Regression";
	}

	@Override
	protected void getParameters() {
		//empty set of parameters
		agent_options = new ontology.messages.Agent();
		agent_options.setName(getLocalName());
		List _options = new ArrayList();
		//parameters...

		agent_options.setOptions(_options);
	}

	@Override
	public boolean loadAgent(String agentName) {
		try{
			// deserialize model + header
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream("saved/"+agentName+".model"));
			Vector v = (Vector) ois.readObject();

			Instances header = (Instances) v.get(0);  // TODO this is not used so far
			// System.out.println(agentName+" Header: "+header); 
			trainFileName = (String) v.get(1);
			testFileName = (String) v.get(2);
			state = (states) v.get(3);
			coefs = (Vector)v.get(4);

			ois.close();		 

			System.out.println("Loading... : Description: "+this.toString());
			System.out.println("                          trainFileName: "+trainFileName);
			System.out.println("                          testFileName: "+testFileName);
			System.out.println("                          state: "+state);


			// re-register with DF
			// TODO what if it fails?
			// deregisterWithDF();
			// registerWithDF();

			return true;
		}
		catch (Exception e){
			System.out.println(e);
			return false;
		}
	}

	@Override
	public boolean saveAgent() {
		try{
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream("saved/"+getLocalName()+".model"));

			// save model + header
			Vector v = new Vector();
			v.add(new Instances(data, 0));
			v.add(trainFileName);
			v.add(testFileName);
			v.add(state);
			v.add(coefs);

			oos.writeObject(v);
			oos.flush();
			oos.close();
			System.out.println("Saving... : Description:"+this.toString());
			return true;

		}
		catch (Exception e){
			System.out.println(e);
			return false;
		}
	}
	/*Create linear model of data insts (least squares)
	 * */
	private void buildModel(Instances insts) throws Exception{
		if(insts.numAttributes() != 2){
			throw new Exception("Wrong dimensionality of data for regression");
		}
		double x;
		double y;
		double sx = 0;
		double sy = 0;
		double sxy = 0;
		double sx2 = 0;
		final int n = insts.numInstances();

		for(int i = 0; i < n; i++){
			x = insts.instance(i).value(0);
			y = insts.instance(i).value(1);
			sx += x;
			sy += y;
			sxy += x*y;
			sx2 += x*x;
		}
		double denom = sx2*n - sx*sx;
		double a = (sxy*n - sx*sy) / denom;
		double b = (sx2*sy - sx*sxy) / denom;
		coefs.add(b);//zero order
		coefs.add(a);//first order
	}
	/*Computes model error (MSE) of the model on insts
	 */
	private float modelError(Instances insts) throws Exception{
		if(coefs.size()==0){
			throw new Exception("Model not trained");
		}
		double e = 0;
		for(int i = 0; i < insts.numInstances(); i++){
			double x = insts.instance(i).value(0);
			double y = insts.instance(i).value(1);
			//e=y-(ax+b)
			double e1 = y - coefs.get(0) - x*coefs.get(1);
			e += e1*e1;
		}
		return (float) Math.sqrt(e/insts.numInstances());
	}

	@Override
	protected ontology.messages.Evaluation evaluateCA() {
		working = true;   

		System.out.println("Agent "+getLocalName()+": Testing...");
		ontology.messages.Evaluation result = new ontology.messages.Evaluation();       
		// evaluate classifier and print some statistics
		try{
			result.setError_rate(modelError(test));
			/*eval = new Evaluation(train);
				eval.evaluateModel(cls, test);
				System.out.println(eval.toSummaryString(getLocalName()+" agent: "+"\nResults\n=======\n", false));
			 */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		working = false;   
		return result;
	}


	@Override
	protected void train() throws Exception{
		working = true;   
		System.out.println("Agent "+getLocalName()+": Training...");


		coefs.clear();
		if (OPTIONS.length > 0){
			//set options, should be empty!
			throw new Exception("Too much options");
		}

		buildModel(train);

		state = states.TRAINED;  // change agent state

		//changing options OPTIONS = cls.getOptions();

		// write out net parameters
		System.out.println(getLocalName()+" "+getOptions());

		working = false;
	}

	@Override
	protected DataInstances getPredictions(Instances test,
			DataInstances onto_test) {
		// TODO Auto-generated method stub
		return null;
	}

}
