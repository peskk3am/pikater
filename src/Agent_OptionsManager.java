import java.io.*;

import java.util.Date;
import java.util.Vector;


import ontology.messages.*;

import weka.classifiers.Evaluation;
import weka.core.Instances;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.IteratedAchieveREInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

import java.util.Date;
import java.util.Vector;

import ontology.messages.Computation;
import ontology.messages.Compute;
import ontology.messages.Data;
import ontology.messages.Execute;
import ontology.messages.MessagesOntology;
import ontology.messages.Option;
import ontology.messages.Results;
import ontology.messages.Task;


public abstract class Agent_OptionsManager extends Agent {
	 	 private Codec codec = new SLCodec();
	 	 private Ontology ontology = MessagesOntology.getInstance();
	 	 
		 private String trainFileName;
		 private String testFileName;
		 
		 private Computation receivedComputation;

		 private String receiver;
	 	 private String computation_id;
	 	 private String problem_id;
	 	 
	 	 protected float error_rate = (float) 0.3; 
	 	 protected int maximum_tries = 10;
	 	 
	 	 private int task_i = 0; // task number

	 	 private long timeout = -1; 

	 	 boolean working = false;
	 	 boolean finished = false;	 
		 protected ontology.messages.Evaluation evaluation;
	 	 protected List Options;
	 	 protected ontology.messages.Agent Agent;
	 	 	
	 	 private ACLMessage msgPrev = new ACLMessage(ACLMessage.FAILURE);
	 	 private boolean sendAgain = false;
	 	 
		 protected abstract String getAgentType();
		 protected abstract boolean finished();
		 protected abstract void generateNewOptions(ontology.messages.Evaluation result);
		 

		 private class ComputeComputation extends IteratedAchieveREInitiator{

			
			private List results = new ArrayList();
			
			public ComputeComputation(Agent a, ACLMessage request) {
				super(a, request);
				System.out.println(a.getLocalName()+": ComputeComputation behavior created.");				
			}
			
			// Since we don't know what message to send to the responder
			// when we construct this AchieveREInitiator, we redefine this 
			// method to build the request on the fly
			protected Vector prepareRequests(ACLMessage request) {
				// Klara's note: this method is called just once at the beginning of the behaviour
				// Retrieve the incoming request from the DataStore
				String incomingRequestKey = (String) ((AchieveREResponder) parent).REQUEST_KEY;
				ACLMessage incomingRequest = (ACLMessage) getDataStore().get(incomingRequestKey);
								
				// System.out.println("Agent "+getLocalName()+": Received action: "+incomingRequest.getContent()+". Preparing response.");				
				
				try {
			  		ContentElement content = getContentManager().extractContent(incomingRequest);
			  		if (((Action)content).getAction() instanceof Compute){
	                    Computation computation = (Computation)((Compute) ((Action)content).getAction()).getComputation();
	                    receivedComputation = computation;
	                    Agent = computation.getAgent();
	                    Options = Agent.getOptions();
					  	trainFileName = computation.getData().getTrain_file_name();
					  	testFileName = computation.getData().getTest_file_name();
					  	receiver = computation.getAgent().getName();
					  	computation_id = computation.getId();
					  	error_rate = computation.getMethod().getError_rate();			  	
					  	maximum_tries = computation.getMethod().getMaximum_tries();
					  	problem_id = computation.getProblem_id();
					  	if (timeout < 0){
					  		timeout = System.currentTimeMillis() + computation.getTimeout();
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
				
				
				AID responder = new AID(receiver, AID.ISLOCALNAME);
				
				// Prepare the request to forward to the responder
				System.out.println("Agent "+getLocalName()+": Forward the request to "+responder.getName());
				
				ACLMessage outgoingRequest;
				if (sendAgain){
					outgoingRequest = msgPrev;
				}
				else{
					outgoingRequest = newMessage(request);
				}
				msgPrev = outgoingRequest;
				
				/* 
				ACLMessage outgoingRequest = new ACLMessage(ACLMessage.REQUEST);
				
				outgoingRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				outgoingRequest.addReceiver(responder);
				outgoingRequest.setContent(incomingRequest.getContent());
				outgoingRequest.setReplyByDate(incomingRequest.getReplyByDate());
				*/
				// System.out.println("Agent "+getLocalName()+": outgoingRequest: "+outgoingRequest);
								
				//if (outgoingRequest.getPerformative() == ACLMessage.CANCEL){
				//	System.out.println("cancel hned na zacatku");
					// storeNotification(ACLMessage.CANCEL);
					// return null;
			 	// }
				Vector v = new Vector(1);
				v.addElement(outgoingRequest);
				return v;
				
			}
			
			protected void handleInform(ACLMessage inform, java.util.Vector nextRequests) {
				sendAgain = false;
				System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" sent a reply.");		
								
				ACLMessage msgNew = newMessage(inform); 
				nextRequests.add(msgNew);
								
				storeTask();
								
				if (finished() || finished){
					storeNotification(ACLMessage.INFORM);
				}
				msgPrev = msgNew;
				
				// prepare the result to be added to results List:
				
				// set the Evaluation					
				// ontology.messages.Evaluation evaluation = new ontology.messages.Evaluation();
				// evaluation.setError_rate((float)result.errorRate);
				// evaluation.setPct_incorrect((float)result.pctIncorrect);
												
			}
			
			protected void handleRefuse(ACLMessage refuse) {
				
				System.out.println(getLocalName()+": Agent "+refuse.getSender().getName()+" refused to perform the requested action");
				if (System.currentTimeMillis() < timeout){
					doWait(200);					
					this.reset();
					sendAgain = true;
					addBehaviour(this);
				}
				else{
					finished = true;
					storeNotification(ACLMessage.FAILURE);	
				}
			}
		 
			protected void handleFailure(ACLMessage failure) {
				sendAgain = false;
				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver
					// does not exist
					System.out.println("Responder does not exist");
					finished = true;
					storeNotification(ACLMessage.FAILURE);
				}
				else {
					System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");					
					
					ACLMessage msgNew = newMessage(failure); 
					
					Vector v = new Vector(1);
					v.addElement(msgNew);
					
					String requestsKey = (String) (this).ALL_NEXT_REQUESTS_KEY;
					getDataStore().put(requestsKey, v);
					
					storeTask();
									
					if (finished() || finished){
						storeNotification(ACLMessage.INFORM);
					}
					msgPrev = msgNew;
				}																	
			}

			private void storeTask(){
				// get the Task from the last message						
				try {
			  		ContentElement content = getContentManager().extractContent(msgPrev);
			  		if (((Action)content).getAction() instanceof Execute){
	
			  			Task task = ( (Execute) ((Action)content).getAction() ).getTask();
			  			task.setResult(evaluation);
				  		results.add(task);			  		
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
							
			}
			
			
			private void storeNotification(int performative) {
				
				if (performative == ACLMessage.INFORM) {			
					System.out.println("Agent "+getLocalName()+": computation executed successfully");
				}
				else { 						
					if (performative == ACLMessage.CANCEL){
						// there were no tasks to compute => send inform message					
						System.out.println("Agent "+getLocalName()+": there were no tasks to compute.");
					}
					else{						
						System.out.println("Agent "+getLocalName()+": computation failed");
					}
					performative = ACLMessage.FAILURE;
				}
					
				// Retrieve the incoming request from the DataStore

				String incomingRequestkey = (String) ((AchieveREResponder) parent).REQUEST_KEY;
				ACLMessage incomingRequest = (ACLMessage) getDataStore().get(incomingRequestkey);
				// System.out.println("Agent "+getLocalName()+"incomingRequestkey: "+incomingRequestkey);
				
				/*
				// Prepare the notification to the request originator and store it in the DataStore
				ACLMessage notification = incomingRequest.createReply();
				notification.setPerformative(performative);
				String notificationkey = (String) ((AchieveREResponder) parent).RESULT_NOTIFICATION_KEY;
				getDataStore().put(notificationkey, notification);
				*/

		
				ACLMessage msgOut = incomingRequest.createReply();
				msgOut.setPerformative(performative);
				
				
				if (finished() || finished){					
					// String incomingReplykey = (String) this.REPLY_KEY;
					// ACLMessage incomingReply = (ACLMessage) getDataStore().get(incomingReplykey);
					
					System.out.println("Agent "+getLocalName()+" finished the goal, sending the results to the manager.");
				
					// prepare the outgoing message content:
					
					Results _results = new Results();
					_results.setResults(results);
					_results.setComputation_id(computation_id);
					_results.setProblem_id(problem_id);									
					

				   ContentElement content;
					try {
						content = getContentManager().extractContent(incomingRequest);
						Result result = new Result((Action)content, _results);
						getContentManager().fillContent(msgOut, result);
	
					
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
					
				} // end if (finished())	

				// save the outgoing message to the dataStore
				String notificationkey = (String) ((AchieveREResponder) parent).RESULT_NOTIFICATION_KEY;
				getDataStore().put(notificationkey, msgOut);
		
			}   // end storeNotification
		
			 ACLMessage newMessage(ACLMessage _result){
				 ACLMessage msg;
				 if (_result != null){
					 if (_result.getPerformative() != ACLMessage.FAILURE){					 					
						 ContentElement content;
							try {
								content = getContentManager().extractContent(_result);
								// System.out.println(getLocalName()+": Action: "+((Result)content).getAction());
								if (content instanceof Result) {
					                Result result = (Result) content;
					                
					                if (result.getValue() instanceof ontology.messages.Evaluation) {
					                	evaluation = (ontology.messages.Evaluation)result.getValue();
					                }
						  		}
							} catch (CodecException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OntologyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	 
						 System.out.println(getLocalName()+": Agent "+_result.getSender().getLocalName()+"'s errorRate was "+evaluation.getError_rate());
					 }
				 }				 
				 // System.out.println(getLocalName()+": error_rate "+error_rate+" maximum tries "+maximum_tries);
				 
				 if (!(finished || finished())){
					 
					if (Options != null){
						generateNewOptions(evaluation);
					}
					Agent.setOptions(Options);
					System.out.println(getLocalName()+": new options for agent "+receiver+" are "
							+Agent.optionsToString() ); 
					 
					msg = new ACLMessage(ACLMessage.REQUEST);
					msg.setLanguage(codec.getName());
					msg.setOntology(ontology.getName());
					msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
					msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					// We want to receive a reply in 30 secs
					msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));			
					
					Execute execute = new Execute();
					
					Task task = new Task();
					String id = computation_id+"_"+task_i;
					task_i++;
					task.setId(id);
					task.setComputation_id(computation_id);  // TODO vzit z receivedComputation
					task.setProblem_id(problem_id);
					// task.setOptions(opt);

					// Data data = new Data();
					// data.setTrain_file_name(trainFileName);
					// data.setTest_file_name(testFileName);
					
					// task.setData(data);
					task.setData(receivedComputation.getData());				
					task.setAgent(Agent);
					
					execute.setTask(task);
					
					
	  				Action a = new Action();
	   				a.setAction(execute);
	   				a.setActor(myAgent.getAID());
	   		  		
	   		  		try {
						getContentManager().fillContent(msg, a);
					} catch (CodecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OntologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					
					if (Options == null || noMutableOptions()){
						finished = true;
					}
					
				 }
				 else{
					msg = new ACLMessage(ACLMessage.CANCEL);
				 }
				 				 
				 return msg;				

			 } // newMessage

			
			
			
		 }  // end class ComputeComputation
		 
		
		 
		 protected boolean registerWithDF(){
	         //register with the DF
	         
	         DFAgentDescription description = new DFAgentDescription();
	         // the description is the root description for each agent 
	         // and how we prefer to communicate. 
	         
	         description.setName(getAID());     
	         // the service description describes a particular service we
	         // provide.
	         ServiceDescription servicedesc = new ServiceDescription();
	         //the name of the service provided (we just re-use our agent name)
	         servicedesc.setName(getLocalName());
	         
	         //The service type should be a unique string associated with
	         //the service.s
	         String typeDesc = getAgentType();
	         
	         servicedesc.setType(typeDesc); 

	         //the service has a list of supported languages, ontologies
	         //and protocols for this service.
	         // servicedesc.addLanguages(language.getName());
	         // servicedesc.addOntologies(ontology.getName());
	         // servicedesc.addProtocols(InteractionProtocol.FIPA_REQUEST);
	         
	         description.addServices(servicedesc);


	         // add "OptionsManager agent service"
	         ServiceDescription servicedesc_g = new ServiceDescription();

	         servicedesc_g.setName(getLocalName());
	         servicedesc_g.setType("OptionsManager"); 
	         description.addServices(servicedesc_g);
	         
	         
	         //register synchronously registers us with the DF, we may
	         //prefer to do this asynchronously using a behaviour.
	         try {
	                 DFService.register(this,description);
	                 System.out.println(getLocalName() + ": successfully registered with DF; service type: "+typeDesc);
	                 return true;
	         }catch(FIPAException e){
	                 System.err.println(getLocalName() + ": error registering with DF, exiting:" + e);
	                 // doDelete();
	                 return false;
	                 
	         }
		 }  // end registerWithDF
				 
		 		 
		 
		 protected void setup() {
			
			
		  	System.out.println(getLocalName()+" is alive...");
		  	
		  	getContentManager().registerLanguage(codec);
			getContentManager().registerOntology(ontology);

			registerWithDF();
			
			
  		  	MessageTemplate template_inform = MessageTemplate.and(
	  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
	  		MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
	  		  		
  		    AchieveREResponder receive_computation = new AchieveREResponder(this, template_inform) {
				protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
					System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+".");
						
						// We agree to perform the action. Note that in the FIPA-Request
						// protocol the AGREE message is optional. Return null if you
						// don't want to send it.						
						
						System.out.println("Agent "+getLocalName()+": Agree");
						ACLMessage agree = request.createReply();
						agree.setPerformative(ACLMessage.AGREE);
						return agree;
						
				}  // end prepareResponse
								
			};
						
			receive_computation.registerPrepareResultNotification( new ComputeComputation(this, null) );
			
			addBehaviour(receive_computation);
			
		 } // end setup

		 private boolean noMutableOptions(){
			Iterator itr = Options.iterator();	 		   		 
   		 	while (itr.hasNext()) {
   		 		Option next_option = (Option) itr.next();
   		 		if (next_option.getMutable()){
   		 			return false;
   		 		}
   		 	}
   		 	return true;
		 }
		 
		 private String getImmutableOptions(){
			String str = ""; 
			Iterator itr = Options.iterator();	 		   		 
   		 	while (itr.hasNext()) {
   		 		Option next_option = (Option) itr.next();
   		 		if (!next_option.getMutable() && next_option.getValue() != null){
   		 			if (next_option.getData_type().equals("BOOLEAN") && next_option.getValue().equals("True")){
   		 				str += "-"+next_option.getName()+" ";
   		 			}
   		 			else{
   		 				str += "-"+next_option.getName()+" "+next_option.getValue()+" ";
   		 			}
   		 		}
   		 	}
   		 	return str;
		 }
		 
}