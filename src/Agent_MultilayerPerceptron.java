import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
//import weka.classifiers.trees.J48;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.RBFNetwork;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**

 */
public class Agent_MultilayerPerceptron extends Agent_ComputingAgent {
	private MultilayerPerceptron cls;
	
	 protected String getAgentType(){
		 return "MultilayerPerceptron";
	 }

	 protected boolean setModelObject(Classifier _cls){
		 try {
			 cls = (MultilayerPerceptron) _cls;
			 return true;
		 }
		 catch (Exception e){
			 	System.out.println(e);
			 	return false;
		}
	 }	 
	 
	 protected MultilayerPerceptron getModelObject(){
		 return cls;
	 }
	
	 
	 protected void train(){
		 working = true;   
		 System.out.println("Agent "+getLocalName()+": Training...");
		       
		 
		 cls = new MultilayerPerceptron();
		 try {
			if (OPTIONS.length > 0){
				cls.setOptions(OPTIONS);
			}
			
			cls.buildClassifier(train);
			
			state = states.TRAINED;  // change agent state
			
			OPTIONS = cls.getOptions();
			
			// write out net parameters
			System.out.print(getLocalName()+": ");

	 	 } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
		 
		 working = false;
     }  // end train
     
     
	 protected double test(){
		 working = true;   

		 
		 double result = 100;
		 System.out.println("Agent "+getLocalName()+": Testing...");
                
			// evaluate classifier and print some statistics
			Evaluation eval;
			try {
				eval = new Evaluation(train);
				eval.evaluateModel(cls, test);
				System.out.println(eval.toSummaryString(getLocalName()+" agent: "+"\nResults\n=======\n", false));
				result = eval.errorRate();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		 return result;
	 }  // end test
	 

}