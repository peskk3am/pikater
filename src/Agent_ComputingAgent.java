
//import java.io.BufferedReader;
import java.util.*;

import jade.util.leap.List;
import weka.core.Instances;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;

import ontology.messages.*;

import jade.content.lang.Codec;
import jade.content.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.*;

public abstract class Agent_ComputingAgent extends Agent{
	 private Codec codec = new SLCodec();
	 private Ontology ontology = MessagesOntology.getInstance();

	 public enum states {
		    NEW, TRAINED 
	 }
	 	 
	 
	 /* common properties for all computing agents */
	 public String trainFileName;
	 public String testFileName;

	 public states state = states.NEW;
	 public boolean hasGotRightData = false;
	 
	 //protected Vector<MyWekaOption> Options;
	 protected ontology.messages.Agent agent_options = null;
	 
	 protected Instances data; // data read from fileName file
	 Instances train; 
	 DataInstances onto_train;
	 Instances test;
	 DataInstances onto_test;
	 int convId = 0;

	 protected String[] OPTIONS;
	 protected ontology.messages.Task current_task = null;
	 //protected String[] OPTIONS_;
	 protected String[] OPTIONS_ARGS;
	 
	 protected Object[] args;
	 
	 	 
	 boolean working = false;  // TODO -> state?
	
	 protected abstract void train() throws Exception;
	 protected abstract ontology.messages.Evaluation evaluateCA();
	 protected abstract DataInstances getPredictions(Instances test, DataInstances onto_test);
	 
	 public abstract String getAgentType();
	 

	 public abstract boolean saveAgent();
	 public abstract boolean loadAgent(String agentName);

	 protected abstract void getParameters();

	 protected boolean registerWithDF(){
         //register with the DF
         
         DFAgentDescription description = new DFAgentDescription();
         // the description is the root description for each agent 
         // and how we prefer to communicate. 
         // description.addLanguages(language.getName());
         // description.addOntologies(ontology.getName());
         // description.addProtocols(InteractionProtocol.FIPA_REQUEST);
         description.setName(getAID());
         
         // the service description describes a particular service we
         // provide.
         ServiceDescription servicedesc = new ServiceDescription();
         //the name of the service provided (we just re-use our agent name)
         servicedesc.setName(getLocalName());
         
         //The service type should be a unique string associated with
         //the service.s
         String typeDesc;
         if (state == states.TRAINED){ // add fileName to service description
        	 typeDesc = getAgentType() +" trained on "+trainFileName;
         }
         else{
        	 typeDesc = getAgentType();
         }
         servicedesc.setType(typeDesc); 

         //the service has a list of supported languages, ontologies
         //and protocols for this service.
         // servicedesc.addLanguages(language.getName());
         // servicedesc.addOntologies(ontology.getName());
         // servicedesc.addProtocols(InteractionProtocol.FIPA_REQUEST);
         
         description.addServices(servicedesc);


         // add "computing agent service"
         ServiceDescription servicedesc_g = new ServiceDescription();

         servicedesc_g.setName(getLocalName());
         servicedesc_g.setType("ComputingAgent"); 
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
	 
	 
	 protected void deregisterWithDF(){
		   try {
			     DFService.deregister(this);
		   } catch (FIPAException e) {
				   System.err.println(getLocalName() + " failed to deregister with DF.");
			       // doDelete();
		   }  
	 }  // end deregisterWithDF
	 
	 
	 protected ACLMessage sendOptions(ACLMessage request){
		 ACLMessage msgOut = request.createReply();
		 msgOut.setPerformative(ACLMessage.INFORM);
		 try {
			 // Prepare the content
			 ContentElement content = getContentManager().extractContent(request); // TODO exception block?
			 Result result = new Result((Action)content, agent_options);
			 // result.setValue(options);	

			 try {
				 // Let JADE convert from Java objects to string
				 getContentManager().fillContent(msgOut, result);
				 // send(msgOut);
			 }
			 catch (CodecException ce) {
				 ce.printStackTrace();
			 }
			 catch (OntologyException oe) {
				 oe.printStackTrace();
			 }

		 } catch (Exception e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }

		 return msgOut;

	 }  // end SendOptions
	 
	 
	 protected void setup() {		 
   
		 
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
			
	  	System.out.println(getAgentType()+" "+getLocalName()+" is alive...");
	  	
	  		  	
		args = getArguments();

		OPTIONS_ARGS = new String[args.length];
		
		if (args != null && args.length > 0) {
		    if (args[0].equals("load")){
		    	loadAgent(getLocalName());
		    	args = new String[0];
		    }
		    else{
			
				// parameters of the network
				for (int i=0; i < args.length; i++){
					OPTIONS_ARGS[i] = (String) args[i];
				}
				
				// write out parameters
				for (String s : OPTIONS_ARGS) {
					System.out.print(s+" ");
				}
					
		    }
		}
		 
		registerWithDF();

		getParameters();
		 			
			                
	  		  	MessageTemplate template_inform = MessageTemplate.and(
	  		  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
	  		  		MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
	  		  				MessageTemplate.and(MessageTemplate.MatchLanguage(codec.getName()), MessageTemplate.MatchOntology(ontology.getName()))
	  		  				)
	  		  	);


	  		  		
	  			AchieveREResponder resp =	
	  		  	new AchieveREResponder(this, template_inform) {
	  					protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
	  						System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()); // +". Action is "+request.getContent());
	  						if (!working) {
	  							// We agree to perform the action. Note that in the FIPA-Request
	  							// protocol the AGREE message is optional. Return null if you
	  							// don't want to send it.						
	  							
	  							System.out.println("Agent "+getLocalName()+": Agree");
	  							ACLMessage agree = request.createReply();
	  							agree.setPerformative(ACLMessage.AGREE);
	  							return agree;
	  						}
	  						else {
	  							// We refuse to perform the action
	  							System.out.println("Agent "+getLocalName()+": Refuse");
	  							throw new RefuseException("check-failed");
	  						}
	  					}  // end prepareResponse
	  				} ;
	  			addBehaviour(resp);
	  			resp.registerPrepareResultNotification( new ProcessAction(this) );

	 
	 } // end setup
	 
	 
	 
	 public boolean setOptions(ontology.messages.Task task){
		  /* INPUT: task with weka options
		   * Fills the OPTIONS array and current_task.
		   */
		 current_task = task;
		 OPTIONS = task.getAgent().optionsToString().split("[ ]+");		 		    
		 
		 return true;
	 }  // end loadConfiguration
	 
	 public String getOptions(){
		// write out OPTIONS

		String strOPTIONS = ""; 
	    strOPTIONS += "OPTIONS:";
		for (int i=0; i<OPTIONS.length; i++){
			strOPTIONS += " "+OPTIONS[i];
		}
		return strOPTIONS;
	 }
 
	 
	 protected ACLMessage sendGetDataReq(String fileName){ 
		 AID[] ARFFReaders;
		 AID reader;
		 ACLMessage msgOut = null;
		 // Make the list of reader agents
		 DFAgentDescription template = new DFAgentDescription();
		 ServiceDescription sd = new ServiceDescription();
		 sd.setType("ARFFReader");
		 template.addServices(sd);
		 try {
			 DFAgentDescription[] result = DFService.search(this, template); 
			 System.out.println("Found the following ARFFReader agents:");
			 ARFFReaders = new AID[result.length];
			 for (int i = 0; i < result.length; ++i) {
				 ARFFReaders[i] = result[i].getName();
				 System.out.println(ARFFReaders[i].getName());
			 }
			 // choose one
			 reader = ARFFReaders[0];
			 //request
			 msgOut = new ACLMessage(ACLMessage.REQUEST);
			 msgOut.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			 msgOut.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
			 msgOut.setLanguage(codec.getName());
			 msgOut.setOntology(ontology.getName());
			 msgOut.addReceiver(reader);
			 msgOut.setConversationId("get-data_"+convId++);
			 //content
			 GetData get_data = new GetData();
			 get_data.setFile_name(fileName);
			 Action a = new Action();
			 a.setAction(get_data);
			 a.setActor(this.getAID());
			 getContentManager().fillContent(msgOut, a);
		 }
		 catch (FIPAException fe) {
			 fe.printStackTrace();
			 return null;
		 } catch (CodecException e) {
			e.printStackTrace();
			return null;
		} catch (OntologyException e) {
			e.printStackTrace();
			return null;
		}
		 return msgOut;
	 } // end sendGetDataReq
	 
	 public static byte[] toBytes(Object object) throws Exception{
		 java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		 java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
		 oos.writeObject(object);

		 return baos.toByteArray();
	 }
	  
	  
	  public static Object toObject(byte [] data) throws Exception{

	      Object object = new java.io.ObjectInputStream(new
	    		  java.io.ByteArrayInputStream(data)).readObject();
	      return object;
	  }
	  
	  
	    /* Send partial results to the GUI Agent(s)
	     * call it after training or during training?*/
	    protected void sendResultsToGUI(Boolean first_time, Task _task, List _evaluations){
	    	ACLMessage msgOut = new ACLMessage(ACLMessage.INFORM);
	    	DFAgentDescription template = new DFAgentDescription();
	    	ServiceDescription sd = new ServiceDescription();
	    	sd.setType("GUIAgent");
	        template.addServices(sd);
	        try {
	        	DFAgentDescription[] gui_agents = DFService.search(this, template); 
	            for (int i = 0; i < gui_agents.length; ++i) {
	            	msgOut.addReceiver(gui_agents[i].getName());
	            }
	        }catch (FIPAException fe) {
	            fe.printStackTrace();
	        }

	        msgOut.setConversationId("partial-results");
	       
	        PartialResults content= new PartialResults();
	        content.setResults(_evaluations);
	        content.setTask_id(_task.getId());
	        if(first_time){
	        	content.setTask(_task);
	        }
	        try {
				getContentManager().fillContent(msgOut, content);
			} catch (CodecException e) {
				e.printStackTrace();
			} catch (OntologyException e) {
				e.printStackTrace();
			}

	        send(msgOut);
	    }
	    private class ProcessAction extends FSMBehaviour{
	    	private static final String INIT_STATE = "Init";
	    	private static final String GETTRAINDATA_STATE = "GetTrainingData";
	    	private static final String GETTESTDATA_STATE = "GetTestData";
	    	private static final String TRAINTEST_STATE = "TrainTest";
	    	private static final String SENDRESULTS_STATE = "SendResults";
	    	private static final int NEXT_JMP = 0;
	    	private static final int LAST_JMP = 1;
	    	ACLMessage incoming_request;
	    	ACLMessage result_msg;
	    	Execute execute_action;
	    	boolean success;
	    	ontology.messages.Evaluation eval;
	    	String train_fn;
	    	String test_fn;
	    	String output;
	    	String mode;
	    	
	    	void failureMsg(String desc){
	    		result_msg = incoming_request.createReply();
	    		result_msg.setPerformative(ACLMessage.FAILURE);
	    		result_msg.setContent(desc);	    		
	    	}

	    	void notUnderstoodMsg(){
	    		result_msg = incoming_request.createReply();
	    		result_msg.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	    		//TODO: add the description
	    	}
	    	
	    	void getRequest(){
	    		String incomingRequestKey = (String) ((AchieveREResponder) parent).REQUEST_KEY;
				incoming_request = (ACLMessage) getDataStore().get(incomingRequestKey);
	    	}
	    	
	    	void setResultMsg(){
	    		String notificationkey = (String) ((AchieveREResponder) parent).RESULT_NOTIFICATION_KEY;
				getDataStore().put(notificationkey, result_msg );				
	    	}
	    	
	    	boolean processNonExecute(){
	    		try{
	    			ContentElement content = getContentManager().extractContent(incoming_request);
	    			if (((Action)content).getAction() instanceof GetOptions){
	    				result_msg = sendOptions(incoming_request);
	    				return true;
	    			}
	    			if (((Action)content).getAction() instanceof Execute){
	    				execute_action = (Execute) ((Action)content).getAction();
	    				return false;
	    			}
	    		}
	    		catch (CodecException ce) {
	    			ce.printStackTrace();
	    		}
	    		catch (OntologyException oe) {
	    			oe.printStackTrace();
	    		}
	    		notUnderstoodMsg();
	    		return true;

	    	}
	    	
	    	ontology.messages.DataInstances processGetData(ACLMessage inform){
	    		ContentElement content;
					try {
						content = getContentManager().extractContent(inform);
						if (content instanceof Result) {
							Result result = (Result) content;
							if (result.getValue() instanceof ontology.messages.DataInstances) {
								return (ontology.messages.DataInstances)result.getValue();
							}
						}
					} catch (UngroundedException e) {
						e.printStackTrace();
					} catch (CodecException e) {
						e.printStackTrace();
					} catch (OntologyException e) {
						e.printStackTrace();
					}
					return null;
	    	}
	    	
	    	ProcessAction(Agent a){
	    		super(a);
	    		registerFirstState(new OneShotBehaviour(a){
  					int next;
  					public void action(){
  						result_msg = null;
  						execute_action = null;
  						getRequest();
  						if(processNonExecute()){
  							next = LAST_JMP;
  							return;
  						}
  						state = states.NEW;
  						//Set options
  						setOptions(execute_action.getTask());
  						eval = null;
  						success = true;
  						Data data = execute_action.getTask().getData();
  						output = data.getOutput();
  						mode = data.getMode();
  						
  						//Get training data
  						train_fn = data.getTrain_file_name();  						  							  						
  						AchieveREInitiator get_train_behaviour = (AchieveREInitiator) ((ProcessAction)parent).getState(GETTRAINDATA_STATE);
  						if (!train_fn.equals(trainFileName)){
  							get_train_behaviour.reset(sendGetDataReq(train_fn));
  						}else{
  							get_train_behaviour.reset(null);
  						}  						
  					
  						//Get testing data
						test_fn = data.getTest_file_name();
						AchieveREInitiator get_test_behaviour = (AchieveREInitiator) ((ProcessAction)parent).getState(GETTESTDATA_STATE);
  						if (!test_fn.equals(testFileName)){
  							get_test_behaviour.reset(sendGetDataReq(test_fn));
  						}else{
  							get_test_behaviour.reset(null); 					
  						}
  						next = NEXT_JMP;
  					}
  					public int onEnd(){
  						return next;
  					}
  				}, INIT_STATE);
  				
	    		registerState(new AchieveREInitiator(a, null){
  					public int next = NEXT_JMP;
  					
  					protected void handleInform(ACLMessage inform) {
  						ontology.messages.DataInstances _train = processGetData(inform);
  						if(_train!=null){
  							trainFileName = train_fn;
  							onto_train= _train;
  							train = onto_train.toWekaInstances();
  							train.setClassIndex(train.numAttributes() - 1);
  							next = NEXT_JMP;
  							return;
  						}else{
  							next = LAST_JMP;
  							failureMsg("No train data received from the reader agent: Wrong content.");
  							return;
  						}
  					}
  					
  					protected void handleFailure(ACLMessage failure) {
  						failureMsg("No train data received from the reader agent: Reader Failed.");
  						next = LAST_JMP;
  					}
  					
  					public int onEnd(){
  						int next_val = next;
  						next = NEXT_JMP;
  						return next;
  					}
  				}, GETTRAINDATA_STATE);
  				registerState(new AchieveREInitiator(a, null){
  					public int next = NEXT_JMP;
  					
  					protected void handleInform(ACLMessage inform) {
  						ontology.messages.DataInstances _test = processGetData(inform);
  						if(_test!=null){
  							testFileName = test_fn;
  							onto_test= _test;
  							test = onto_test.toWekaInstances();
  							test.setClassIndex(test.numAttributes() - 1);
  							
  							next = NEXT_JMP;
  							return;
  						}else{
  							next = LAST_JMP;
  							failureMsg("No test data received from the reader agent: Wrong content.");
  							return;
  						}  						 
  					}
  					
  					protected void handleFailure(ACLMessage failure) {
  						failureMsg("No test data received from the reader agent: Reader Failed.");
  						next = LAST_JMP;
  					}
  					
  					public int onEnd(){
  						int next_val = next;
  						next = NEXT_JMP;
  						return next;
  					}
  				}, GETTESTDATA_STATE);
  				
  				registerState(new Behaviour(a){
  					
  					public void action(){  						
  						//Train&test		  							
  						try{
  						/*	if (mode.equals("test_only")){
  								eval = evaluateCA();
  		  						if (output.equals("predictions")){
  		  							eval.setData_table(getPredictions(test, onto_test));
  								}
  							}
  						*/	
  							
  								if(state != states.TRAINED) { 
	  								train(); 
	  								if(state == states.TRAINED){
	  									eval = evaluateCA();
	  	  		  						if (output.equals("predictions")){	  	  		  							
	  	  		  							eval.setData_table(getPredictions(test, onto_test));
	  	  								}
	  								}
	  							}
  							  							
  						}
  						catch (Exception e){
  							working = false;
  							success = false;
  							failureMsg(e.getMessage()); 
  							System.out.println("Error: "+e.getMessage()+" ");
  							e.printStackTrace();
  						}
  					}
 					
  					@Override
					public boolean done() {
						return (state == states.TRAINED) || !success;
					}
  				}, TRAINTEST_STATE);
  				
  				registerLastState(new OneShotBehaviour(a){
  					
  					public void action(){
  						if (success && (result_msg == null)) {
   							result_msg = incoming_request.createReply();
  							result_msg.setPerformative(ACLMessage.INFORM);
  							try {
  								// Prepare the content - Result with Evaluation instead of MyWekaEvaluation is sended!!!
  								ContentElement content = getContentManager().extractContent(incoming_request); // TODO exception block?
  								Result result = new Result((Action)content, eval);
  								getContentManager().fillContent(result_msg, result);
  							} catch (UngroundedException e) {
  								e.printStackTrace();
  							} catch (CodecException e) {
  								e.printStackTrace();
  							} catch (OntologyException e) {
  								e.printStackTrace();
  							}
  						}
  						setResultMsg();
  					}
  				}, SENDRESULTS_STATE);
  				
  				registerTransition(INIT_STATE,GETTRAINDATA_STATE,NEXT_JMP);
  				registerTransition(INIT_STATE,SENDRESULTS_STATE,LAST_JMP);
  				
  				registerTransition(GETTRAINDATA_STATE,GETTESTDATA_STATE,NEXT_JMP);
  				registerTransition(GETTRAINDATA_STATE,SENDRESULTS_STATE,LAST_JMP);
  				
  				registerTransition(GETTESTDATA_STATE,TRAINTEST_STATE,NEXT_JMP);
  				registerTransition(GETTESTDATA_STATE,SENDRESULTS_STATE,LAST_JMP);
  				
  				registerDefaultTransition(TRAINTEST_STATE,SENDRESULTS_STATE);  				
	    	}
	    }

}; 