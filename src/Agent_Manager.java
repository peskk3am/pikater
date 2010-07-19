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
				System.out.println(a.getLocalName()+": SendComputation behavior created. "+request);				
			}
			
			// Since we don't know what message to send to the responder
			// when we construct this AchieveREInitiator, we redefine this 
			// method to build the request on the fly
			protected Vector prepareRequests(ACLMessage request) {
				// Klara's note: this method is called just once at the beginning of the behaviour
				System.out.println("Agent "+getLocalName()+": Received action: "+incomingRequest.getContent()+". Preparing response.");
				
				// get generated problem id from agree message (it contains a string: "gui_id and id" of a problem 
				String[] ID = incomingResponse.getContent().split(" ");
				String problemId = ID[1];
			
				return prepareComputations(incomingRequest, problemId, failure); // Prepare the request to forward to the responder										
			}
			
			protected void handleInform(ACLMessage inform) {
				System.out.println("Agent:"+getLocalName()+": Agent "+inform.getSender().getName()+" sent an inform.");
				sendSubscription(inform);
				killAgent(inform.getSender().getName());
			}
			
			protected void handleFailure(ACLMessage failure) {
				System.out.println("Agent:"+getLocalName()+": Agent "+failure.getSender().getName()+" sent a failure.");
				// if (System.currentTimeMillis() < timeout){
					// this.reset();
				//	this.failure = failure;
				//	addBehaviour(this);
				//}
				//else{
				//	sendSubscription(failure);
					// killAgent(failure.getSender().getName());
				//}
				
				sendSubscription(failure);
				killAgent(failure.getSender().getName());
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
				System.out.println("Agent: "+getLocalName()+": result: "+result+" "+result.getPerformative());
								
				// Prepare the msgOut to the request originator				
				ACLMessage msgOut = incomingRequest.createReply();
				msgOut.setPerformative(result.getPerformative());
				
				String problemGuiId = null;
				if (result.getPerformative() != ACLMessage.FAILURE){

					// fill its content
					Results results = prepareComputationResults(result);
					if (results != null){
						
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
					}
				}  // end if				
		
				// go through every subscription				
				java.util.Iterator it = subscriptions.iterator();
				while(it.hasNext()){
					Subscription subscription = (Subscription)it.next();
					
					if (subscription.getMessage().getConversationId().equals("subscription"+incomingRequest.getConversationId())){
						subscription.notify(msgOut);
					}
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
		  		System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());

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
	
	protected Vector<ACLMessage> prepareComputations(ACLMessage request, String problemId,
			ACLMessage failure){
		Vector<ACLMessage> msgVector = new Vector<ACLMessage>();		
		
		System.out.println("Agent "+getLocalName()+" failure :"+failure);
		
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
	       		 	Iterator a_itr = problem.getAgents().iterator();	 
	            	while (a_itr.hasNext()) {
	    	           ontology.messages.Agent a_next = (ontology.messages.Agent) a_itr.next();
	    	           
	    	           Iterator d_itr = problem.getData().iterator();	 
	    	           while (d_itr.hasNext()) {
	    	        	   Data next_data = (Data) d_itr.next();
	    	        	   
	    	        	   if (a_next.getName() == null){
	    	        		   String agentType = a_next.getType();	
	    	        		   boolean unknownAgentType = false;
	    	        		   if (a_next.getType().contains("?")){
	    	        			   unknownAgentType = true;
	    	        			   agentType = _test_recommendRBFAgentType(next_data, offerAgentTypes());
	    	        			   a_next.setType(agentType);	    	        			   
	    	        		   }
	    	        		   AID aid = null;
	    	        		   String agentName = null;		    	      
	    	        		  	    	        		   
	    	        		   while (aid == null) { // TODO && System.currentTimeMillis() < timeout){
		    	    				// try until you find agent of the given type or you manage to create it	    	        			    
	    	        			    aid = getAgentByType(agentType);
		    	    				if (aid == null){
		    	    					// agent of given type doesn't exist
		    	    					agentName = generateName(agentType);
		    	    					aid = createAgent("Agent_"+agentType, agentName);
		    	    					doWait(100);
		    	    				}
		    	    			}
	    	        		   if (aid == null){
	    	        			   // TODO ! this computation failed
	    	        		   }
	    	        		   agentName = aid.getLocalName();
	    	        		   a_next.setName(agentName);
	    	        		   
	    	        		   if (unknownAgentType){
	    	        			   ontology.messages.Agent agent_options = onlyGetAgentOptions(agentName);
	    	        			   a_next.setOptions(agent_options.getOptions());
	    	        		   }
	    	        		   
	    	        		   System.out.println("********** Agent "+a_next.getName()+" recommended. **********");
	    	        	   }
	    	        	   
	    	        	   Computation computation = new Computation();
	    	        	   computation.setAgent(a_next);
	    	        	   computation.setData(next_data);
	    	        	   computation.setProblem_id(problemId);
	    	        	   computation.setId(problemId+"_"+computation_i);
	    	        	   computation.setTimeout(problem.getTimeout());
	    	        	   computation.setMethod(problem.getMethod());
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
			
	} // end prepareComputations
	
	private String recommendRandomAgentType(Data dataset, Vector<String> agents){
		Random generator = new Random();
		int rnd = generator.nextInt(agents.size());
		return agents.elementAt(rnd);
	}

	private String _test_recommendRBFAgentType(Data dataset, Vector<String> agents){
		return "RBFNetwork";
	}
	
	public AID getAgentByType(String agentType){
		AID[] Agents;
		
		System.out.println(agentType);
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

          if (Agents.length > 0){
              // choose one
              Random generator = new Random();
       	   int rnd = generator.nextInt(Agents.length);
	           return Agents[rnd];
          }
          else {
       	   return null;
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
		// System.out.println("Agent "+getLocalName()+": rrrresult:"+result.getContent());
		ContentElement content;
		try {
			content = getContentManager().extractContent(result);
			if (content instanceof Result) {
                Result _result = (Result) content;              
                if (_result.getValue() instanceof Results) {
                	results = (Results) _result.getValue(); 
                	List listOfResults = results.getResults();
                	
           		 	float sumError_rate = Integer.MAX_VALUE;
           		 	float sumPct_incorrect = 100;
                	if (listOfResults == null){
                		// there were no tasks computed
                		results.setAvg_error_rate(sumError_rate); 
	           		 	results.setAvg_pct_incorrect(sumPct_incorrect);
                	}
                	else{
	           		 	Iterator itr = listOfResults.iterator();
	           		 	while (itr.hasNext()) {
	           	           Task next = (Task) itr.next();
	           	           Evaluation evaluation = next.getResult();
	           	           
	           	           sumError_rate += evaluation.getError_rate();		
	           	           sumPct_incorrect += evaluation.getPct_incorrect();
	    				}
	           		 	results.setAvg_error_rate( sumError_rate / listOfResults.size() ); 
	           		 	results.setAvg_pct_incorrect( sumPct_incorrect / listOfResults.size() );
                	}
                }
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
			   newDataSet.setAttribute("train", next_task.getData().getTrain_file_name());
			   newDataSet.setAttribute("test", next_task.getData().getTest_file_name());
	
			   Element newEvaluation = new Element ("evaluation");
			   Element newMetric1 = new Element ("metric");
			   newMetric1.setAttribute ("mean_absolute_error", Double.toString(next_task.getResult().getError_rate()));
			   Element newMetric2 = new Element ("metric");
			   newMetric2.setAttribute ("root_mean_squared_error", Double.toString(next_task.getResult().getPct_incorrect()));
			   			   
			   newEvaluation.addContent(newMetric1);
			   newEvaluation.addContent(newMetric2);
			   
		       newExperiment.addContent(newSetting);
		       newExperiment.addContent(newEvaluation);
		       newSetting.addContent(newAlgorithm);
		       newSetting.addContent(newDataSet);
	
		       root.addContent(newExperiment);
		    }	       
	    }  

	    Element newStatistics = new Element ("statistics");
 	    Element newMetric1 = new Element ("metric");
	    newMetric1.setAttribute ("average_error_rate", Double.toString(results.getAvg_error_rate()));
	    Element newMetric2 = new Element ("metric");
	    newMetric2.setAttribute ("average_pct_incorrect", Double.toString(results.getAvg_pct_incorrect()));
		
	    newStatistics.addContent(newMetric1);
	    newStatistics.addContent(newMetric2);
	    
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
	 
	
	protected String generateProblemID(){
		Date date = new Date();
		String problem_id = Long.toString(date.getTime())+"_"+problem_i;
		problem_i++;
	    return problem_id;
	}
}
