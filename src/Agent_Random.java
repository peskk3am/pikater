import jade.util.leap.Iterator;

import java.util.Random;

import ontology.messages.Evaluation;
import ontology.messages.Option;


public class Agent_Random extends Agent_OptionsManager {
	private int number_of_tries = 0;
	
	 protected String getAgentType(){
		 return "Random";
	 }
	
	 
	 protected boolean finished(){
		 if (evaluation != null) {
			 if (evaluation.getError_rate() < error_rate || number_of_tries >= maximum_tries ){
				 return true;
			 }
		 }
		 return false;
	 }
	 
	 
	 
	 protected String generateNewOptions(Evaluation result){
		 // go through the Options Vector, find mutable options, generate random values, make it a string
		 
		 String str = "";
		 Random generator = new Random();		    
		 Iterator itr = Options.iterator();	 
		 while (itr.hasNext()) {
	           Option next = (Option) itr.next();
	           
	    	   if (next.getMutable()){
	    		   
	    		   String optionName = " -"+next.getName()+" ";
	    		   
	    		   // int numArgs = (int)(next.getNumber_of_args().getMin()
	    		   //	   +generator.nextInt((int)(next.getNumber_of_args().getMax()-next.getNumber_of_args().getMin()+1)));
	    		   
	    	    	String[] values = next.getValue().split(",");
	    	    	int numArgs = values.length;
	    		   
	    		   if (!next.getIs_a_set()){	    		   
		    		   if(next.getData_type().equals("INT")){
		    		   		
		    		   			String si = "";
		    		   			for (int i=1; i<numArgs; i++){
		 	    					int rInt = (int) (next.getRange().getMin() 
		 	    								+ generator.nextInt((int)(next.getRange().getMax()-next.getRange().getMin())));
		 	    					si += Integer.toString(rInt)+",";
		    		   			}
		    		   			int rInt = (int) (next.getRange().getMin() 
		    		   					+ generator.nextInt((int)(next.getRange().getMax()-next.getRange().getMin())));
		    		   			si += Integer.toString(rInt);
		    		   			 
		    		   			str += (optionName+si);
		    		   			next.setValue(si);
		    		   }	
		    		   if(next.getData_type().equals("FLOAT")){
		    		   			String sf = "";
		    		   			for (int i=1; i<numArgs; i++){
		    		   				float rFloat = next.getRange().getMin() + (float)(generator.nextDouble())
		    		   						*(next.getRange().getMax() - next.getRange().getMin());
		 	    					sf += Float.toString(rFloat)+",";
		    		   			}
		    		   			float rFloat = next.getRange().getMin() + (float)(generator.nextDouble())
		    		   					*(next.getRange().getMax() - next.getRange().getMin());
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
	    				   if (values[i-1].equals("?")){
	    					   int index = generator.nextInt(next.getSet().size());
	    					   s += next.getSet().get(index)+",";
	    				   }
	    				   else{
	    					   s += values[i-1]+",";
	    				   }
	    					   
	    			   }
    				   if (values[numArgs-1].equals("?")){
    					   int index = generator.nextInt(next.getSet().size());
    					   s += next.getSet().get(index);
    				   }
    				   else{
    					   s += values[numArgs-1];
    				   }
    				   
    				   str += (optionName+s);
	    		   }
	    	   }	           
	       }
		 number_of_tries++;
		 return str;
	 }
}
