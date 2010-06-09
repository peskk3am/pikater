import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import weka.core.Instances;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Agent_ARFFReader extends Agent {
	// File name
	private String fileName;
	// data read from file
	protected Instances data;
	// path to the file
	// private String path = "D:/diplomka/eclipse/diplomka/";
	private String path = System.getProperty("user.dir")+System.getProperty("file.separator");
	private boolean working = false;

	
	boolean ReadFromFile(String fileName){

		try {
			BufferedReader reader = new BufferedReader(
	        		  new FileReader(fileName)
	              );
			data = new Instances(reader);
		    reader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
				
		return true;
	}
	
	protected void setup() {
		// register with DF		  
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();   
		
		sd.setType("ARFFReader"); 
		sd.setName(getName());
		dfd.setName(getAID());
		dfd.addServices(sd);
		
		try {
		    DFService.register(this,dfd);
		} catch (FIPAException e) {
		    System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
		    doDelete();
		}
		
		// Add the CyclicBehaviour
	    addBehaviour(new CyclicBehaviour(this) {
	    	public void action(){
	    		ACLMessage msg = myAgent.receive();
	    		if (msg != null) {
		    		// Message received. Process it
		    		Read(msg.getContent());
		    		
		    		// System.out.println("Message content:"+msg.getContent());
		    		ACLMessage msgOut = new ACLMessage(ACLMessage.INFORM);
		    		msgOut.addReceiver(msg.getSender());
		    		try {
						msgOut.setContentObject(data);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		
		    		send(msgOut);
	    		}
	    		else {
	    			block();
	    		}
	    	}
	    });
	    
		System.out.println("Agent "+getLocalName()+" is ready!");
		

	}  // end Setup
	
	private void Read(String fileName) {
		working = true;

	    // System.out.println("File name: "+path+fileName);
	    
	    if (ReadFromFile(fileName)){
		    System.out.println("Reading of data from file "+fileName+" succesful.");		    
		    // System.out.println(data);
	    }
	    else{
		    System.out.println("Reading of data from file "+fileName+" failed.");
	    }
		    
	  	working = false;
	  } // end Read
	
}
