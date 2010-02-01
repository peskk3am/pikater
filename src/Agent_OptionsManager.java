	import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

// import Agent_ComputingAgent.states;

import weka.classifiers.Evaluation;
import weka.core.Instances;
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
import jade.core.behaviours.SimpleBehaviour;
import jade.proto.AchieveREInitiator;


public abstract class Agent_OptionsManager extends Agent {

	
		 String fileName = "weather.arff";
		 String receiver = "mp1";
		 
	 	 boolean working = false;
	 	 
		 MyWekaEvaluation result;
	 	 protected Vector<MyWekaOption> Options;
	 	 
		 protected abstract String getAgentType();
		 protected abstract boolean finished();
		 protected abstract String generateNewOptions(MyWekaEvaluation result);
		 
		 
		 
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


	         // add "computing agent service"
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
		
		 
		 
		
		 ACLMessage NewMessage(ACLMessage _result){
			 
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
	  			msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
				msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				// We want to receive a reply in 30 secs
				msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));			
				msg.setContent(fileName+" "+opt);
			 }
			 else{
				msg = new ACLMessage(ACLMessage.CANCEL);
			 }
			 			
			 return msg;				

		 } // NewMessage

		 
		 void Start(ACLMessage request){
			 
			 IteratedAchieveREInitiator behav = new IteratedAchieveREInitiator(this, NewMessage(null)) {
					
					protected void handleInform(ACLMessage inform, java.util.Vector nextRequests) {
						System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" sent a reply.");
					  			
						nextRequests.add(NewMessage(inform));
					}
					
					protected void handleRefuse(ACLMessage refuse) {
						System.out.println(getLocalName()+": Agent "+refuse.getSender().getName()+" refused to perform the requested action");
					}
					
					protected void handleFailure(ACLMessage failure) {
						if (failure.getSender().equals(myAgent.getAMS())) {
							// FAILURE notification from the JADE runtime: the receiver
							// does not exist
							System.out.println("Responder does not exist");
						}
						else {
							System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
						}
					}

				};
				
			 addBehaviour(behav);

		 }
		 
		 
		 protected void setup() {
			// behaviour = new ChooseBestOptions(this);
	        // addBehaviour(behaviour);
			
			
		  	System.out.println(getLocalName()+" is alive...");
		  	
		 
			registerWithDF();


			
			final Agent_OptionsManager _this = this; 	
	
					
			
  		  	MessageTemplate template_inform = MessageTemplate.and(
	  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
	  		MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
	  		  		
  		    AchieveREResponder receive_goal = new AchieveREResponder(this, template_inform) {
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
				
				protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {					
						System.out.println("Agent "+getLocalName()+": Preparing response");
						try {
							Options = (Vector<MyWekaOption>) request.getContentObject();
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						Start(request);
											
					
						ACLMessage msgOut = new ACLMessage(ACLMessage.INFORM);
						msgOut.addReceiver(request.getSender());
						msgOut.setContent("OK");
						
						return msgOut;
				}  //  end prepareResultNotification
				
			};
			
			addBehaviour(receive_goal);
			

		 
		 } // end setup

	
}
