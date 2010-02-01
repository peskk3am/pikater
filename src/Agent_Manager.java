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

import ontology.*;
import jade.content.lang.Codec;
import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.sl.*;;

public class Agent_Manager extends Agent{
	
	private int nResponders;	
	
	protected void setup(){
			doWait(1500);  // 1.5 seconds
		
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
		  			  	

		  	
		  	
		  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
  			msg.addReceiver(new AID("mp1", AID.ISLOCALNAME));
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			// We want to receive a reply in 30 secs
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));			
			msg.setContent("Send options");
			
			
			
		  	AchieveREInitiator behav = new AchieveREInitiator(this, msg) {
				
				protected void handleInform(ACLMessage inform) {
					System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" replied.");					
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
		
	  	// ACLMessage msg = reply;
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
	  	// msg.clearAllReceiver();
	  	
	  	try {
			// System.out.println("!!!!!!!!!!!!! "+reply.getContentObject());
	  		msg.setContentObject(reply.getContentObject());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  	
	  	msg.addReceiver(new AID("r", AID.ISLOCALNAME));
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
