import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Vector;

import ontology.messages.Data;
import ontology.messages.DataInstances;
import ontology.messages.Instance;
import ontology.messages.Interval;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Option;


public abstract class Agent_WekaCA extends Agent_ComputingAgent {
	 protected abstract Classifier getModelObject();
	 protected abstract boolean setModelObject(Classifier _cls);
	 protected abstract String getOptFileName();
	 protected abstract Evaluation test();
	 
	 @Override
	 protected ontology.messages.Evaluation evaluateCA() {
		 Evaluation eval = test();
		 
		 ontology.messages.Evaluation result = new ontology.messages.Evaluation();
		 result.setError_rate((float) eval.errorRate());
		 
		 try{
			 result.setKappa_statistic((float)eval.kappa());
		 } catch (Exception e) {
				result.setKappa_statistic(-1);
		 }
			 
		 result.setMean_absolute_error((float) eval.meanAbsoluteError());
		 
		 try {
			result.setRelative_absolute_error((float) eval.relativeAbsoluteError());
		 } catch (Exception e) {
			result.setRelative_absolute_error(-1);
		 }
		 
		 result.setRoot_mean_squared_error((float) eval.rootMeanSquaredError());
		 result.setRoot_relative_squared_error((float) eval.rootRelativeSquaredError());
		 
		 return result;
	 }
	 
	 @Override
	 protected DataInstances getPredictions(Instances test, DataInstances onto_test){
		 
		 Evaluation eval = test();		 
		 double pre[] = new double[test.numInstances()];
		 for (int i=0; i<test.numInstances(); i++){
			 try {
				pre[i] = eval.evaluateModelOnce(getModelObject(), test.instance(i));
			} catch (Exception e) {
				pre[i] = Integer.MAX_VALUE;
			}
		 }
		 
		// copy results to the DataInstancs
		int i = 0; 
		Iterator itr = onto_test.getInstances().iterator();	 		   		 
		while (itr.hasNext()){
			Instance next_instance = (Instance) itr.next();
			next_instance.setPrediction(pre[i]);
			i++;
		}
		 
		return onto_test;
	 }
	 
	 @Override
	 public boolean saveAgent(){
		 try{
			 ObjectOutputStream oos = new ObjectOutputStream(
			                            new FileOutputStream("saved/"+getLocalName()+".model"));
			 
 		     // save model + header
 		     Vector v = new Vector();
 		     v.add(getModelObject());
 		     v.add(new Instances(data, 0));
 		     v.add(trainFileName);
 		     v.add(testFileName);
 		     v.add(state);

 			 oos.writeObject(v);
			 oos.flush();
			 oos.close();
			 System.out.println("Saving... : Description:"+this.toString());
			 return true;

		 }
		 catch (Exception e){
		 	System.out.println(e);
		 	return false;
		 }
	 } // end saveAgent

	 @Override
	 public boolean loadAgent(String agentName){
		 try{
			 // deserialize model + header
			 ObjectInputStream ois = new ObjectInputStream(
                     new FileInputStream("saved/"+agentName+".model"));
			 Vector v = (Vector) ois.readObject();
			 
			 Classifier cls = (Classifier) v.get(0);   // TODO this isn't general enough - cls doesn't have to be derived from Classifier
			 Instances header = (Instances) v.get(1);  // TODO this is not used so far
			 // System.out.println(agentName+" Header: "+header); 
			 trainFileName = (String) v.get(2);
			 testFileName = (String) v.get(3);
			 state = (states) v.get(4);
			 
			 // TODO watch "working" variable
			 setModelObject(cls);			 
			 ois.close();		 
			 
			 System.out.println("Loading... : Description: "+this.toString());
			 System.out.println("                          trainFileName: "+trainFileName);
			 System.out.println("                          testFileName: "+testFileName);
			 System.out.println("                          state: "+state);
			 
			 
			 // re-register with DF
			 // TODO what if it fails?
			 // deregisterWithDF();
			 // registerWithDF();
			 
			 return true;
		 }
		 catch (Exception e){
			 	System.out.println(e);
			 	return false;
		}
	 }  // end loadAgent
	 
	 private ontology.messages.Option convertOption(MyWekaOption _weka_opt){
		 ontology.messages.Option opt = new ontology.messages.Option();
		 Interval interval = null;
		 opt.setMutable(_weka_opt.mutable);
         
         interval = new Interval();
         interval.setMin(_weka_opt.lower);
         interval.setMax(_weka_opt.upper);		  					           					           
         opt.setRange(interval);
         
         if (_weka_opt.set != null){
      	   // copy array to List
      	   List set = new ArrayList();
      	   for (int i=0; i<_weka_opt.set.length; i++){
      		   set.add(_weka_opt.set[i]);
      	   }
      	   opt.setSet(set);
         }
         
         opt.setIs_a_set(_weka_opt.isASet);
         
         interval = new Interval();
         interval.setMin(_weka_opt.numArgsMin);
         interval.setMax(_weka_opt.numArgsMax);		  					           					           
         opt.setNumber_of_args(interval);
         
         opt.setData_type(_weka_opt.type.toString());
         opt.setDescription(_weka_opt.description);
         opt.setName(_weka_opt.name);
         opt.setSynopsis(_weka_opt.synopsis);
         opt.setDefault_value(_weka_opt.default_value);
         opt.setValue(_weka_opt.default_value);
         return opt;
	 }
	 
	 @Override
	 protected void getParameters(){
			// fills the global Options vector
		 
			System.out.println(getLocalName()+": The options are: ");
			 
			String optPath = System.getProperty("user.dir")+getOptFileName();   
			
			agent_options = new ontology.messages.Agent();
			agent_options.setName(getLocalName());
			agent_options.setType(getAgentType());
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
				
	            //list of ontology.messages.Option
	            List _options = new ArrayList();
	            
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
	                	   if (params[2].equals("mixed")){
	                		   dt = MyWekaOption.dataType.MIXED; 
	                	   }
	                	   
	                	   String[] default_options = getModelObject().getOptions();
	                	   
	                	   Enumeration en = getModelObject().listOptions();
			       	       while(en.hasMoreElements()){
			       	    	   
			       	    	   Option next = (weka.core.Option)en.nextElement();
			       	    	   String default_value = "False";
			       	    	   for (int i=0; i<default_options.length; i++){ 
			       	    		 if (default_options[i].equals("-"+next.name())){
									if (default_options[i].startsWith("-")){									
										// if the next array element is again an option name, 
										// (or it is the last element)
										// => it's a boolean parameter
										if (i == default_options.length-1){
											default_value = "True";
										}
										else {
											//if (default_options[i+1].startsWith("-")){
					         				if (default_options[i+1].matches("\\-[A-Z]")){					         					
												default_value = "True";
											}
											else{
												default_value = default_options[i+1];    				
											}
										}	
									}  
			       	    		 }
			       	    	   }
			       	    	   			       	    	    
			       	    	   if ((next.name()).equals(params[1])){
			       	    		   MyWekaOption o;
			                	   if (params.length > 4){
					       	    				                		   
			                		   o = new MyWekaOption(
				       	    				   next.description(), next.name(), next.numArguments(), next.synopsis(), 
				       	    				   dt, new Integer(params[3]).intValue(),
				       	    				   new Integer(params[4]).intValue(),
				       	    				   params[5], default_value, params[6]
				       	    		   ); 
			                		   
			                	   }
			                	   else{
			                		   o = new MyWekaOption(
				       	    				   next.description(), next.name(), next.numArguments(), next.synopsis(), 
				       	    				   dt, 0, 0, "", default_value, ""
				       	    		   );   
			                	   }
			       	    		   
			       	    		   
			       	    		   // convert&save o to options vector
			                	   _options.add(convertOption(o));
			       	    	   }  
			       	       } 

	                }
	                
	                line = bufRead.readLine();
	                
	                count++;
	            }
	            agent_options.setOptions(_options);
	            bufRead.close();
	            
	        }catch (ArrayIndexOutOfBoundsException e){
	            /* If no file was passed on the command line, this exception is
	            generated. A message indicating how to the class should be
	            called is displayed */
	            System.out.println("Usage: java ReadFile filename\n");          
	        }
	        catch (Exception e){
	        	e.printStackTrace();
	        	System.err.println(getLocalName()+": Reading options from .opt file failed.");
	        }
			//Save the agent's options  
			  
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
	 }  // end getParameters



}
