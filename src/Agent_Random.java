import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Option;
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
		 
		 for (Enumeration e = Options.elements() ; e.hasMoreElements() ;) {
	           MyWekaOption next = (MyWekaOption)e.nextElement();
	    	   if (next.mutable){
	    		   
	    		   String optionName = " -"+next.name+" ";
	    		   
	    		   int numArgs = next.numArgsMin+generator.nextInt(next.numArgsMax-next.numArgsMin+1);
	    		   
	    		   if (!next.isASet){	    		   
		    		   switch(next.type){
		    		   		case INT:
		    		   			String si = "";
		    		   			for (int i=1; i<numArgs; i++){
		 	    					int rInt = (int)next.lower + generator.nextInt((int)(next.upper-next.lower));
		 	    					si += Integer.toString(rInt)+",";
		    		   			}
		    		   			int rInt = (int)next.lower + generator.nextInt((int)(next.upper-next.lower));
		    		   			si += Integer.toString(rInt);
		    		   			 
		    		   			str += (optionName+si);
		    		   			break;
		    		   			
		    		   		case FLOAT:
		    		   			String sf = "";
		    		   			for (int i=1; i<numArgs; i++){
		    		   				float rFloat = next.lower + (float)(generator.nextDouble())*(next.upper - next.lower);
		 	    					sf += Float.toString(rFloat)+",";
		    		   			}
		    		   			float rFloat = next.lower + (float)(generator.nextDouble())*(next.upper - next.lower);
		    		   			sf += Float.toString(rFloat);
		    		   			 
		    		   			str += (optionName+sf);
		    		   			break;
		    		   			
		    		   		case BOOLEAN:
		    		   			int rInt2 = generator.nextInt(2);
		    		   			if (rInt2 == 1){
		    		   				str += optionName;
		    		   			}
		    		   			break;
		    		   			
		    		   }  // end switch
	    		   }
	    		   else{
	    			   	    			   
	    			   String s = "";
	    			   for (int i=1; i<numArgs; i++){
	    				   int index = generator.nextInt(next.set.length);
	    				   s += next.set[index]+",";	    		   
	    			   }
	    			   int index = generator.nextInt(next.set.length);
    				   s += next.set[index];
    				   
    				   str += (optionName+s);
	    		   }
	    	   }
	           
	       }
		 		 	 		 
		 return str;
	 }
	 

}
