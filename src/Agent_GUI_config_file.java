import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.jdom.JDOMException;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import ontology.messages.*;


public class Agent_GUI_config_file extends Agent_GUI{

	private String path = System.getProperty("user.dir")+System.getProperty("file.separator");
 
	
	
	@Override
	protected void displayOptions(Problem problem, String message) {
		System.out.println("Agent :"+getName()+": Displaying the options ;) "+message);
	} //  end displayOptions

	@Override
	protected void displayResult(ACLMessage inform) {
		System.out.println("Agent :"+getName()+": Displaying the results ;)");
	}

	@Override
	protected void allOptionsReceived(int problem_id) {
		sendProblem(problem_id);
	}
	
	@Override
	protected String getAgentType() {
		return "GUI config file";
	}

	@Override
	protected void mySetup() {
		doWait(1000);
		
		String fileName = "xml_config";
		try {
			// System.out.println("Agent "+getLocalName()+": "+getProblemsFromXMLFile(fileName));
			getProblemsFromXMLFile(fileName);
		    for (Enumeration e = problems.elements() ; e.hasMoreElements() ;) {
			       Problem next_problem = (Problem)e.nextElement();
			       Iterator itr = next_problem.getAgents().iterator();	 		   		 
		   		 	while (itr.hasNext()) {
		   		 		ontology.messages.Agent next_agent = (ontology.messages.Agent) itr.next();
		   		 		getAgentOptions(next_agent.getName());
		   		 	}
			       // sendProblem(next_problem);
		    }
		}
		// indicates a well-formedness error
        catch (JDOMException e) { 
          System.out.println(fileName + " is not well-formed. "+e.getMessage());
        }  
        catch (IOException e) { 
          System.out.print("Could not check " + fileName);
          System.out.println(" because " + e.getMessage());
        } 
		
        // getProblemFromFile("config");

		
	}	// end mySetup

	@Override
  	protected void displayPartialResult(ACLMessage inform) {
		System.out.println("Partial results");
	} 

/*	
	void getProblemFromFile(String fileName){
		
		try {
					
			//  Sets up a file reader to read the init file 
			FileReader input = new FileReader(path+fileName);
            // Filter FileReader through a Buffered read to read a line at a
               time 
            BufferedReader bufRead = new BufferedReader(input);
           
            String line;    // String that holds current file line
            int count = 0;  // Line number of count 
            // Read first line
            line = bufRead.readLine();
            count++;
            
            
            // Read through file one line at time. Print line # and line
            while (line != null){
                System.out.println(count+": "+line);
                
                // parse the line
                String delims = "[ ]+";
                String[] params = line.split(delims);
                if (params[0].equals("$f")){
                	
                	if (params.length != 2){
                		// we want just one parameter per line (a filename)
                		throw new InterruptedException();
                	}
               			
                	addFileToProblem(params[1]);
                }
                
                if (params[0].equals("$a")){

                	String[] rest_of_the_array;           	
                	rest_of_the_array = new String[params.length - 1];
                	for (int i=1; i < params.length; i++){
                		rest_of_the_array[i-1] = params[i]; 
                	}              	
                	addAgentToProblem(rest_of_the_array);
                	
                	getAgentOptions(params[1]);
                                		
                }
                
                line = bufRead.readLine();
                
                count++;
            }
            
            bufRead.close();
			

            
        }catch (ArrayIndexOutOfBoundsException e){
            // If no file was passed on the command line, this exception is
            // generated. A message indicating how to the class should be
            // called is displayed
            // System.out.println("Usage: java ReadFile filename\n");          

        }catch (IOException e){
            // If another exception is generated, print a stack trace
            e.printStackTrace();
            
        } catch (Exception e) {  // TODO change the Exception class
        	System.out.println(fileName+"file: Syntax Error");
			e.printStackTrace();
		}

       
        
	} // end getProblemFromFile

	*/
}
