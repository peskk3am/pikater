import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Option;

import java.util.Random;

public class Agent_MultilayerPerceptron extends Agent_WekaCA {
	private MultilayerPerceptron cls = new MultilayerPerceptron();
	
	
	 public String getAgentType(){
		 return "MultilayerPerceptron";
	 }

	 protected String getOptFileName(){
		 return "/options/MultilayerPerceptron.opt";
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
		 
	 
	 protected void train() throws Exception{
		working = true;   
		System.out.println("Agent "+getLocalName()+": Training... ");
		       
		
		if (OPTIONS.length > 0){
			cls.setOptions(OPTIONS);
		}
		cls.setAutoBuild(true);
		cls.buildClassifier(train);
		
		state = states.TRAINED;  // change agent state
		
		OPTIONS = cls.getOptions(); 
		
		// write out net parameters
		System.out.println(getLocalName()+" "+getOptions());
		 
		working = false;
     }  // end train
     
     
	 protected Evaluation test(){
		 working = true;   
		 
		 // double result = 100;
		 Evaluation result = null;
		 System.out.println("Agent "+getLocalName()+": Testing...");
                
			// evaluate classifier and print some statistics
			Evaluation eval;
			try {
				eval = new Evaluation(train);
				
				// void crossValidateModel(Classifier classifier, Instances data, int numFolds, java.util.Random random) 
				eval.crossValidateModel(cls, test, 10, new java.util.Random());
				// eval.evaluateModel(cls, test);
				System.out.println(eval.toSummaryString(getLocalName()+" agent: "+"\nResults\n=======\n", false));
				// result = eval.errorRate();
				result = eval;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 working = false;   
		 return result;
	 }  // end test
	 
}