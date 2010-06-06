import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;

import ontology.messages.Option;

import weka.classifiers.Evaluation;
import weka.core.Instances;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.util.leap.Iterator;


public class Agent_Random extends Agent_OptionsManager {

	
	 protected String getAgentType(){
		 return "Random";
	 }
	
	 
	 protected boolean finished(){
		 if (result != null) {
			 if (result.errorRate < 0.3 ){
				 return true;
			 }
		 }
		 return false;
	 }
	 
	 
	 
	 protected String generateNewOptions(MyWekaEvaluation result){
		 // go through the Options Vector, find mutable options, generate random values, make it a string
		 
		 String str = "";
		 
		 Random generator = new Random();
		    
		 Iterator itr = Options.iterator();	 
		 
		 while (itr.hasNext()) {
	           Option next = (Option) itr.next();
	           
	    	   if (next.getMutable()){
	    		   
	    		   String optionName = " -"+next.getName()+" ";
	    		   
	    		   
	    		   int numArgs = (int)(next.getNumber_of_args().getMin()+generator.nextInt((int)(next.getNumber_of_args().getMax()-next.getNumber_of_args().getMin()+1)));
	    		   
	    		   if (!next.getIs_a_set()){	    		   
		    		   if(next.getData_type().equals("INT")){
		    		   		
		    		   			String si = "";
		    		   			for (int i=1; i<numArgs; i++){
		 	    					int rInt = (int) (next.getRange().getMin() + generator.nextInt((int)(next.getRange().getMax()-next.getRange().getMin())));
		 	    					si += Integer.toString(rInt)+",";
		    		   			}
		    		   			int rInt = (int) (next.getRange().getMin() + generator.nextInt((int)(next.getRange().getMax()-next.getRange().getMin())));
		    		   			si += Integer.toString(rInt);
		    		   			 
		    		   			str += (optionName+si);
		    		   			next.setValue(si);
		    		   }	
		    		   if(next.getData_type().equals("FLOAT")){
		    		   			String sf = "";
		    		   			for (int i=1; i<numArgs; i++){
		    		   				float rFloat = next.getRange().getMin() + (float)(generator.nextDouble())*(next.getRange().getMax() - next.getRange().getMin());
		 	    					sf += Float.toString(rFloat)+",";
		    		   			}
		    		   			float rFloat = next.getRange().getMin() + (float)(generator.nextDouble())*(next.getRange().getMax() - next.getRange().getMin());
		    		   			sf += Float.toString(rFloat);
		    		   			 
		    		   			str += (optionName+sf);
		    		   			next.setValue(sf);
		    		  }
		    		  if(next.getData_type().equals("BOOLEAN")){
		    		   			int rInt2 = generator.nextInt(2);
		    		   			if (rInt2 == 1){
		    		   				str += optionName;
		    		   				next.setValue("True");
		    		   			}
		    		   			else{
		    		   				next.setValue("False");
		    		   			}
		    		  }  
	    		   }
	    		   else{
	    			   	    			   
	    			   String s = "";
	    			   for (int i=1; i<numArgs; i++){
	    				   int index = generator.nextInt(next.getSet().size());
	    				   s += next.getSet().get(index)+",";	    		   
	    			   }
	    			   int index = generator.nextInt(next.getSet().size());
    				   s += next.getSet().get(index);
    				   
    				   str += (optionName+s);
	    		   }
	    	   }
	           
	       }
		 		 	 		 
		 return str;
	 }
	 

}
