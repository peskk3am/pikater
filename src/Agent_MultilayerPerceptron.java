import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import weka.core.Instances;
import weka.classifiers.Evaluation;
//import weka.classifiers.trees.J48;
import weka.classifiers.functions.MultilayerPerceptron;


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
	 protected String getAgentType(){
		 return "MultilayerPerceptron";
	 }

	 protected double proceed(){
		 double result = 100;
		 working = true;   
		 
		 System.out.println("Agent "+getLocalName()+": Proceeding...");
		      
			Instances train = data;
			Instances test = data;
	                 
			// train classifier
			MultilayerPerceptron cls = new MultilayerPerceptron();
			try {
				if (OPTIONS.length > 0){
					cls.setOptions(OPTIONS);
				}
				
				cls.buildClassifier(train);
				OPTIONS = cls.getOptions();
				
				// write out net parameters
				System.out.print(getLocalName());
				for (String s : OPTIONS) {
					System.out.print(s+" ");
				}
				System.out.println();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
		    
		    working = false;		    
		    return result;	
		    
	} // end proceed
}