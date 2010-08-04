import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import ontology.messages.Option;
import ontology.messages.Problem;
import ontology.messages.Evaluation;

public class Agent_ChooseXValues extends Agent_OptionsManager {
	private int n = Integer.MAX_VALUE;
	private int ni = 0;
	
	private Vector<String> options_vector = new Vector<String>();
	private Vector<String> sub_options_vector = new Vector<String>();
	
	@Override
	protected boolean finished() {
		if (ni < n){
			return false;	
		}
		else {
			return true;
		}	
	}

	@Override
	protected String getAgentType() {
		return "ChooseXValues";
	}

	
	private String[] generateOptionValues(Option next){
    	Random generator = new Random();
    	String optionName = " -"+next.getName()+" ";
    	// number of values ~ number of "?"s set by user
    	System.out.println("user value:"+next.getUser_value());
    	String[] values = next.getUser_value().split(",");
    	int numArgs = values.length;
    	
    	if (!next.getIs_a_set()){	    		   
		   if(next.getData_type().equals("INT")){
			    int x = next.getNumber_of_values_to_try();
	   			int range = (int)(next.getRange().getMax() - next.getRange().getMin() + 1);
	   			// if there is less possibilities than x -> change x
	   			if (range < x){
	   				x = range;
	   			}	   			
	   			String[] a = new String[x];
	   			for (int i=0; i<x; i++){
		   			String si = "";
	   				for (int j=1; j<numArgs; j++){
		   				int vInt = (int) (next.getRange().getMin() + i * (range / x) );
    		   			si += Integer.toString(vInt)+",";
		   			}
		   			int vInt = (int) (next.getRange().getMin() + i * (range / x) );
		   			si += Integer.toString(vInt);
		   			 
		   			a[i] = optionName+si;
	   			}
	   			return a;
		   }	
		   if(next.getData_type().equals("FLOAT")){
			    int x = next.getNumber_of_values_to_try();
	   			float dv = (next.getRange().getMax() - next.getRange().getMin()) / (x-1);
	   			String[] a = new String[x];
	   			
	   			for (int i=0; i<x; i++){
		   			String sf = "";
		   			for (int j=1; j<numArgs; j++){
		   				float vFloat = next.getRange().getMin() + i * dv ;
    		   			sf += Float.toString(vFloat)+",";
		   			}
	   				float vFloat = next.getRange().getMin() + i * dv ;
		   			sf += Float.toString(vFloat);

		   			a[i] = optionName+sf;
		   		}
	   			return a;
		  }
		  if(next.getData_type().equals("BOOLEAN")){
			  return new String[] {optionName+" True", optionName+" False"};
		  }
	   }
	   else{		   
		   
		   //  int x = next.getNumber_of_values_to_try();
		   //  if (next.getSet().size() < x){
		   //	   x = next.getSet().size();
		   //  }
		   
		   // sub_generate(0, "-H ", next.getSet().toArray(), values );
		   System.out.println("velikost pole:"+next.getSet().toArray().length);
		   sub_generate(0, "-"+next.getName()+" ", next.getSet().toArray(), values );
		   
		   // copy vector to array:
		   String[] a = new String[sub_options_vector.size()];
		   int i = 0;
		   for (Enumeration e = sub_options_vector.elements() ; e.hasMoreElements() ;) {
		       String next_option = (String)e.nextElement();      
		       a[i] = next_option;
		       i++;
		   }
		   return a; 		   
	   }
		   
	   return new String[0];
	}
	
	@Override
	protected void generateNewOptions(Evaluation result) {
		if (n == Integer.MAX_VALUE){
			// generate the options_vector when called for the first time
			generateOptions_vector();
		}
		if (n == 0){
			return;
		}
		// return options_vector.get(ni++);
		// go through a string, add the values to the Options
		String[] optStringArray = options_vector.get(ni++).replaceFirst("[ ]+", "").split("[ ]+");
		// List newOpt = (new ontology.messages.Agent()).stringToOptions(optString);
				
		List newOptions = new ArrayList();
		Iterator itr = Options.iterator();	 
		while (itr.hasNext()) {
	        Option next = (Option) itr.next();
	        for (int i=0; i < optStringArray.length; i=i+2){
	        	// always a couple name - value
	        	if (optStringArray[i].equals("-"+next.getName())){	        			     	       
	        			next.setValue(optStringArray[i+1]);			        	
	        			newOptions.add(next);
			    }
			}
			if (!newOptions.contains(next)){
				if (!next.getValue().equals(next.getDefault_value())){
					newOptions.add(next);	        	
				}
			}			
		}
		Options = newOptions;
	}

	
	private String generate(String str, String[][] possible_options_array){
		if (possible_options_array.length < 1){
			options_vector.add(str);
			return str;
		}
		
		String[][] new_possible_options_array = new String[possible_options_array.length-1][];

		System.arraycopy(possible_options_array,1, 
				new_possible_options_array, 0, possible_options_array.length-1);
		for (int i=0; i < possible_options_array[0].length; i++){
			generate(str+" "+possible_options_array[0][i], new_possible_options_array);
		}
		
		return "";		
	}
	
	
	 private void generateOptions_vector(){
			Vector<String[]> possible_options = new Vector<String[]>();
			
			Iterator itr = Options.iterator();	 
			while (itr.hasNext()) {
		        Option next = (Option) itr.next();
		        if (next.getMutable()){
		        	possible_options.add(generateOptionValues(next));
		        }
		    }
			
			for (Enumeration e = possible_options.elements() ; e.hasMoreElements() ;) {
			       String[] next_option = (String[])e.nextElement();
			       for (int i=0; i < next_option.length; i++){
			    	  System.out.println("element: "+next_option[i]);
			       }
			       System.out.println("------------next array");
			}
						
			generate("", possible_options.toArray(new String[possible_options.size()][]) );
						
			n = options_vector.size();

			for (Enumeration e = options_vector.elements() ; e.hasMoreElements() ;) {
			       String next_option = (String)e.nextElement();      
			       System.out.println("--"+next_option);
			}
	 }
	 
	 
	 private void sub_generate(int j, String str, Object[] possible_options_array, String[] values){
		 	System.out.println("888888str"+str);
		    if ((str.split("[, ]+")).length > values.length+1){		
		 		sub_options_vector.add(str);
				return;
			}
		 	System.out.println("333333333333333j:"+j);
			System.out.println("3333333333333333 :"+values[j]);
				
			if (!values[j].equals("?")){
				if (str.startsWith("-")){
					sub_generate(++j, " "+str+values[j-1], possible_options_array, values);
				}
				else{
					sub_generate(++j, str+","+values[j-1], possible_options_array, values);
				}
				// problem, kdyz je jina velikost mnoziny, ze ktery se vybira a pocet argumentu (aspon to tak vyada)
			}
			else{	
				for (int i=0; i < possible_options_array.length; i++){
					System.out.println("44444444444444i:"+i);
					System.out.println("44444444444444 :"+possible_options_array[i]);
					if (str.startsWith("-")){
						sub_generate(++j, " "+str+possible_options_array[i], possible_options_array, values);
					}
					else{
						sub_generate(++j, str+","+possible_options_array[i], possible_options_array, values);
					}					
					
					// sub_generate(++j, str+","+possible_options_array[i], possible_options_array, values);
					j--;					
				}
			}
			
			return;		 
	 }
}
