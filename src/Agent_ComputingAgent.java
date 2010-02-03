//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
import java.io.*;

import java.util.*;

import weka.core.Instances;
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

import ontology.*;

import jade.content.lang.Codec;
import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.sl.*;

public abstract class Agent_ComputingAgent extends Agent{


	 public enum states {
		    NEW, TRAINED 
	 }
	 	 
	 
	 /* common properties for all computing agents */
	 public String fileName;
	 public states state = states.NEW;
	 public boolean hasGotRightData = false;
	 
	 protected Vector<MyWekaOption> Options;
	 
	 protected Instances data; // data read from fileName file
	 Instances train;  // TODO - divide data
	 Instances test;

	 protected String[] OPTIONS;
	 protected String[] OPTIONS_;
	 protected String[] OPTIONS_ARGS;
	 
	 protected Object[] args;
	 
	 	 
	 boolean working = false;  // TODO -> state?
	
	 protected abstract void train();
	 protected abstract Evaluation test();
	 
	 protected abstract String getAgentType();
	 
	 protected abstract Object getModelObject();
	 protected abstract boolean setModelObject(Classifier _cls);

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
        	 typeDesc = getAgentType() +" trained on "+fileName;
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
	 

	 public boolean saveAgent(){
		 try{
			 ObjectOutputStream oos = new ObjectOutputStream(
			                            new FileOutputStream("saved/"+getLocalName()+".model"));
			 
 		     // save model + header
 		     Vector v = new Vector();
 		     v.add(getModelObject());
 		     v.add(new Instances(data, 0));
 		     v.add(fileName);
 		     v.add(state);

 			 oos.writeObject(v);
			 oos.flush();
			 oos.close();
			 System.out.println("Saving... : Description:"+this.toString());
			 return true;

		 }
		 catch (Exception e){
		 	System.out.println(e);
		 	return false;
		 }
	 } // end saveAgent


	 public boolean loadAgent(String agentName){
		 try{
			 // deserialize model + header
			 ObjectInputStream ois = new ObjectInputStream(
                     new FileInputStream("saved/"+agentName+".model"));
			 Vector v = (Vector) ois.readObject();
			 
			 Classifier cls = (Classifier) v.get(0);   // TODO this isn't general enough - cls doesn't have to be derived from Classifier
			 Instances header = (Instances) v.get(1);  // TODO this is not used so far
			 // System.out.println(agentName+" Header: "+header); 
			 fileName = (String) v.get(2);
			 state = (states) v.get(3);
			 
			 // TODO watch "working" variable
			 setModelObject(cls);			 
			 ois.close();		 
			 
			 System.out.println("Loading... : Description: "+this.toString());
			 System.out.println("                          fileName: "+fileName);
			 System.out.println("                          state: "+state);
			 
			 
			 // re-register with DF
			 // TODO what if it fails?
			 // deregisterWithDF();
			 // registerWithDF();
			 
			 return true;
		 }
		 catch (Exception e){
			 	System.out.println(e);
			 	return false;
		}
	 }  // end loadAgent

	 
	 
	 protected void setup() {		 

            // ContentManager manager = getContentManager();
            // Codec language = new SLCodec();
             
            // manager.registerLanguage(language);
             
            // Ontology ontology = O_Ontology.getInstance();
            // manager.registerOntology(ontology);
             
             // seed  = System.currentTimeMillis();
             // System.out.println(getLocalName()+ ": Has started, waiting for information queries");
             
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
	  		  		MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
	  		  		
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
	  						if (request.getContent().equals("Send options")){
	  							
	  							ACLMessage msgOut = new ACLMessage(ACLMessage.INFORM);
	  							msgOut.addReceiver(request.getSender());
	  							try {
									msgOut.setContentObject(Options);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								
								return msgOut;
	  						}
	  						else{
	  							state = states.NEW;
	  							
		  						OPTIONS_ = request.getContent().split(" ", 2);
		  						
		  						setOptions(OPTIONS_[1].split(" "));
			  						
		  						Evaluation result = null;
		  						MyWekaEvaluation my_result = null;
		  						
		  						
		  						boolean success = true;
		  						
		  						if (OPTIONS_[0].equals(fileName)){
		  							// TODO - better control of the data
		  							hasGotRightData = true;
		  						}
		  						else{
		  							fileName = OPTIONS_[0];
		  							getData();
		  							hasGotRightData = false;

			  						boolean done = false; 

			  						Date start = new Date();
			  						long start_long = start.getTime();
			  						long now;
			  						while(!done){ 							
			  						  now = start.getTime();
			  						  // give up after 10 seconds
			  						  if (now > start_long+10000){
			  							  done = true;
			  						  }
			  						  MessageTemplate template_msg_from_reader = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			  						  ACLMessage reply = myAgent.receive(template_msg_from_reader);
				  					  
			  						  
			  						  if (reply != null) {		  				        
			  				    	  // Reply received
				  							try {
				  								data = (Instances) reply.getContentObject();
											} catch (UnreadableException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
												success = false;
											}
				  				    		System.out.println("Data: "+data);  
				  						    hasGotRightData = true;    
				  			    		
				  							/* 
				  							 The class index indicate the target attribute used for
				  							 classification. By default, in an ARFF File, it's the 
				  							 last attribute, that's why it's set to numAttributes-1.
				  							 You must set it if your instances are used as a parameter
				  							 of a weka function (ex: weka.classifiers.Classifier.buildClassifier(data))
				  							*/		  						    
				  				    		data.setClassIndex(data.numAttributes() - 1);
				  				    		
				  				    		test = data;
				  				    		train = data;
				  				    		
				  				    		done = true;
				  				    		
				  					  }  // end if (reply != null)
			  						  else{
			  							  block();
			  						  }
			  						}  // end while (!done)
		  					
		  						}  // end else (= hasGotRightData = false)
		  						
		  		
				  					  							
	  				    		try{
			  						if (state != states.TRAINED) { train(); }
		  				    		// saveAgent();
		  				    		// loadAgent(getLocalName());
		  				  	  	
		  				    		result = test();
		  				    		// TODO
		  				    		my_result = new MyWekaEvaluation(result);
	  				    		}
	  				    		catch (Exception e){
	  				    			success = false;
	  				    		}
					  					   	
			  				
		  						if (success) {
		  							System.out.println("Agent "+getLocalName()+": Action successfully performed.");
		  							ACLMessage inform = request.createReply();
		  							inform.setPerformative(ACLMessage.INFORM);
		  							
		  							try {
										inform.setContentObject(my_result);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
		  							return inform;
		  						}
		  						else {
		  							System.out.println("Agent "+getLocalName()+": Action failed");
		  							throw new FailureException("unexpected-error");
		  						}
		  					
	  						} 	  					
	  					}  //  end prepareResultNotification
	  					
	  				} );
	 
	 } // end setup
	 
	 
	 
	 public boolean setOptions(String[] CONFIGURATION){
		  /* INPUT: weka parameters
		   * Fills the OPTIONS array.
		   */
		 OPTIONS = CONFIGURATION;
		 
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
 
	 
	 protected boolean getData(){ 
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
}; 