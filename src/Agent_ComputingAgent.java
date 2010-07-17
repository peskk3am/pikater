//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
import java.io.*;

import java.util.*;

import org.jdom.JDOMException;

import jade.util.leap.List;
import jade.util.leap.ArrayList;

import weka.core.Instances;
import weka.core.Option;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.RBFNetwork;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
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
import jade.proto.ContractNetResponder;
import jade.proto.AchieveREResponder;

import ontology.messages.*;

import jade.content.lang.Codec;
import jade.content.*;
import jade.content.abs.*;
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
	 Instances train;  // TODO - divide data
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
	 
	 
	 protected ACLMessage Execute(ACLMessage request, Execute execute, AchieveREResponder behavior) throws FailureException{
		state = states.NEW;
		//Set options
		setOptions(execute.getTask());
			
		ontology.messages.Evaluation eval = null;
		
		boolean success = true;
		
		Data data = execute.getTask().getData();
		//Get training data
		if (!data.getTrain_file_name().equals(trainFileName)){
			hasGotRightData = false;	
			trainFileName = data.getTrain_file_name();
			onto_train = getData_(trainFileName);
			if (data == null || onto_train == null){
				throw new FailureException("No train data received from the reader agent.");
			}
			else{
				hasGotRightData = true;
				train = onto_train.toWekaInstances();
				train.setClassIndex(train.numAttributes() - 1);
			}
		}
		//Get testing data
		if (!data.getTest_file_name().equals(testFileName)){
			hasGotRightData = false;
			testFileName = data.getTest_file_name();
			onto_test = getData_(testFileName);
			if (data == null || onto_test == null){
				throw new FailureException("No test data received from the reader agent.");
			}
			else{
				hasGotRightData = true;
				test = onto_test.toWekaInstances();
				test.setClassIndex(test.numAttributes() - 1);
			}

		}

		//Train&test		  							
		try{
			if (state != states.TRAINED) { train(); }
			// saveAgent();
			// loadAgent(getLocalName());
			
			//testing...
			eval = evaluateCA();			
		}
		catch (Exception e){
			success = false;
		}
				   	
		//Send results
		if (success) {
			System.out.println("Agent "+getLocalName()+": Action successfully performed.");
			ACLMessage inform = request.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			try {
				// Prepare the content - Result with Evaluation instead of MyWekaEvaluation is sended!!!
				ContentElement content = getContentManager().extractContent(request); // TODO exception block?
				Result result = new Result((Action)content, eval);
				getContentManager().fillContent(inform, result);
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
			
			return inform;
		}
		else {
			System.out.println("Agent "+getLocalName()+": Action failed");
			throw new FailureException("unexpected-error");
		}
	
	 } 	// end Execute  					
	 
	 
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


	  		  		
	  				addBehaviour(new AchieveREResponder(this, template_inform) {
	  					protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
	  						System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
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
	  					
	  					protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
	  						
	  						
	  						try{
	  							ContentElement content = getContentManager().extractContent(request);
	  							// System.out.println(((Action)content).getAction());
	  							
	  							if (((Action)content).getAction() instanceof GetOptions){
	  								return sendOptions(request);
	  							}
	  							
	  							if (((Action)content).getAction() instanceof Execute){
	  								Execute execute = (Execute) ((Action)content).getAction();
	  								return Execute(request, execute, this);
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
	 
	 } // end setup
	 
	 
	 
	 public boolean setOptions(ontology.messages.Task task){
		  /* INPUT: task with weka options
		   * Fills the OPTIONS array and current_task.
		   */
		 current_task = task;
		 OPTIONS = task.getAgent().optionsToString().split(" ");
		 
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
			 //sending
			 send(msgOut);
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
	 
	 /*Blocking get_data*/
	 protected DataInstances getData_(String file_name){
		 ACLMessage req = sendGetDataReq(file_name);
		 if(req!=null){
			 MessageTemplate template_msg_from_reader = MessageTemplate.and(
					 MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					 MessageTemplate.MatchConversationId(req.getConversationId()));
			 //waiting for data - blocking!!!
			 ACLMessage respond = blockingReceive(template_msg_from_reader, 10000);
			 //content extraction
			 ContentElement content;
			 try {
				 content = getContentManager().extractContent(respond);
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
		 }
		 //something is wrong
		 return null;
		 /*_data = data_instances.toWekaInstances();
		_data.setClassIndex(_data.numAttributes() - 1); */
	 }
	 
	 
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
}; 