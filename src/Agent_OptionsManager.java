import java.io.*;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ontology.messages.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

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
	import jade.core.Agent;
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
import jade.proto.AchieveREResponder;
	import jade.proto.ContractNetResponder;
import jade.proto.IteratedAchieveREInitiator;
	import jade.core.AID;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SimpleBehaviour;
import jade.proto.AchieveREInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;


public abstract class Agent_OptionsManager extends Agent {
	 	 private Codec codec = new SLCodec();
	 	 private Ontology ontology = MessagesOntology.getInstance();
	
		 private String fileName;
		 private String receiver;
	 	 private String computation_id;
	 	 private String problem_id;
	 	 
	 	 private int task_i = 0; // task number

	 	 boolean working = false;
	 	 	 	 
		 MyWekaEvaluation result;
	 	 protected List Options;
	 	 
		 protected abstract String getAgentType();
		 protected abstract boolean finished();
		 protected abstract String generateNewOptions(MyWekaEvaluation result);
		 

		 private class ComputeComputation extends IteratedAchieveREInitiator{

			private ACLMessage msgPrev = new ACLMessage(ACLMessage.FAILURE);;
			private List results = new ArrayList();
			
			public ComputeComputation(Agent a, ACLMessage request) {
				super(a, request);
				System.out.println(a.getLocalName()+": ComputeComputation behavior created; "+request);				
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
				
				
				try {
			  		ContentElement content = getContentManager().extractContent(incomingRequest);
			  		if (((Action)content).getAction() instanceof Compute){
	                    Compute compute = (Compute) ((Action)content).getAction();
	                    Options = compute.getComputation().getAgent().getOptions();
					  	fileName = compute.getComputation().getData_file_name();
					  	receiver = compute.getComputation().getAgent().getName();
					  	computation_id = compute.getComputation().getId();
					  	problem_id = compute.getComputation().getProblem_id();
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
				
				ACLMessage outgoingRequest = newMessage(request);
				msgPrev = outgoingRequest;
				
				/* 
				ACLMessage outgoingRequest = new ACLMessage(ACLMessage.REQUEST);
				
				outgoingRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				outgoingRequest.addReceiver(responder);
				outgoingRequest.setContent(incomingRequest.getContent());
				outgoingRequest.setReplyByDate(incomingRequest.getReplyByDate());
				*/
				System.out.println("Agent "+getLocalName()+": outgoingRequest: "+outgoingRequest);
								
				
				Vector v = new Vector(1);
				v.addElement(outgoingRequest);
				return v;
				
			}
			
			protected void handleInform(ACLMessage inform, java.util.Vector nextRequests) {
				
				System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" sent a reply.");		
								
				ACLMessage msgNew = newMessage(inform); 
				nextRequests.add(msgNew);
								
				
				
				// prepare the result to be added to results List:
				
				// set the Evaluation					
				ontology.messages.Evaluation evaluation = new ontology.messages.Evaluation();
				evaluation.setError_rate((float)result.errorRate);
				evaluation.setPct_incorrect((float)result.pctIncorrect);
				
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
				
				
				if (finished()){
					storeNotification(ACLMessage.INFORM);
				}
				
				msgPrev = msgNew; 
				
			}
			
			protected void handleRefuse(ACLMessage refuse) {
				storeNotification(ACLMessage.FAILURE);
				System.out.println(getLocalName()+": Agent "+refuse.getSender().getName()+" refused to perform the requested action");
			}
			
			protected void handleFailure(ACLMessage failure) {
				storeNotification(ACLMessage.FAILURE);
				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver
					// does not exist
					System.out.println("Responder does not exist");
				}
				else {
					System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
				}
			}
			
			private void storeNotification(int performative) {
				
				if (performative == ACLMessage.INFORM) {			
					System.out.println("Agent "+getLocalName()+": computation executed successfully");
				}
				else { 
				
					System.out.println("Agent "+getLocalName()+": computation failed");
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
				
				
				if (finished()){
					String incomingReplykey = (String) this.REPLY_KEY;
					ACLMessage incomingReply = (ACLMessage) getDataStore().get(incomingReplykey);   // TODO incomingReply ~ MyWekaEvaluation -> change to ontology Evaluation

					
					System.out.println("Agent "+getLocalName()+" finished the goal succesfully, sending the results to the manager.");
				
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
								
				// System.out.println("Agent "+getLocalName()+" says good bye!");
				// doDelete();
				
		
			}   // end storeNotification
		
			 ACLMessage newMessage(ACLMessage _result){
				 
				 ACLMessage msg;
				 if (_result != null){
					 try {
						result = (MyWekaEvaluation) _result.getContentObject();
					 } catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					 }
					 System.out.println(getLocalName()+": Agent "+_result.getSender().getLocalName()+"'s errorRate was "+result.errorRate);
				 }
				 
				 
				 
				 
				 if (!finished()){
					String opt = generateNewOptions(result);
					System.out.println(getLocalName()+": new options for agent "+receiver+" are "+opt); 
					 
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
					task.setComputation_id(computation_id);
					task.setProblem_id(problem_id);
					task.setOptions(fileName+" "+opt);
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

					// msg.setContent(fileName+" "+opt);
					
				 }
				 else{
					msg = new ACLMessage(ACLMessage.CANCEL);
					// write the results to a file
					boolean exists = (new File("xml")).exists();
					if (!exists) {	
						boolean success = (new File("xml")).mkdir();
					    if (!success) {
					      System.out.println("Directory: " + "xml" + " could not be created");  // TODO exception
					    } 
					}
					
					writeResult("xml"+System.getProperty("file.separator")+computation_id+".xml", receiver);
					
					
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
		
		 
		 
		 
		 protected boolean writeResult(String file_name, String agent){
			 
			 
			 	/* Generate the ExpML document  */
			 	Document doc = new Document(new Element("experiment"));
			 	Element root = doc.getRootElement();
			
					   
		       Element newSetting = new Element ("setting");
		       Element newAlgorithm = new Element ("algorithm");
		       newAlgorithm.setAttribute("name", agent);
		       newAlgorithm.setAttribute("libname", "weka");
		       
			    
			   Iterator itr = Options.iterator();	  
			   while (itr.hasNext()) {
				   	Option next = (Option) itr.next();
				    
				   	Element newParameter = new Element ("parameter");
				    newParameter.setAttribute("name", next.getName());
				    
				    String value = "";
				    if (next.getValue() != null){ value = next.getValue(); }
				    newParameter.setAttribute("value", value);
				    
				    newAlgorithm.addContent(newParameter);
			   }
			   
			   Element newDataSet = new Element ("dataset");
			   newDataSet.setAttribute("name", fileName);

			   Element newEvaluation = new Element ("evaluation");
			   Element newMetric1 = new Element ("metric");
			   newMetric1.setAttribute ("mean_absolute_error", Double.toString(result.errorRate));
			   Element newMetric2 = new Element ("metric");
			   newMetric2.setAttribute ("root_mean_squared_error", Double.toString(result.pctIncorrect));
			   			   
			   newEvaluation.addContent(newMetric1);
			   newEvaluation.addContent(newMetric2);
			   
		       root.addContent(newSetting);
		       root.addContent(newEvaluation);
		       newSetting.addContent(newAlgorithm);
		       newSetting.addContent(newDataSet);
		      
		       
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
		 }
		 
		 		 
		 
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
				
				public int onEnd(){
					System.out.println("Agent "+getLocalName()+" says good-bye.");
					myAgent.doDelete();
					// return super.onEnd();
					return 1;
				}
								
			};
						
			receive_computation.registerPrepareResultNotification( new ComputeComputation(this, null) );
			
			addBehaviour(receive_computation);
			
			
	 

		 
		 } // end setup

}
