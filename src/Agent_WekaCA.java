import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Option;


public abstract class Agent_WekaCA extends Agent_ComputingAgent {
	 protected abstract Classifier getModelObject();
	 protected abstract boolean setModelObject(Classifier _cls);
	 protected abstract String getOptFileName();
	 
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
	 
	 protected void getParameters(){
			// fills the global Options vector
			 
			System.out.println(getLocalName()+": The options are: ");
			 
			String optPath = System.getProperty("user.dir")+getOptFileName();   
			
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
	                	   if (params[2].equals("mixed")){
	                		   dt = MyWekaOption.dataType.MIXED; 
	                	   }
	                	   
	                	   Enumeration en = getModelObject().listOptions();

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
	 }  // end getParameters



}
