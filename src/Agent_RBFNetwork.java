import java.io.*;

import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
//import weka.classifiers.trees.J48;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.RBFNetwork;
import weka.gui.visualize.PrintablePanel;


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



public class Agent_RBFNetwork extends Agent_WekaCA{
	 private RBFNetwork cls = new RBFNetwork(); 
	 
	 protected RBFNetwork getModelObject(){
		 return cls;
	 }

	 protected String getOptFileName(){
		 return "/options/RBFNetwork.opt";
	 }
	 
	 protected boolean setModelObject(Classifier _cls){
		 try {
			 cls = (RBFNetwork) _cls;
			 return true;
		 }
		 catch (Exception e){
			 	System.out.println(e);
			 	return false;
		}
	 }
	 
	 public String getAgentType(){
		 return "RBFNetwork";
	 }
	
	 protected void train() throws Exception{
		working = true;   
		System.out.println("Agent "+getLocalName()+": Training...");
		       

		cls = new RBFNetwork();

		if (OPTIONS.length > 0){
			cls.setOptions(OPTIONS);
		}
		
		cls.buildClassifier(train);
		
		state = states.TRAINED;  // change agent state
		
		OPTIONS = cls.getOptions();
		
		// write out net parameters
		System.out.println(getLocalName()+" "+getOptions());
		 
		working = false;
     }  // end train
     
     
	 protected Evaluation test(){
		 working = true;   
		 
		 System.out.println("Agent "+getLocalName()+": Testing...");
                
			// evaluate classifier and print some statistics
			Evaluation eval = null;
			try {
				eval = new Evaluation(train);
				eval.evaluateModel(cls, test);
				System.out.println(eval.toSummaryString(getLocalName()+" agent: "+"\nResults\n=======\n", false));
				
				// VisualizePanel();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 working = false;   
		 return eval;
	 }  // end test
	 
		    
}