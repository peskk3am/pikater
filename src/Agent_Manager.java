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
import jade.wrapper.AgentController;
import jade.wrapper.PlatformController;

import ontology.messages.*;
import weka.core.Option;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.*;


public class Agent_Manager extends Agent{
	
	private String receiver;
	private int problem_i = 0;
	
	private Codec codec = new SLCodec();
	private Ontology ontology = MessagesOntology.getInstance();
	
	
	private class SendComputation extends AchieveREInitiator{
		
			public SendComputation(Agent a, ACLMessage request) {
				super(a, request);
				System.out.println(a.getLocalName()+": SendComputation behavior created.");				
			}
			
			// Since we don't know what message to send to the responder
			// when we construct this AchieveREInitiator, we redefine this 
			// method to build the request on the fly
			protected Vector prepareRequests(ACLMessage request) {
				// Klara's note: this method is called just once at the beginning of the behaviour
				// Retrieve the incoming request from the DataStore
				String incomingRequestKey = (String) ((AchieveREResponder) parent).REQUEST_KEY;
				ACLMessage incomingRequest = (ACLMessage) getDataStore().get(incomingRequestKey);
								
				System.out.println("Agent "+getLocalName()+": Received action: "+incomingRequest.getContent()+". Preparing response.");
				

				return prepareComputations(incomingRequest); // Prepare the request to forward to the responder
										
			}
			protected void handleAllResponses(java.util.Vector responses) {
				Enumeration en = responses.elements();
				while(en.hasMoreElements()){
					ACLMessage msgNext = (ACLMessage)en.nextElement();	
					System.out.println("Agent:"+getLocalName()+": Agent "+msgNext.getSender().getName()+" sent a reply.");
				}		
			}
			
			protected void handleAllResultNotifications(java.util.Vector resultNotifications) {
			/*  JADE documentation: 
			 * Known bugs: The handler handleAllResponses is not called if the 
			 * agree message is skipped and the inform message is received instead.
			 * One message for every receiver is sent instead of a single message for all the receivers.
			 */
				Enumeration en = resultNotifications.elements();
				while(en.hasMoreElements()){
					ACLMessage msgNext = (ACLMessage)en.nextElement();	
					System.out.println("Agent:"+getLocalName()+": Agent "+msgNext.getSender().getName()+" sent a reply.");
				}
												
			}
			
			
			private void storeNotification() {
					
				// Retrieve the incoming request from the DataStore
				String incomingRequestkey = (String) ((AchieveREResponder) parent).REQUEST_KEY;
				ACLMessage incomingRequest = (ACLMessage) getDataStore().get(incomingRequestkey);

				// Prepare the notification to the request originator and store it in the DataStore
				ACLMessage notification = incomingRequest.createReply();
				notification.setPerformative(ACLMessage.INFORM);
				String notificationkey = (String) ((AchieveREResponder) parent).RESULT_NOTIFICATION_KEY;
				getDataStore().put(notificationkey, notification);
				
		
			}   // end storeNotification
	 }
	
	
	protected void setup(){
		
			// doWait(1500);  // 1.5 seconds
		
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


	  		  		
		  	AchieveREResponder receive_problem = new AchieveREResponder(this, template_inform) {
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
	  					
	  		/*			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
	  						System.out.println("Agent "+getLocalName()+": preparing the response.");
	  						
	  						try{
	  							ContentElement content = getContentManager().extractContent(request);
	  							// System.out.println(((Action)ce).getAction());
	  							
	  							if (((Action)content).getAction() instanceof Solve){
	  								System.out.println("Agent "+getLocalName()+": received SOLVE instance.");
<<<<<<< HEAD
	  								return prepareTasks(request);
=======
	  								return prepareComputations(request);
>>>>>>> a51021a82a66d6b8b35a7b330f51abd0191e19f7
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
	  					
	  					*/
	  					
		  	};
	
			receive_problem.registerPrepareResultNotification( new SendComputation(this, null) );

		  	addBehaviour(receive_problem);
		
			
	}  // end setup
	
	protected Vector<ACLMessage> prepareComputations(ACLMessage request){
		Vector<ACLMessage> msgVector = new Vector<ACLMessage>();		
		
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
	            	
	            	int computation_i = 0;
	       		 	Iterator a_itr = problem.getAgents().iterator();	 
	            	while (a_itr.hasNext()) {
	    	           ontology.messages.Agent a_next = (ontology.messages.Agent) a_itr.next();
	    	           
	    	           Iterator f_itr = problem.getFile_names().iterator();	 
	    	           while (f_itr.hasNext()) {
	    	        	   String f_next = (String) f_itr.next();
	    	        	   
	    	        	   Computation computation = new Computation();
	    	        	   computation.setAgent(a_next);
	    	        	   computation.setData_file_name(f_next);
	    	        	   computation.setProblem_id(problemID);
	    	        	   computation.setId(problemID+"_"+computation_i);
	    	        	   computation_i++;
	    	        	   
	    	        	   msgVector.add( Compute(computation) );
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
			
			
			return msgVector;
			
	} // end prepareComputation
	
	protected ACLMessage Compute(Computation computation){
	// creates an Option Manager agent and returns a message for this agent

		// create an Option Manager agent
		String option_manager_name = computation.getId();
		PlatformController container = getContainerController(); // get a container controller for creating new agents
		
		try{	
			AgentController agent = container.createNewAgent(option_manager_name, "Agent_Random", new String[0] );
			agent.start();
		}
		catch (Exception e) {
	        System.err.println( "Exception while adding agent"+computation.getId()+": " + e );
	        e.printStackTrace();
	    }
	
		// sreate a message for the Option Manager agent
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());

  		try {
  			Compute compute = new Compute();
  			compute.setComputation(computation);
  			
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
                		
		
	  	msg.addReceiver(new AID(option_manager_name, AID.ISLOCALNAME));		
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
	  	
		return msg;

	} // end Compute()

	
	protected String generateProblemID(){
		Date date = new Date();
		String problem_id = Long.toString(date.getTime())+"_"+problem_i;
		problem_i++;
	    return problem_id;
	}
}
