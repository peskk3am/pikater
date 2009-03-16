import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;

import weka.core.Instances;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;


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

import ontology.*;

import jade.content.lang.Codec;
import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.lang.sl.*;

public abstract class Agent_ComputingAgent extends Agent{
	 
	 protected String fileName;
	 
	 // data read from file
	 protected Instances data;
	 
	 protected String[] OPTIONS;
	 protected String[] OPTIONS_;
	 protected String[] OPTIONS_ARGS;
	 
	 protected Object[] args;
	 
	 boolean working = false;
	
	 protected abstract double proceed();
	 protected abstract String getAgentType();
	 // protected abstract void registerWithDF();
	 
	 
	 protected void setup() {		 

            // ContentManager manager = getContentManager();
            // Codec language = new SLCodec();
             
            // manager.registerLanguage(language);
             
            // Ontology ontology = O_Ontology.getInstance();
            // manager.registerOntology(ontology);
             
             // seed  = System.currentTimeMillis();
             // System.out.println(getLocalName()+ ": Has started, waiting for information queries");
             
             //register with the DF
             
             DFAgentDescription description = new DFAgentDescription();
             // the description is the root description for each agent 
             // and how we prefer to communicate. 
             // description.addLanguages(language.getName());
             // description.addOntologies(ontology.getName());
             // description.addProtocols(InteractionProtocol.FIPA_REQUEST);
             description.setName(getAID());
             
             // the service descriptioon describes a particular service we
             // provide.
             ServiceDescription servicedesc = new ServiceDescription();
             //the name of the service provded (we just re-use our agent name)
             servicedesc.setName(getLocalName());
             
             //The service type should be a unique string associtated with
             //the service.
             servicedesc.setType(getAgentType()); 

             //the service has a list of supported languages, ontologies
             //and protocols for this service.
             // servicedesc.addLanguages(language.getName());
             // servicedesc.addOntologies(ontology.getName());
             // servicedesc.addProtocols(InteractionProtocol.FIPA_REQUEST);
             
             description.addServices(servicedesc);
             // servicedest.setT

             
             //register synchronously registers us with the DF, we may
             //prefer to do this asynchronously using a behaviour.
             try{
                     DFService.register(this,description);
             }catch(FIPAException e){
                     System.err.println(getLocalName() + ": error registering with DF, exiting:" + e);
                     doDelete();
                     return;
                     
             }
		 
		 
	  	System.out.println(getAgentType()+" "+getLocalName()+" is alive...");

		args = getArguments();

		OPTIONS_ARGS = new String[args.length];
		
		if (args != null && args.length > 0) {
		
			// parameters of the network
			for (int i=0; i < args.length; i++){
				OPTIONS_ARGS[i] = (String) args[i];
			}
			/*
			// write out parameters
			for (String s : OPTIONS_ARGS) {
				System.out.print(s+" ");
			}
			*/	
		}
	  	
	  	MessageTemplate template = MessageTemplate.and(
	  	  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
	  	  		MessageTemplate.MatchPerformative(ACLMessage.CFP) );
	  	
	  			addBehaviour(new ContractNetResponder(this, template) {
	  				protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
	  					System.out.println("Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName()+". Parameters are "+cfp.getContent());
	// 602939833 Vrtatko  				    	
	  					if (!working) {					
	  						System.out.println("Agent "+getLocalName()+": goal accepted.");
	  						String parameters = cfp.getContent();
	  						
	  						OPTIONS  = parameters.split(" ");
	  						OPTIONS_ = new String[OPTIONS.length-1+args.length]; 
	  						
	  						if (OPTIONS.length > 0) {
	  							// first argument ... file name
	  							fileName = (String) OPTIONS[0];
	  							System.out.println("File name: "+fileName);
	  						}
	  						else{
	  							// ERROR
	  						}
	  					
	  						for(int i=1; i<OPTIONS.length; i++){  // delete first element
	  						   OPTIONS_[i-1] = OPTIONS[i];
	  						}
	  						
	  						// add OPTIONS_ARGS
	  						for (int i=0; i<args.length; i++){
	  							OPTIONS_[i+OPTIONS.length-1] = OPTIONS_ARGS[i];
	  						}
	  						// put options back to OPTIONS
	  						OPTIONS = OPTIONS_;
	  						
	  						//if (OPTIONS[0] == null){
	  						//	OPTIONS = null; // prazdny seznam
	  						//}
	  						
	  						// write out OPTIONS
	  						for (int i=0; i<OPTIONS.length; i++){
	  							System.out.println(i+" "+OPTIONS[i]);
	  						}
	  						
	  						
	  						getData(); // if == false, no reader agent found
	  						
	  						double result = 100;
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
		  				    	try{
		  				    		data = (Instances) reply.getContentObject();
		  				    		System.out.println("Data:"+data);  
		  						    
		  							/* 
		  							 The class index indicate the target attribute used for
		  							 classification. By default, in an ARFF File, it's the 
		  							 last attribute, that's why it's set to numAttributes-1.
		  							 You must set it if your instances are used as a parameter
		  							 of a weka function (ex: weka.classifiers.Classifier.buildClassifier(data))
		  							*/		  						    
		  				    		data.setClassIndex(data.numAttributes() - 1);
		  				    		
		  				    		result = proceed();
		  				    	}
		  				    	catch (Exception e){
		  							// TODO Auto-generated catch block
		  							e.printStackTrace();
		  				    	}
			  					done = true;
		  				      }
		  				      else {
		  				        block();
		  				      }
		  			
	  						}
	  				      
	  						
	  						// provide a proposal
	  						ACLMessage propose = cfp.createReply();
	  						propose.setPerformative(ACLMessage.PROPOSE);
	  						propose.setContent(String.valueOf(result));
	  						return propose;
	  					}
	  					else {
	  						// refuse to provide a proposal
	  						System.out.println("Agent "+getLocalName()+": is working now");
	  						throw new RefuseException("Agent "+getLocalName()+" is busy");
	  					}
	  				}
	  				
	  				protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
	  					System.out.println("Agent "+getLocalName()+": Proposal accepted");

	  					ACLMessage inform = accept.createReply();
	  					inform.setPerformative(ACLMessage.INFORM);
	  					return inform;
	  				}
	  				
	  				protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
	  					System.out.println("Agent "+getLocalName()+": Proposal rejected");
	  				}
	  			} );
	  		    

	 
	 } // end setup
	 
	 
	 
	 protected boolean getData(){ 
		 // send message to ARFFReader agent
         
		 // The list of known seller agents
		 AID[] ARFFReaders;
		 AID reader;
		 
		 // Make the list of reader agents
		 DFAgentDescription template = new DFAgentDescription();
         ServiceDescription sd = new ServiceDescription();
         sd.setType("ARFFReader");
         template.addServices(sd);
         try {
         	DFAgentDescription[] result = DFService.search(this, template); 
         	System.out.println("Found the following ARFFReader agent:");
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
	 	 
}; 