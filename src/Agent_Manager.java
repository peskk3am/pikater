import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import jade.proto.IteratedAchieveREInitiator;

import ontology.messages.*;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.*;

public class Agent_Manager extends Agent{
	
	// private int nResponders;	
	
	private Codec codec = new SLCodec();
	private Ontology ontology = MessagesOntology.getInstance();
	
	// private Agent_Manager_GUI myGUI;
	
	protected void setup(){
		
			doWait(1500);  // 1.5 seconds
		
		    // myGUI = new Agent_Manager_GUI(this);
		    // myGUI.show();
		    
			
			getContentManager().registerLanguage(codec);
			getContentManager().registerOntology(ontology);
			
		    // register with DF
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();   
			sd.setType("Manager"); 
			sd.setName(getName());
			dfd.setName(getAID());
			dfd.addServices(sd);
			try {
			    DFService.register(this,dfd);
			} catch (FIPAException e) {
			    System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
			    doDelete();
			}  
		  	System.out.println("Manager "+getLocalName()+" is alive and waiting...");
		  			  	
		  	// find a concrete agent according to a selected agent type
		  	// TODO
		    String receiver = "mp1"; 
		    
		  	// get available Options from selected agent:
		  	
		  	// create a request message with GetOptions content
		  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
  			msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			
			// We want to receive a reply in 30 secs
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));			
			
			// Prepare the content.			
			GetOptions get = new GetOptions();
			Action a = new Action();
			a.setAction(get);
			a.setActor(this.getAID());
			
			try {
				// Let JADE convert from Java objects to string
				getContentManager().fillContent(msg, a);
				
			}
			catch (CodecException ce) {
				ce.printStackTrace();
			}
			catch (OntologyException oe) {
				oe.printStackTrace();
			}
			
			
		  	AchieveREInitiator behav = new AchieveREInitiator(this, msg) {
				
				protected void handleInform(ACLMessage inform) {
					System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" replied.");					
				  	// we've just received the Options in an inform message
					Compute(inform);		
				}
				
				protected void handleRefuse(ACLMessage refuse) {
					System.out.println(getLocalName()+": Agent "+refuse.getSender().getName()+" refused to perform the requested action");
				}
				
				protected void handleFailure(ACLMessage failure) {
					if (failure.getSender().equals(myAgent.getAMS())) {
						// FAILURE notification from the JADE runtime: the receiver
						// does not exist
						System.out.println("Responder does not exist");
					}
					else {
						System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
					}
				}

			};
			
			addBehaviour(behav);	
			

			
	}  // end setup
	
	
	
	void Compute(ACLMessage reply){
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());
	  
	  	try {	
	  		ContentElement content = getContentManager().extractContent(reply);
	  		if (content instanceof Result) {
                Result result = (Result) content;
                if (result.getValue() instanceof Options) {
                   Options options = (Options)result.getValue();

                   // Prepare the content.			
       				Task task = new Task();
       				task.setData_file_name("weather.arff");
       				task.setAgent_name("mp1");
       				task.setOptions(options.getOptions());
       				Compute compute = new Compute();
       				compute.setTask(task);
       				
       				Action a = new Action();
       				a.setAction(compute);
       				a.setActor(this.getAID());
       		  		
       		  		getContentManager().fillContent(msg, a);
                
                }
	  		}

	    } catch (OntologyException e){
			e.printStackTrace();
		} catch (Codec.CodecException e){
			e.printStackTrace();
		}
		
	  	
	  	msg.addReceiver(new AID("r", AID.ISLOCALNAME));   // TODO find or create OptionManager agent instead
	  	msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

	  	
	  	AchieveREInitiator compute = new AchieveREInitiator(this, msg) {
	  		
			protected void handleInform(ACLMessage inform) {
				System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" replied.");
			  			
			}
			
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println(getLocalName()+": Agent "+refuse.getSender().getName()+" refused to perform the requested action");
			}
			
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver
					// does not exist
					System.out.println("Responder does not exist");
				}
				else {
					System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
				}
			}

		};
		
		addBehaviour(compute);

	} // end Compute()

}
