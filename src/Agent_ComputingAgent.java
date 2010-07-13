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
	 Instances test;

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
		 ACLMessage msgOut = new ACLMessage(ACLMessage.INFORM);
		 msgOut.setLanguage(codec.getName());
		 msgOut.setOntology(ontology.getName());

		 msgOut.addReceiver(request.getSender());
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
					
		setOptions(execute.getTask());
			
		ontology.messages.Evaluation eval = null;
		
		boolean success = true;
		
		Data data = execute.getTask().getData();
		if (!data.getTrain_file_name().equals(trainFileName)){
			hasGotRightData = false;	
			trainFileName = data.getTrain_file_name();
			getData(trainFileName);
				
			train = waitForAnswer(behavior);
			if (data == null){
				throw new FailureException("No train data received from the reader agent.");
			}
			else{
				hasGotRightData = true;
			}
		}

		if (!data.getTest_file_name().equals(testFileName)){
			hasGotRightData = false;
			testFileName = data.getTest_file_name();
			getData(testFileName);
				
			test = waitForAnswer(behavior);
			if (data == null){
				throw new FailureException("No test data received from the reader agent.");
			}
			else{
				hasGotRightData = true;
			}

		}

				  							
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
	 
	 
	 private Instances waitForAnswer(AchieveREResponder behavior) throws FailureException{
		boolean done = false; 
		Instances _data = null;
		
		Date start = new Date();
		long start_long = start.getTime();
		long now;
		while(!done){ 							
		  now = start.getTime();
		  // give up after 10 seconds
		  // TODO ...
		  if (now > start_long+10000){
			  throw new FailureException("No data received");
		  }
		  MessageTemplate template_msg_from_reader = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		  // ACLMessage reply = myAgent.receive(template_msg_from_reader);
		  ACLMessage reply = receive(template_msg_from_reader);
		  
		  if (reply != null) {		  				        
    	  // Reply received
				try {
					_data = (Instances) reply.getContentObject();
		    		System.out.println("Data: "+_data);  
	    		
					/* 
					 The class index indicate the target attribute used for
					 classification. By default, in an ARFF File, it's the 
					 last attribute, that's why it's set to numAttributes-1.
					 You must set it if your instances are used as a parameter
					 of a weka function (ex: weka.classifiers.Classifier.buildClassifier(data))
					*/		  						    
		    		_data.setClassIndex(_data.numAttributes() - 1);
		    			    		
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		done = true;
	    		
		  }  // end if (reply != null)
		  else{
			  behavior.block();
		  }
		}  // end while (!done)
		return _data; 
	 }  // end waitForAnswer
	 
	 
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
 
	 
	 protected boolean getData(String fileName){ 
		 // send message to ARFFReader agent
         
		 // The list of known reader agents
		 AID[] ARFFReaders;
		 AID reader;
		 
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
         }
         catch (FIPAException fe) {
           fe.printStackTrace();
           return false;
         }
		 
		 ACLMessage msgOut = new ACLMessage(ACLMessage.REQUEST);
		 // msgOut.addReceiver(new AID((String) "reader@klara:1099/JADE", AID.ISLOCALNAME));
		 msgOut.addReceiver(reader);
		 msgOut.setContent(fileName);
		 send(msgOut);
		 
		 return true;
	 } // end getData
	 
	 
	 
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