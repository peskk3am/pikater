import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.Subscription;
import jade.proto.SubscriptionResponder.SubscriptionManager;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import jade.wrapper.StaleProxyException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import ontology.messages.Computation;
import ontology.messages.Compute;
import ontology.messages.Data;
import ontology.messages.Evaluation;
import ontology.messages.MessagesOntology;
import ontology.messages.Metadata;
import ontology.messages.Option;
import ontology.messages.Problem;
import ontology.messages.Results;
import ontology.messages.Solve;
import ontology.messages.Task;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class Agent_Manager extends Agent{
	private String path = System.getProperty("user.dir")+System.getProperty("file.separator");
	
	private int problem_i = 0;
	
	private long timeout = 10000;
	
	private Codec codec = new SLCodec();
	private Ontology ontology = MessagesOntology.getInstance();
	
	private Set subscriptions = new HashSet();
	// private Subscription subscription;

	double minAttributes = Integer.MAX_VALUE;
	double maxAttributes = Integer.MIN_VALUE;
	double minInstances = Integer.MAX_VALUE;
	double maxInstances = Integer.MIN_VALUE;
	
	Vector<AID> busyAgents = new Vector<AID>();  // by this manager
	
	private class SendComputation extends AchieveREInitiator{
			private ACLMessage failure = null;
			private ACLMessage incomingRequest = null;
			private ACLMessage incomingResponse = null;
			private String parentConversationID;
			
			public SendComputation(Agent a, ACLMessage request, ACLMessage response) {
				super(a, request);
				incomingRequest = request;
				incomingResponse = response;
				parentConversationID = incomingRequest.getConversationId();
				System.out.println(a.getLocalName()+": SendComputation behavior created."); // +request);				
			}
			
			// Since we don't know what message to send to the responder
			// when we construct this AchieveREInitiator, we redefine this 
			// method to build the request on the fly
			protected Vector prepareRequests(ACLMessage request) {
				// Klara's note: this method is called just once at the beginning of the behaviour
				// System.out.println("Agent "+getLocalName()+": Received action: "+incomingRequest.getContent()+". Preparing response.");
				
				// get generated problem id from agree message (it contains a string: "gui_id and id" of a problem 
				String[] ID = incomingResponse.getContent().split(" ");
				String problemId = ID[1];
			
				Vector v = prepareComputations(this, incomingRequest, problemId, failure);
				if (v.size() == 0){
					storeNotification(ACLMessage.FAILURE);
				}
				return v;								
			}
			
			protected void handleInform(ACLMessage inform) {
				System.out.println("Agent:"+getLocalName()+": Agent "+inform.getSender().getName()+" sent an inform.");
				sendSubscription(inform);
				// killAgent(inform.getSender().getName());
			}
			
			protected void handleFailure(ACLMessage failure) {			
				System.out.println("Agent:"+getLocalName()+": Agent "+failure.getSender().getName()+" sent a failure.");

				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver does not exist
					System.out.println("Responder does not exist");
				}
				else {
					System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");					
				}
				sendSubscription(failure);
								
				// if (System.currentTimeMillis() < timeout){
					// this.reset();
				//	this.failure = failure;
				//	addBehaviour(this);
				//}
				//else{
				//	sendSubscription(failure);
					// killAgent(failure.getSender().getName());
				//}				
				
				// killAgent(failure.getSender().getName());
			}

			protected void handleAllResultNotifications(java.util.Vector resultNotifications) {
			/*  JADE documentation: 
			 * Known bugs: The handler handleAllResponses is not called if the 
			 * agree message is skipped and the inform message is received instead.
			 * One message for every receiver is sent instead of a single message for all the receivers.
			 */	
			
				if (resultNotifications.size() == 0){
					storeNotification( ACLMessage.FAILURE );
				}
				else{
					storeNotification( ACLMessage.INFORM );
				}
				
			}
			
			private void sendSubscription(ACLMessage result) {
				// System.out.println("Agent: "+getLocalName()+": result: "+result+" "+result.getPerformative());
								
				// Prepare the msgOut to the request originator				
				ACLMessage msgOut = incomingRequest.createReply();
				msgOut.setPerformative(result.getPerformative());
									
				String problemGuiId = null;
			
				// if (result.getPerformative() != ACLMessage.FAILURE){

					// fill its content
					Results results = prepareComputationResults(result);
					if (results != null){
					 							
						// write results to the database
						Iterator resIterator = results.getResults().iterator();					 	
					 	while (resIterator.hasNext()) {					 		
					 		DataManagerService.saveResult(myAgent, (Task)resIterator.next());
					 	}
						
					 	
						writeXMLResults(results);						
						
						msgOut.setPerformative(ACLMessage.INFORM);
						ContentElement content;
						try {
							content = getContentManager().extractContent(incomingRequest);
							if (((Action)content).getAction() instanceof Solve){
			                    Solve solve = (Solve) ((Action)content).getAction();
			                    problemGuiId = solve.getProblem().getGui_id();
							}
							Result _result = new Result((Action)content, results);
							getContentManager().fillContent(msgOut, _result);
								
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
					}
					else{
						msgOut.setPerformative(ACLMessage.FAILURE);						
						msgOut.setContent(result.getContent());
					}
				// }  // end if				
		
				// go through every subscription				
				java.util.Iterator it = subscriptions.iterator();
				while(it.hasNext()){
					Subscription subscription = (Subscription)it.next();
					
					if (subscription.getMessage().getConversationId().equals("subscription"+incomingRequest.getConversationId())){
						subscription.notify(msgOut);
					}
				}
				try{
					String name = ((Task)results.getResults().iterator().next()).getAgent().getName();
					busyAgents.remove(new AID(name, AID.ISLOCALNAME));
				}
				catch (Exception e){
					// do nothing (we don't need to remove an agent, if there wasn't any)
				}
				
				//*/					            					
			}
			
			private void storeNotification(int performative) {				
				// Create an outgoing message
				ACLMessage msgOut = incomingRequest.createReply();
				msgOut.setPerformative(performative);
				
				if (performative == ACLMessage.FAILURE){
					System.out.println("Agent: "+getLocalName()+": no results from the option managers received.");
					msgOut.setContent("No results from the option managers received");
				}
				else{
					System.out.println("Agent: "+getLocalName()+": all results sent.");	
					msgOut.setContent("Finished");
				}
								
				send(msgOut);
						
			}   // end storeNotification

			private void killAgent(String name){
				System.out.println("Agent:"+getLocalName()+": Agent "+name+" is being killed.");

				PlatformController container = getContainerController();

				try {
					container.getAgent(name).kill();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}

	} // end SendComputaion behavior
		
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
		  			  	
		  	
	  	
		  	SubscriptionManager subscriptionManager = new SubscriptionManager() {
		        public boolean register(Subscription s) {
		        	subscriptions.add(s);
		        	return true;
		        }
		        public boolean deregister(Subscription s) {
		        	subscriptions.remove(s);
		        	return true;
		        }
		      };


		    MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchOntology(ontology.getName()),   // TODO MatchLanguage, MatchProtocol...
					MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE), MessageTemplate.MatchPerformative(ACLMessage.CANCEL)));


		  	SubscriptionResponder send_results = new SubscriptionResponder(this, mt, subscriptionManager) {
		  		// protected ACLMessage handleSubscription(ACLMessage subscription_msg) {
		  		//	createSubscription(subscription_msg);
		  		//	return null;
		  		// }
		  	};
		  	addBehaviour(send_results);
	
			MessageTemplate template_inform = MessageTemplate.and(
	  		  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
	  		  		MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
	  		  				MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.MatchOntology(ontology.getName()))
	  		  				)
	  		);
		  	
		  	AchieveREResponder receive_problem = new AchieveREResponder(this, template_inform) {
		  		protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		  		System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()); // +". Action is "+request.getContent());

			  		// We agree to perform the action. Note that in the FIPA-Request
			  		// protocol the AGREE message is optional. Return null if you
			  		// don't want to send it.
			  		
			  		ACLMessage agree = request.createReply();
			  		agree.setPerformative(ACLMessage.AGREE);
			  			  		
			  		ContentElement content;
					try {
						content = getContentManager().extractContent(request);
			    		if (((Action)content).getAction() instanceof Solve){
			                Action action = (Action) content;
			                Solve solve = (Solve)action.getAction();
			                Problem problem = (Problem)solve.getProblem();
			                agree.setContent(problem.getGui_id() + " " + generateProblemID());
			               			                
			                return agree;
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
					
					agree.setPerformative(ACLMessage.REFUSE);				
													
					return agree;			  		
			  		
			  	} // end prepareResponse
		  		
		  		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		  			// addSolveProblemBehaviour(myAgent, request);
		  			addBehaviour(new SendComputation(myAgent, request, response));
		  			return null; // we don't want to send it now, but after the result is received from an option manager
		  		}
	  		};

	  		// receive_problem.registerPrepareResultNotification( new SendComputation(this, null) );

	  		addBehaviour(receive_problem);

		  	
			
	}  // end setup
	
	protected Vector<ACLMessage> prepareComputations(SendComputation behav, ACLMessage request, String problemId,
			ACLMessage failure){
		
		Vector<ACLMessage> msgVector = new Vector<ACLMessage>();				
		// System.out.println("Agent "+getLocalName()+" failure :"+failure);
		
		ContentElement content;
		try {
			content = getContentManager().extractContent(request);
	    	System.out.println("Agent "+getLocalName()+": "+content);
	    	
	    		if (((Action)content).getAction() instanceof Solve){

	                Action action = (Action) content;
	                Solve solve = (Solve)action.getAction();
	                Problem problem = (Problem)solve.getProblem();
	                          		 	
	            	// String problemID = generateProblemID();
	            	problem.setId(problemId);
	            	
	            	int computation_i = 0;
	            	Iterator d_itr = problem.getData().iterator();	 
	            	while (d_itr.hasNext()) {
	    	           Data next_data = (Data) d_itr.next();	    	           	    	        
	    	           
	    	           if (next_data.getMetadata() != null){
	    	        	   next_data.getMetadata().setInternal_name(next_data.getTrain_file_name());
	    	           }
	    	           
	    	           Iterator a_itr = problem.getAgents().iterator();
	    	           while (a_itr.hasNext()) {
	    	        	   ontology.messages.Agent a_next = (ontology.messages.Agent) a_itr.next();	    	       
    	        		   
	    	        	   ontology.messages.Agent a_next_copy = new ontology.messages.Agent();
	    	        	   a_next_copy.setGui_id(a_next.getGui_id());
	    	        	   a_next_copy.setName(a_next.getName());
	    	        	   a_next_copy.setOptions(a_next.getOptions());
	    	        	   a_next_copy.setType(a_next.getType());
	    	        	  
	    	        	   if (a_next_copy.getName() == null){
	    	        		   String agentType = a_next.getType();		    	        		   	    	        		   	    	        		   	    	        		   
	    	        		   boolean getOptions = false;
	    	        		   if (agentType.contains("?")){	    	        			   
	    	        			   
	    	        			   Metadata metadata;
	    	        			   if (next_data.getMetadata() == null){
	    	        				   metadata = new Metadata(); 
	    	        				   metadata.setInternal_name(next_data.getTest_file_name());
	    	        			   }
	    	        			   else{
	    	        				   metadata = next_data.getMetadata();
	    	        			   }
		    	        		   
		    	        		   a_next = chooseTheBestAgent(metadata);
		    	        		   
		    	        		   if (a_next == null){
		    	        			   ACLMessage msg = new ACLMessage(ACLMessage.FAILURE);
		    	        			   msg.setContent("No metadata available.");
		    	        			   behav.sendSubscription(msg);		    	        			   
		    	        		   }
		    	        		   else{	    	        			   
		    	        			   getOptions = true;
		    	        			   agentType = a_next.getType();
		    	        			   a_next_copy.setType(agentType);
		    	        		   }
	    	        		   }

	    	        		   if (a_next != null){
		    	        		   AID aid = null;
		    	        		   String agentName = null;		    	      	    	        		  	    	        		   
		    	        		   while (aid == null) { // TODO && System.currentTimeMillis() < timeout){
			    	    				// try until you find agent of the given type or you manage to create it	    	        			    
		    	        			    aid = getAgentByType(agentType);
			    	    				//if (aid == null){
			    	    					// agent of given type doesn't exist
			    	    				//	agentName = generateName(agentType);
			    	    				//	aid = createAgent("Agent_"+agentType, agentName);
			    	    				//	doWait(100);
			    	    				//}
			    	    			}
		    	        		   if (aid == null){	    	        			   
		    	        			   ACLMessage msg = new ACLMessage(ACLMessage.FAILURE);
		    	        			   msg.setContent(agentType+" agent could not be created.");
		    	        			   behav.sendSubscription(msg);		    	        			   
		    	        		   }
		    	        		   else{
			    	        		   agentName = aid.getLocalName();
			    	        		   // a_next.setName(agentName);
			    	        		   a_next_copy.setName(agentName);
		    	        			   
		    	        			   if (getOptions){
		    	        				   ontology.messages.Agent agent_options = onlyGetAgentOptions(agentName);
		    	        				   a_next_copy.setOptions(mergeOptions(agent_options.getOptions(), a_next.getOptions()));
			    	        			   
		    	        				   System.out.println("********** Agent "+agentType+
				    	        				   " recommended. Options: "+a_next_copy.optionsToString()+"**********");		    	        			  

		    	        			   }
		    	        		   }
	    	        		   }
	    	        	   }
	    	        	   
	    	        	   if (a_next != null){
		    	        	   Computation computation = new Computation();		    	        	   
		    	        	   computation.setAgent(a_next_copy);
		    	        	   computation.setData(next_data);
		    	        	   computation.setProblem_id(problemId);
		    	        	   computation.setId(problemId+"_"+computation_i);
		    	        	   computation.setTimeout(problem.getTimeout());
		    	        	   computation.setMethod(problem.getMethod());
		    	        	   computation_i++;
		    	        	   
		    	        	   msgVector.add( Compute(computation) );
	    	        	   }
	    	           } // end while (iteration over files)
	    	           
	    	           // enter metadata to the table
	    	           if (next_data.getMetadata() != null){
	    	        	   DataManagerService.saveMetadata(this, next_data.getMetadata());	    	        	
	    	           }
	    	           
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
			
	} // end prepareComputations
	
	private List mergeOptions(List o1_CA, List o2){
			List new_options = new ArrayList();
			if (o1_CA != null) {				
				 
				// if this type of agent has got some options
				// update the options (merge them)
		 			
				// go through the CA options 
	   		 	// replace the value and add it to the new options			
				Iterator o2itr = o2.iterator();	 		   		 
	   		 	while (o2itr.hasNext()) {
	   		 		Option next_option = (Option) o2itr.next();	   		 			   		 	
	   		 		
	   		 		Iterator o1CAitr = o1_CA.iterator();		   		 	
		   		 	while (o1CAitr.hasNext()) {
		   		 		Option next_CA_option = (Option) o1CAitr.next();
		   		 		
		   		 		if (next_option.getName().equals(next_CA_option.getName())){
		   		 			// copy the value		   		 			
		   		 			next_CA_option.setValue(next_option.getValue());
		   		 			next_CA_option.setMutable(false);
		   		 			
		   		 			new_options.add(next_CA_option);
		   		 			
		   		 		}
	   		 		}
	   		 	}							
				
			}
			return new_options;
	}
	
	
	private boolean isBusy(AID agent){
		
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(agent);
		
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		request.setLanguage(codec.getName());
		request.setOntology(ontology.getName());
		request.setReplyByDate(new Date(System.currentTimeMillis() + 200));
		
		ontology.messages.GetOptions get = new ontology.messages.GetOptions();
		Action a = new Action();
		a.setAction(get);
		a.setActor(this.getAID());
		
		try {
			// Let JADE convert from Java objects to string
			getContentManager().fillContent(request, a);
			
			ACLMessage r = FIPAService.doFipaRequestClient(this, request);
			
			if (r != null) {
				return false;
			}
		}
		catch (CodecException ce) {
			ce.printStackTrace();
		}
		catch (OntologyException oe) {
			oe.printStackTrace();
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		return true;		
	}
	
	public AID getAgentByType(String agentType){
		
		AID[] Agents;
		
		// System.out.println("getAgentByType"+agentType);
		// Make the list of agents of given type
		DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agentType);
        template.addServices(sd);
        try {
        	DFAgentDescription[] result = DFService.search(this, template); 
        	System.out.println("Found the following "+agentType+" agents:");
        	Agents = new AID[result.length];
          
          for (int i = 0; i < result.length; ++i) {
       	  Agents[i] = result[i].getName();
	          	System.out.println(Agents[i].getName());
          }

          if (Agents.length == 0){
    		  // create agent
    		  String agentName = generateName(agentType);
    		  AID a = createAgent("Agent_"+agentType, agentName);
    		  busyAgents.add(a);
    		  return a;        	  
          }
          else{
              // choose one
              // Random generator = new Random();
        	  // int rnd = generator.nextInt(Agents.length);
        	  // return Agents[rnd];
        	  int i = 0;  
        	  while ( (isBusy(Agents[i]) || busyAgents.contains(Agents[i])) && i < Agents.length-1 ){        		  
        		  i++;
        	  }
        	  if (i < Agents.length-1){
        		  busyAgents.add(Agents[i]);
        		  return Agents[i];
        	  }
        	  else{
        		  String agentName = generateName(agentType);
        		  AID a = createAgent("Agent_"+agentType, agentName);
        		  busyAgents.add(a);
        		  return a;
        	  }

          }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
            return null;
        }
	}  // end getAgentByType

	public Vector<String> offerAgentTypes(){
		// read agent types from file 	
			Vector<String> AgentTypes = new Vector<String>();
			
			//  Sets up a file reader to read the agent_types file 
			FileReader input;
			try {
				input = new FileReader(path+"agent_types");
	            // Filter FileReader through a Buffered read to read a line at a time
	            BufferedReader bufRead = new BufferedReader(input);
	            String line = bufRead.readLine();
	         
	            // Read through file one line at time
	            while (line != null){
	            	AgentTypes.add(line);
	                line = bufRead.readLine();
	            }
	            
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return AgentTypes;            
	}  // end offerAgentTypes
	
	private ontology.messages.Agent onlyGetAgentOptions(String agent) {
		
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(new AID(agent, AID.ISLOCALNAME));
		
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		request.setLanguage(codec.getName());
		request.setOntology(ontology.getName());
		
		ontology.messages.GetOptions get = new ontology.messages.GetOptions();
		Action a = new Action();
		a.setAction(get);
		a.setActor(this.getAID());
		
		try {
			// Let JADE convert from Java objects to string
			getContentManager().fillContent(request, a);
			
			ACLMessage inform = FIPAService.doFipaRequestClient(this, request);
			
			if (inform == null) {
				return null;
			}
			
			Result r = (Result)getContentManager().extractContent(inform);
			
			return (ontology.messages.Agent)r.getItems().get(0);
			
		}
		catch (CodecException ce) {
			ce.printStackTrace();
		}
		catch (OntologyException oe) {
			oe.printStackTrace();
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		return null;
		
	}
	
	public String generateName(String agentType){
		int number = 0;
		String name = agentType + number;
		boolean success = false;
		while (!success){
			// try to find an agent with "name"
			DFAgentDescription template = new DFAgentDescription();
	        ServiceDescription sd = new ServiceDescription();
	        sd.setName(name);
	        template.addServices(sd);
	        try {
	        	DFAgentDescription[] result = DFService.search(this, template);
	        	// if the agent with this name already exists, increase number
	        	if (result.length > 0){
	        		number++;
	        		name = agentType + number;
	        	}          
	        	else {
	        		success = true;
	        		return name;
	        	}
	        }
	        catch (FIPAException fe) {
	          fe.printStackTrace();
	        }
		}
		return null;		
	}

	public boolean exists(String name){
		DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName(name);
        template.addServices(sd);
        try {
        	DFAgentDescription[] result = DFService.search(this, template);
        	if (result.length > 0){
        		return true;
        	}          
        }
        catch (FIPAException fe) {
          fe.printStackTrace();
        }
        return false;
	}
	
	public AID createAgent(String type, String name){
		// get a container controller for creating new agents
		PlatformController container = getContainerController();
		
		try{	
			AgentController agent = container.createNewAgent(name, type, new Object[0]);
			agent.start();
			return new AID((String) name, AID.ISLOCALNAME);
		}
		catch (ControllerException e) {
	        // System.err.println( "Exception while adding agent: " + e );
	        // e.printStackTrace();
	        return null;
	    }	
	}
	
	protected ACLMessage Compute(Computation computation){
	// creates an Option Manager agent and returns a message for this agent

		// create an Option Manager agent
		String option_manager_name = computation.getId();
		PlatformController container = getContainerController(); // get a container controller for creating new agents
		
		try{	
			// AgentController agent = container.createNewAgent(option_manager_name, "Agent_Random", new String[0] );
			AgentController agent = container.createNewAgent(
					option_manager_name, "Agent_"+computation.getMethod().getName(), new String[0] );
			agent.start();
		}
		catch (Exception e) {
	        System.err.println( "Exception while adding agent"+computation.getId()+": " + e );
	        // TODO send it to GUI agent
	        e.printStackTrace();
	    }
	
		// create a message for the Option Manager agent
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

	
	protected Results prepareComputationResults(ACLMessage result){
		Results results = null;
		
		ContentElement content;
		try {
			content = getContentManager().extractContent(result);
			if (content instanceof Result) {
                Result _result = (Result) content;              
                if (_result.getValue() instanceof Results) {
                	results = (Results) _result.getValue(); 
                	List listOfResults = results.getResults();
                	
           		 	float sumError_rate = 0;
           			float sumKappa_statistic = 0;  
           			float sumMean_absolute_error = 0;
           			float sumRoot_mean_squared_error = 0;
           			float sumRelative_absolute_error = 0; // percent
           			float sumRoot_relative_squared_error = 0; // percent
           		 	
                	if (listOfResults == null){
                		// there were no tasks computed
                		// leave the default values
                		return null;
                	}
                	else{
	           		 	Iterator itr = listOfResults.iterator();
	           		 	while (itr.hasNext()) {
	           	            Task next = (Task) itr.next();
	           	            Evaluation evaluation;
	           	            // if the task failed
	           	            if (next.getResult() == null){	           	            	
	           	            	evaluation = new Evaluation();	           	            	
	           	            	evaluation.setError_rate(Integer.MAX_VALUE);
	           	            	next.setResult(evaluation);
	           	            }
	           	            else{
	           	            	evaluation = next.getResult();
	           	            }
	           	            
	           	            sumError_rate += evaluation.getError_rate();  // error rate is a manadatory slot	           	            
	           	            
	           	            // if the value has not been set by the CA, the sum will < 0
	           	            sumKappa_statistic += evaluation.getKappa_statistic();
							sumMean_absolute_error += evaluation.getMean_absolute_error();		
							sumRoot_mean_squared_error += evaluation.getRoot_mean_squared_error();		
							sumRelative_absolute_error += evaluation.getRelative_absolute_error();		
							sumRoot_relative_squared_error += evaluation.getRoot_relative_squared_error();		
	    				}
	           		 	if (sumError_rate > -1){
	           		 		results.setAvg_error_rate( sumError_rate / listOfResults.size() );
	           		 	}
	           		 	if (sumKappa_statistic > -1){
	           		 		results.setAvg_kappa_statistic( sumKappa_statistic / listOfResults.size() );
	           		 	}
	           		 	if (sumMean_absolute_error > -1){
	           		 		results.setAvg_mean_absolute_error( sumMean_absolute_error / listOfResults.size() );
	           		 	}
	           		 	if (sumRoot_mean_squared_error > -1){
	           		 		results.setAvg_root_mean_squared_error( sumRoot_mean_squared_error / listOfResults.size() );
	           		 	}
	           		 	if (sumRelative_absolute_error > -1){
	           		 		results.setAvg_relative_absolute_error( sumRelative_absolute_error / listOfResults.size() );
	           		 	}
	           		 	if (sumRoot_relative_squared_error > -1){
	           		 		results.setAvg_root_relative_squared_error( sumRoot_relative_squared_error / listOfResults.size() );      		 	
	           		 	}
                	}
                }
	  		}
		} catch (UngroundedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace(); return null
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace(); return null
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
  		return results;

	} // prepareComputationResult

	
	protected boolean writeXMLResults(Results results){
	 	String file_name = "xml"+System.getProperty("file.separator")+results.getComputation_id()+".xml"; 
	 	
		// create the "xml" directory, if it doesn't exist
		boolean exists = (new File("xml")).exists();
		if (!exists) {	
			boolean success = (new File("xml")).mkdir();
		    if (!success) {
		      System.out.println("Directory: " + "xml" + " could not be created");  // TODO exception
		    } 
		}
	 
		
		/* Generate the ExpML document  */
		Document doc = new Document(new Element("result"));
		Element root = doc.getRootElement();
	
		
	 	List _results = results.getResults();
	    if (_results != null){
		 	Iterator itr = _results.iterator();	  
		    while (itr.hasNext()) {
			   Task next_task = (Task) itr.next();
			   
			   ontology.messages.Agent agent = next_task.getAgent();
			   
			   Element newExperiment = new Element("experiment");				   
		       Element newSetting = new Element ("setting");
		       Element newAlgorithm = new Element ("algorithm");
		       newAlgorithm.setAttribute("name", agent.getName());
		       newAlgorithm.setAttribute("libname", "weka");
		       
			   List Options = agent.getOptions(); 
			   if (Options != null){
				   Iterator itr_o = Options.iterator();	  
				   while (itr_o.hasNext()) {
					   ontology.messages.Option next_o = (ontology.messages.Option) itr_o.next();
					    
					   	Element newParameter = new Element ("parameter");
					    newParameter.setAttribute("name", next_o.getName());
					    
					    String value = "";
					    if (next_o.getValue() != null){ value = next_o.getValue(); }
					    newParameter.setAttribute("value", value);
					    
					    newAlgorithm.addContent(newParameter);
				   }
			   }
			   Element newDataSet = new Element ("dataset");
			   newDataSet.setAttribute("train", next_task.getData().getExternal_train_file_name());
			   newDataSet.setAttribute("test", next_task.getData().getExternal_test_file_name());
	
			   Element newEvaluation = new Element ("evaluation");
			   Element newMetric1 = new Element ("metric");
			   newMetric1.setAttribute ("error_rate", getXMLValue(next_task.getResult().getError_rate()));
			   Element newMetric2 = new Element ("metric");
			   newMetric2.setAttribute ("kappa_statistic", getXMLValue(next_task.getResult().getKappa_statistic()));
			   Element newMetric3 = new Element ("metric");
			   newMetric3.setAttribute ("mean_absolute_error", getXMLValue(next_task.getResult().getMean_absolute_error()));
			   Element newMetric4 = new Element ("metric");
			   newMetric4.setAttribute ("root_mean_squared_error", getXMLValue(next_task.getResult().getRoot_mean_squared_error()));
			   Element newMetric5 = new Element ("metric");
			   newMetric5.setAttribute ("relative_absolute_error", getXMLValue(next_task.getResult().getRelative_absolute_error()));
			   Element newMetric6 = new Element ("metric");
			   newMetric6.setAttribute ("root_relative_squared_error", getXMLValue(next_task.getResult().getRoot_relative_squared_error()));
			   			   
			   newEvaluation.addContent(newMetric1);
			   newEvaluation.addContent(newMetric2);
			   newEvaluation.addContent(newMetric3);
			   newEvaluation.addContent(newMetric4);
			   newEvaluation.addContent(newMetric5);
			   newEvaluation.addContent(newMetric6);
			   
		       newExperiment.addContent(newSetting);
		       newExperiment.addContent(newEvaluation);
		       newSetting.addContent(newAlgorithm);
		       newSetting.addContent(newDataSet);
	
		       root.addContent(newExperiment);
		    }	       
	    }  

	    Element newStatistics = new Element ("statistics");
 	    Element newMetric1 = new Element ("metric");
	    newMetric1.setAttribute ("average_error_rate", getXMLValue(results.getAvg_error_rate()));
	    Element newMetric2 = new Element ("metric");
	    newMetric2.setAttribute ("average_kappa_statistic", getXMLValue(results.getAvg_kappa_statistic()));
	    Element newMetric3 = new Element ("metric");
	    newMetric3.setAttribute ("average_mean_absolute_error", getXMLValue(results.getAvg_mean_absolute_error()));
	    Element newMetric4 = new Element ("metric");
	    newMetric4.setAttribute ("average_root_mean_squared_error", getXMLValue(results.getAvg_root_mean_squared_error()));
	    Element newMetric5 = new Element ("metric");
	    newMetric5.setAttribute ("average_relative_absolute_error", getXMLValue(results.getAvg_relative_absolute_error()));
	    Element newMetric6 = new Element ("metric");
	    newMetric6.setAttribute ("average_root_relative_squared_error", getXMLValue(results.getAvg_root_relative_squared_error()));
			    
	    newStatistics.addContent(newMetric1);
	    newStatistics.addContent(newMetric2);
	    newStatistics.addContent(newMetric3);
	    newStatistics.addContent(newMetric4);
	    newStatistics.addContent(newMetric5);
	    newStatistics.addContent(newMetric6);
	    
	    root.addContent(newStatistics);
	    
	    
	    XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
       	try {
    	   FileWriter fw = new FileWriter(file_name);
    	   BufferedWriter fout = new BufferedWriter(fw);
    	
    	   out.output( root, fout );
		
    	   fout.close();
		
       	} catch (IOException e) {
    	   e.printStackTrace();
    	   return false;
       	}
	    			 
       return true;
	}  // end writeXMLResults
	 
	private String getXMLValue(float value){
		if (value < 0){
			return "NA"; 
		}
		return Double.toString(value);
	}
	
	
	private ontology.messages.Agent chooseTheBestAgent(Metadata metadata){						
		// at least name attribute in metadata has to be filled 
		boolean hasMetadata = false;
		if (metadata.getNumber_of_attributes() > -1 && 
				metadata.getNumber_of_instances() > -1){
			hasMetadata = true;
		}
		
		// choose the nearest training data
		List allMetadata = DataManagerService.getAllMetadata(this);
		
		// set the min, max instances and attributes first
		Iterator itr = allMetadata.iterator();	 		
		while (itr.hasNext()) {
	 		Metadata next_md = (Metadata) itr.next();

			// try to look up the file (-> metadata) in the database
			if (!hasMetadata){				
				if (("data"+System.getProperty("file.separator")+"files"+System.getProperty("file.separator")+next_md.getInternal_name()).equals(metadata.getInternal_name())){ 
					metadata = next_md;
					hasMetadata = true;
				}
			}
					
	 		int na = next_md.getNumber_of_attributes();
	 		if (na < minAttributes){ minAttributes = na; }
	 		if (na > maxAttributes){ maxAttributes = na; }
	 		
	 		int ni = next_md.getNumber_of_instances();	
	 		if (ni < minInstances){
	 			minInstances = ni;
	 		}
	 		if (ni > maxInstances){
	 			maxInstances = ni;
	 		}	
		}

		if (!hasMetadata){
			return null;
		}
		
		System.out.println("*********** files from the table: ");
 
		double d_best = Integer.MAX_VALUE;
		Metadata m_best = null;
		
		double d_new;
		itr = allMetadata.iterator();	 		
		while (itr.hasNext()) {
	 		Metadata next_md = (Metadata) itr.next();
	 		d_new = distance(metadata, next_md);
	 		if (next_md.getNumber_of_tasks_in_db() > 0){		 		
		 		if (d_new < d_best){
		 			d_best = d_new;
		 			m_best = next_md;
		 		}
	 		}
	 		System.out.println("    "+ next_md.getExternal_name()+	 				
	 				" d: "+d_new);
	 	}
		
		System.out.println("Nearest file: "+m_best.getExternal_name());
		String nearestInternalName = m_best.getInternal_name();

		// find the agent with the lowest error_rate
		ontology.messages.Agent agent = DataManagerService.getTheBestAgent(this, nearestInternalName);
		if (agent == null){ return null;}
		agent.setName(null); // we want only the type, since the particular agent may not any longer  exist 

		return agent;		
		
		// TODO - testing data?
	}
	
	/*
	 * Compute distance between two datasets (use metadata)
	 */
	private double distance(Metadata m1, Metadata m2){
		
		double wAttribute_type 			= 1;
		double wDefault_task 			= 1;
		double wMissing_values 			= 1;
		double wNumber_of_attributes 	= 1;
		double wNumber_of_instances		= 1;
		
		// can be null
		double dAttribute_type = dCategory(m1.getAttribute_type(), m2.getAttribute_type());
		double dDefault_task = dCategory(m1.getDefault_task(), m2.getDefault_task());
		// default false - always set
		double dMissing_values = dBoolean(m1.getMissing_values(), m2.getMissing_values());
		// mandatory attributes - always set
		double dNumber_of_attributes = d(m1.getNumber_of_attributes(), m2.getNumber_of_attributes(), minAttributes, maxAttributes);
		double dNumber_of_instances = d(m1.getNumber_of_instances(), m2.getNumber_of_instances(), minInstances, maxInstances);
		
		// System.out.println("   dNumber_of_attributes: "+dNumber_of_attributes);
		// System.out.println("   dNumber_of_instances : "+dNumber_of_instances);
		
		double distance =  
			wAttribute_type * dAttribute_type 
			+ wDefault_task * dDefault_task 
			+ wMissing_values * dMissing_values 
			+ wNumber_of_attributes * dNumber_of_attributes 
			+ wNumber_of_instances * dNumber_of_instances;
		
		return distance;
	}
	
	private double d(double v1, double v2, double min, double max){
		// map the value to the 0,1 interval; 0 - the same, 1 - the most different
		
		return Math.abs(v1 - v2) / (max - min); 
	}
	private int dCategory(String v1, String v2){
		// null considered another value
		if (v1 == null) {v1 = "null";}
		if (v2 == null) {v2 = "null";}
		
		if (v1.equals(v2)){
			return 0;
		}
		return 1;
	}
	private int dBoolean(Boolean v1, Boolean v2){
		if (v1 == v2){
			return 0;
		}
		return 1;
	}
	
	
	protected String generateProblemID(){
		Date date = new Date();
		String problem_id = Long.toString(date.getTime())+"_"+problem_i;
		problem_i++;
	    return problem_id;
	}
}
