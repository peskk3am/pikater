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

public class Agent_MultilayerPerceptron extends Agent_ComputingAgent {
	private MultilayerPerceptron cls = new MultilayerPerceptron();;
	private String optPath = System.getProperty("user.dir")+"/options/MultilayerPerceptron.opt";
	
	
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
	
	 
	 protected void getParameters(){
		 // fills the global Options vector
		 
		 System.out.println(getLocalName()+": The options are: ");
   
		// read options from file
		try {
			/*  Sets up a file reader to read the options file */
			FileReader input = new FileReader(optPath);
            /* Filter FileReader through a Buffered read to read a line at a
               time */
            BufferedReader bufRead = new BufferedReader(input);
           
            String line;    // String that holds current file line
            int count = 0;  // Line number of count 
            // Read first line
            line = bufRead.readLine();
            count++;
            
            Options = new Vector<MyWekaOption>();
            
            // Read through file one line at time. Print line # and line
            while (line != null){
                System.out.println("    "+count+": "+line);
                
                // parse the line
                String delims = "[ ]+";
                String[] params = line.split(delims, 7);

                
                
                if (params[0].equals("$")){
          		  	                 	   
                	   MyWekaOption.dataType dt = MyWekaOption.dataType.BOOLEAN; 
                	   
                	   if (params[2].equals("boolean")){
                		   dt = MyWekaOption.dataType.BOOLEAN; 
                	   }
                	   if (params[2].equals("float")){
                		   dt = MyWekaOption.dataType.FLOAT;
                	   }
                	   if (params[2].equals("int")){
                		   dt = MyWekaOption.dataType.INT; 
                	   }

                	   
                	   
                	   Enumeration en = cls.listOptions();

		       	       while(en.hasMoreElements()){
		       	    	   
		       	    	   Option next = (weka.core.Option)en.nextElement();
		       	    	   
		       	    	   if ((next.name()).equals(params[1])){
		       	    		   MyWekaOption o;
		                	   if (params.length > 3){
		                		   o = new MyWekaOption(
			       	    				   next.description(), next.name(), next.numArguments(), next.synopsis(), 
			       	    				   dt, new Integer(params[3]).intValue(), new Integer(params[4]).intValue(), params[5], params[6]
			       	    		   );   
		                	   }
		                	   else{
		                		   o = new MyWekaOption(
			       	    				   next.description(), next.name(), next.numArguments(), next.synopsis(), 
			       	    				   dt, 0, 0, "", ""
			       	    		   );   
		                	   }
		       	    		   
		       	    		   
		       	    		   // save o to options vector
		       	    		   Options.add(o);
		       	    	   }
		       	       }

                }
                
                line = bufRead.readLine();
                
                count++;
            }
            
            bufRead.close();
            
        }catch (ArrayIndexOutOfBoundsException e){
            /* If no file was passed on the command line, this exception is
            generated. A message indicating how to the class should be
            called is displayed */
            System.out.println("Usage: java ReadFile filename\n");          

        }catch (IOException e){
            // If another exception is generated, print a stack trace
            e.printStackTrace();
        }
        catch (Exception e){
        	e.printStackTrace();
        	System.err.println(getLocalName()+": Reading options from .opt file failed.");
        }
		  
		  
		/*  Enumeration en = cls.listOptions();

	       while(en.hasMoreElements()){
	    	  	Option next = (weka.core.Option)en.nextElement();
	    	   System.out.println("  "+next.description()+ ", "
	    	  						   +next.name()+ ", "
	    	  						   +next.numArguments()+ ", "
	    	  						   +next.synopsis()
	    	  	);
	    	  	System.out.println();
	       }
		  */
		  
	     /*  System.out.println("MyWekaOptions: ");
	       for (Enumeration e = Options.elements() ; e.hasMoreElements() ;) {
	           MyWekaOption next = (MyWekaOption)e.nextElement();
	    	   System.out.print(next.name+" ");
	    	   System.out.print(next.lower+" ");
	    	   System.out.print(next.upper+" ");
	    	   System.out.print(next.type+" ");
	    	   System.out.print(next.numArgsMin+" ");
	    	   System.out.print(next.numArgsMax+" ");
	    	   System.out.println(next.set);
	    	   System.out.println("------------");
	       }
		*/
	 }
	 
	 protected void train(){
		 working = true;   
		 System.out.println("Agent "+getLocalName()+": Training... ");
		       
		
		 try {
			if (OPTIONS.length > 0){
				cls.setOptions(OPTIONS);
			}
			cls.setAutoBuild(true);
			cls.buildClassifier(train);
			
			state = states.TRAINED;  // change agent state
			
			OPTIONS = cls.getOptions(); 
			
			// write out net parameters
			System.out.println(getLocalName()+" "+getOptions());

	 	 } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
		 
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