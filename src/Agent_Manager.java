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
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import jade.proto.IteratedAchieveREInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

import ontology.messages.*;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.*;


public class Agent_Manager extends Agent{
	
	private String receiver;	
	
	private Codec codec = new SLCodec();
	private Ontology ontology = MessagesOntology.getInstance();
	
	// private Agent_Manager_GUI myGUI;
	
	protected void setup(){
		
			// doWait(1500);  // 1.5 seconds
		
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
		  			  	
		  	
		  	MessageTemplate template_inform = MessageTemplate.and(
	  		  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
	  		  		MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
	  		  				MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.MatchOntology(ontology.getName()))
	  		  				)
	  		  	);


	  		  		
	  		addBehaviour(new AchieveREResponder(this, template_inform) {
	  					protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
	  						System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
	  						
	  							// We agree to perform the action. Note that in the FIPA-Request
	  							// protocol the AGREE message is optional. Return null if you
	  							// don't want to send it.						
	  							// System.out.println("Agent "+getLocalName()+": Agree");
	  							// ACLMessage agree = request.createReply();
	  							// agree.setPerformative(ACLMessage.AGREE);
	  							// return agree;
	  							return null;
	  					}  // end prepareResponse
	  					
	  					protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
	  						System.out.println("Agent "+getLocalName()+": preparing the response.");
	  						
	  						try{
	  							ContentElement content = getContentManager().extractContent(request);
	  							// System.out.println(((Action)ce).getAction());
	  							
	  							if (((Action)content).getAction() instanceof Solve){
	  								System.out.println("Agent "+getLocalName()+": received SOLVE instance.");
	  								return prepareTasks(request);
	  							}
	  							
	  						}
  							catch (CodecException ce) {
  								ce.printStackTrace();
  								}
							catch (OntologyException oe) {
  								oe.printStackTrace();
  							}
							
							ACLMessage notUnderstood = request.createReply();
							notUnderstood.setPerformative(ACLMessage.NOT_UNDERSTOOD);
  							return notUnderstood;
  							
							
							// return 
	  						
	  					}  //  end prepareResultNotification
	  					
	  				} );
		
			
	}  // end setup
	
	protected ACLMessage prepareTasks(ACLMessage request){
		   
		ContentElement content;
		try {
			content = getContentManager().extractContent(request);
	    	System.out.println("Agent "+getLocalName()+": "+content);
	    	
	    		if (((Action)content).getAction() instanceof Solve){
	    	// if (content instanceof Result) {
	                Action action = (Action) content;
	                Solve solve = (Solve)action.getAction();
	                Problem problem = (Problem)solve.getProblem();
	                          		 	
	            	String problemID = generateProblemID();
	            	problem.setId(problemID);
	            	
	       		 	Iterator a_itr = problem.getAgents().iterator();	 
	            	while (a_itr.hasNext()) {
	    	           ontology.messages.Agent a_next = (ontology.messages.Agent) a_itr.next();
	    	           
	    	           Iterator f_itr = problem.getFile_names().iterator();	 
	    	           while (f_itr.hasNext()) {
	    	        	   String f_next = (String) f_itr.next();
	    	        	   
	    	        	   Task task = new Task();
	    	        	   task.setAgent(a_next);
	    	        	   task.setData_file_name(f_next);
	    	        	   task.setProblem_id(problemID);
	    	        	   
	    	        	   System.out.println("Agent "+getLocalName()+": TASK: "+task);
	    	        	   
	    	        	   Compute(task);
	    	           } // end while (iteration over files)
	       	           
	               	} // end while (iteration over agents List)
 			    	             
                }
			} catch (UngroundedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CodecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OntologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	   
			
			//TODO zjistit, jestli uz jsou vsechny a vyhodnotit je
			ACLMessage msgOut = new ACLMessage(ACLMessage.INFORM);
			msgOut.setLanguage(codec.getName());
			msgOut.setOntology(ontology.getName());
			
			msgOut.addReceiver(request.getSender());
			
			return msgOut;
			
	} // end prepareTasks
	
	protected void Compute(Task task){
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());
	  

  		try {
  			Compute compute = new Compute();
  			compute.setTask(task);
  			
  			Action a = new Action();
  			a.setAction(compute);
  			a.setActor(this.getAID());
  	  		
			getContentManager().fillContent(msg, a);
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                
		
	  	
	  	msg.addReceiver(new AID("r", AID.ISLOCALNAME));   // TODO find or create OptionManager agent instead
	  	msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

	  	
	  	AchieveREInitiator computeAchieveREInitiator = new AchieveREInitiator(this, msg) {
	  		
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
		
		addBehaviour(computeAchieveREInitiator);

	} // end Compute()

	
	protected String generateProblemID(){
		Date date = new Date();
	    return Long.toString(date.getTime());
	}
}
