import jade.util.leap.Iterator;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import ontology.messages.Option;
import ontology.messages.Problem;


public class Agent_ChooseXValues extends Agent_OptionsManager {
	private int n = Integer.MAX_VALUE;
	private int ni = 0;
	
	private Vector<String> options_vector = new Vector<String>();
	
	
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
    	// choose random number of arguments
    	int numArgs = (int)(next.getNumber_of_args().getMin()+generator.nextInt((int)(next.getNumber_of_args().getMax()
    			-next.getNumber_of_args().getMin()+1)));
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
		   				int vInt = (int) (next.getRange().getMin() + i * range / x );
    		   			si += Integer.toString(vInt)+",";
		   			}
		   			int vInt = (int) (next.getRange().getMin() + i * range / x );
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
			  return new String[] {optionName, ""};
		  }
	   }
	   else{
		   // TODO
	   }
		   
	   return new String[0];
	}
	
	@Override
	protected String generateNewOptions(MyWekaEvaluation result) {
		if (n == Integer.MAX_VALUE){
			// generate the options_vector when called for the first time
			generateOptions_vector();
		}
		if (n == 0){
			return "";
		}
		return options_vector.get(ni++); 		
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
						
			generate("", possible_options.toArray(new String[possible_options.size()][]));
						
			n = options_vector.size();

			for (Enumeration e = options_vector.elements() ; e.hasMoreElements() ;) {
			       String next_option = (String)e.nextElement();      
			       System.out.println("--"+next_option);
			}
	 }
}
