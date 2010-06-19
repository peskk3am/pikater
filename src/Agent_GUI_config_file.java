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
	private String configFileName;
	
	
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
		
		configFileName = getConfigFileName();
		try {
			getProblemsFromXMLFile(configFileName);
		    for (Enumeration e = problems.elements() ; e.hasMoreElements() ;) {
			       Problem next_problem = (Problem)e.nextElement();
			       Iterator itr = next_problem.getAgents().iterator();	 		   		 
		   		 	while (itr.hasNext()) {
		   		 		ontology.messages.Agent next_agent = (ontology.messages.Agent) itr.next();
		   		 		getAgentOptions(next_agent.getName());
		   		 	}
		    }
		}
		// indicates a well-formedness error
        catch (JDOMException e) { 
          System.out.println(configFileName + " is not well-formed. "+e.getMessage());
        }  
        catch (IOException e) { 
          System.out.print("Could not check " + configFileName);
          System.out.println(" because " + e.getMessage());
        } 
		
        // getProblemFromFile("config");

		
	}	// end mySetup

	@Override
  	protected void displayPartialResult(ACLMessage inform) {
		System.out.println("Partial results");
	} 

	private String getConfigFileName(){
		return (String)getArguments()[0];
	}
	
}
