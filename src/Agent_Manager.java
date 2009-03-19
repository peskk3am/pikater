import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import ontology.O_Ontology;

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
import jade.proto.ContractNetInitiator;

import ontology.*;
import jade.content.lang.Codec;
import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.sl.*;

public class Agent_Manager extends Agent{
	
	private int nResponders;	
	
	protected void setup(){
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
		  	System.out.println("Manager "+getLocalName()+" is alive and waiting for CFP...");
		  			  	
		  	// find responders
			AID[] Responders = null;

		  	DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd_responder = new ServiceDescription();
            sd_responder.setType("ComputingAgent");
	        template.addServices(sd_responder);
	        try {
	         	DFAgentDescription[] result = DFService.search(this, template); 
	         	System.out.println("Found the following computing agents:");
	            nResponders = result.length;

	         	Responders = new AID[nResponders];
	            
	           for (int i = 0; i < nResponders; ++i) {
	        	   Responders[i] = result[i].getName();
		           System.out.println(Responders[i].getName());
	           }

	         }
	         catch (FIPAException fe) {
	           fe.printStackTrace();
	         }
		  	
		  	
		  	Object[] args = getArguments();
		  	if (Responders != null && nResponders > 0) {
		  		System.out.println("Number of responders: "+nResponders);
		  		
		  		// Fill the CFP message
		  		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		  		for (int i = 0; i < nResponders; ++i) {
		  			msg.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
		  		}
					msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
					// We want to receive a reply in 30 secs
					msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
					
					// Set responders' parameters
					msg.setContent("weather.arff");
				
					addBehaviour( new ContractNetInitiator(this, msg) {
						
						protected void handlePropose(ACLMessage propose, Vector v) {
							System.out.println("Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
						}
						
						protected void handleRefuse(ACLMessage refuse) {
							System.out.println("Agent "+refuse.getSender().getName()+" refused");
						}
						
						protected void handleFailure(ACLMessage failure) {
							if (failure.getSender().equals(myAgent.getAMS())) {
								// FAILURE notification from the JADE runtime: the receiver
								// does not exist
								System.out.println("Responder does not exist");
							}
							else {
								System.out.println("Agent "+failure.getSender().getName()+" failed");
							}
							// Immediate failure --> we will not receive a response from this agent
							nResponders--;
						}
						
						protected void handleAllResponses(Vector responses, Vector acceptances) {
							if (responses.size() < nResponders) {
								// Some responder didn't reply within the specified timeout
								System.out.println("Timeout expired: missing "+(nResponders - responses.size())+" responses");
							}
							// Evaluate proposals.
							double bestProposal = 100;
							AID bestProposer = null;
							ACLMessage accept = null;
							Enumeration e = responses.elements();
							while (e.hasMoreElements()) {
								ACLMessage msg = (ACLMessage) e.nextElement();
								if (msg.getPerformative() == ACLMessage.PROPOSE) {
									ACLMessage reply = msg.createReply();
									reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
									acceptances.addElement(reply);
									
									double proposal = Double.parseDouble(msg.getContent());
									
									// choose the best agent
									
									if (proposal < bestProposal) {
										bestProposal = proposal;
										bestProposer = msg.getSender();
										accept = reply;
									}
								}
							}
							// Accept the proposal of the best proposer
							if (accept != null) {
								System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
								accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							}			
										
						}
						
						protected void handleInform(ACLMessage inform) {
							System.out.println("Agent "+inform.getSender().getName()+" was THE BEST!");
						}
					} );
					
		  	}
		  	else {
		  		System.out.println("No responder specified.");
		  	}
		  	
	}  // end setup
	
}
