import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import ontology.messages.*;
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
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;


public abstract class Agent_GUI extends Agent {	
	
	private Codec codec = new SLCodec();
	private Ontology ontology = MessagesOntology.getInstance();
	

	private List _agents;
	protected Vector<String[]> Agents;  // vector containing the arrays [agentName, param1, valueOfParam1, nextParams...]
	private List _fileNames;
	private Problem problem;
	
	protected int numberOfReplies;
	
	
	/*
	 * 	should use the following methods:
	 * 		refreshOptions(ontology.messages.Agent agent) should be called after user changes options of an agent
	 * 		sendProblem(); should be called after the Problem is ready to be sent to the manager
	 * 
	 * 	can use the following method:
	 * 		protected String[] getComputingAgents()
	 */
	
	
	protected abstract String getAgentType();
		/* returns the string with agent type */
	
	protected abstract void displayOptions(ontology.messages.Agent agent);
		/* method should be used to display agent options,
		 * it is called automatically after receiving the message from a computing agent */
	
	protected abstract void displayResult(ACLMessage inform);
		/* method should be used to display the result,
		 * it is called automatically after receiving the message from a manager */
	
	protected abstract void mySetup();
		/* it should call
		 * ... addFileToProblem(String fileName) for each data file to be added to the Problem
		 * ... addAgentToProblem(String[] agentParams) to add agents to the Problem,
		 *                     agentParams is an array containing agents name and parameters in weka data format,
		 *                     [agentName, param1, valueOfParam1, nextParams...]
		 * ... getAgentOptions(String agentName) to receive the options from each computing agent 
		 * */
	
	protected abstract void allOptionsReceived();
		/* automatically called after all replies from computing agents are received */
	
	
	
	protected void reset(){
		_agents = new ArrayList();
		Agents = new Vector<String[]>();
		_fileNames = new ArrayList();
		problem = new Problem();
		
		numberOfReplies = 0;
	}
	
	protected String[] getComputingAgents(){
		// returns the array of all computing agents' local names
	  			  	
		String type = "ComputingAgent"; 
	  	
		// The list of known computing agents
		String[] ComputingAgents = null;

		// Make the list
		DFAgentDescription template = new DFAgentDescription();
        ServiceDescription CAsd = new ServiceDescription();
        CAsd.setType(type);
        template.addServices(CAsd);
        try {
         	DFAgentDescription[] result = DFService.search(this, template); 
         	System.out.println("Found the following agents:");
         	ComputingAgents = new String[result.length];
           
            for (int i = 0; i < result.length; ++i) {
            	ComputingAgents[i] = result[i].getName().getLocalName();
	          	System.out.println(ComputingAgents[i]);
           }          

        }
        catch (FIPAException fe) {
           fe.printStackTrace();
        }
        catch (ArrayIndexOutOfBoundsException ae){
        	System.out.println("No "+type+" found.");
        }
        return ComputingAgents;
        
	} // end getComputingAgents
	
	protected void getAgentOptions(String receiver){
		// returns the ontology class Agent (containing agent options) for an
		// agent "receiver", specified by its localName
		 
		
	  	// get available Options from selected agent:
	  	
	  	// create a request message with GetOptions content

	  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		
	  	
	  	msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
	  	
	  	
	  	msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());
		
		// We want to receive a reply in 30 secs
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));			
		
		// Prepare the content.			
		GetOptions get = new GetOptions();
		Action a = new Action();
		a.setAction(get);
		a.setActor(this.getAID());
		
		try {
			// Let JADE convert from Java objects to string
			getContentManager().fillContent(msg, a);
			
		}
		catch (CodecException ce) {
			ce.printStackTrace();
		}
		catch (OntologyException oe) {
			oe.printStackTrace();
		}
		
		
	  	AchieveREInitiator behav = new AchieveREInitiator(this, msg) {
	  			  		
	  		
			protected void handleInform(ACLMessage inform) {
				System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" replied.");					
			  	// we've just received the Options in an inform message
					
			  		ContentElement content;
					try {
						content = getContentManager().extractContent(inform);
						// System.out.println(getLocalName()+": Action: "+((Result)content).getAction());
						if (content instanceof Result) {
			                Result result = (Result) content;
			                
			                if (result.getValue() instanceof ontology.messages.Agent) {
			                	
			                	ontology.messages.Agent agent = (ontology.messages.Agent)result.getValue();
			                	
			                	numberOfReplies++;
			                	
			                	displayOptions(agent);
			                	
			                	if (numberOfReplies == Agents.size()){
			                		allOptionsReceived();
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
		
	} // end getAgentOptions
				
	protected void sendProblem(){		 
		problem.setAid(getAID());

		
	  	// create a request message with SendProblem content
	  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID("manager", AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());
		
		// We want to receive a reply in 30 secs
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));			
		
		// Prepare the content.			
		Solve solve = new Solve();
		solve.setProblem(problem);
		
		Action a = new Action();
		a.setAction(solve);
		a.setActor(this.getAID());
		
		
		try {
			// Let JADE convert from Java objects to string
			getContentManager().fillContent(msg, a);
			
		}
		catch (CodecException ce) {
			ce.printStackTrace();
		}
		catch (OntologyException oe) {
			oe.printStackTrace();
		}
		
		
	  	AchieveREInitiator behav = new AchieveREInitiator(this, msg) {
	  		// send a problem
	  		
			protected void handleInform(ACLMessage inform) {
				System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" replied.");					
				displayResult(inform);
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

		
		
		msg = new ACLMessage(ACLMessage.SUBSCRIBE);
		msg.addReceiver(new AID("manager", AID.ISLOCALNAME));
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
	
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());
		
		SubscriptionInitiator send_problem = new SubscriptionInitiator(this, msg){
			// receive the sequence of replies
			
			protected void handleInform(ACLMessage inform) {
				System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" replied.");					
				displayResult(inform);
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
			
			/* cancel(AID receiver, boolean ignoreResponse)
			Cancel the subscription to agent receiver. This method retrieves the subscription message
			sent to receiver and sends a suitable CANCEL message with the conversationID and all other
			protocol fields appropriately set. The content slot of this CANCEL message is filled in by means
			of the fillCancelContent() method.
			*/
			//-- > zavolat, az budu mit vsechny odpovedi
				
		};
		
		addBehaviour(send_problem);
		
	}
	
	protected void addAgentToProblem(String [] agentParams){
		String agentName = agentParams[0];
		
		// add agent to Agents Vector 	
    	Agents.add(agentParams);
			
    	ontology.messages.Agent agent = new ontology.messages.Agent(); 
    	agent.setName(agentName);
		_agents.add(agent);
        problem.setAgents(_agents);

	}

	protected void addFileToProblem(String _fileName){
		_fileNames.add(_fileName);
        problem.setFile_names(_fileNames);

	}
	
	protected void refreshOptions(ontology.messages.Agent agent) {
		
		List Options = agent.getOptions();
		
		for (Enumeration e = Agents.elements() ; e.hasMoreElements() ;) {
	        String[] next;
			next = (String[])e.nextElement();
			// find the agent in the list
			
			if (next[0].equals(agent.getName())){
				// go through its parameters
		    	for (int i=1; i < next.length-1; i+=2){
		    		// find the same parameter in options list
		    		Iterator itr = Options.iterator();	 		   		 
		   		 	while (itr.hasNext()) {
		   		 		Option opt_next = (Option) itr.next();
		   		 		if ( next[i].equals(("-"+opt_next.getName()))){
		   		 			
		   		 			if (next[i+1].equals("?")){
		   		 				opt_next.setMutable(true);	
		   		 			}
		   		 			else { 
		   		 				// set the value of the parameter
		   		 				opt_next.setValue(next[i+1]);
		   		 			}
		   		 				
		   		 		}
		   		 	}  // end while (finding the right option in agent.options)
		   		}
		   	}
		}  // end for
		
		// agent.setOptions(Options);
	
		
		Iterator itr = problem.getAgents().iterator();	 		   		 
	 	while (itr.hasNext()) {
	 		ontology.messages.Agent _next = (ontology.messages.Agent) itr.next();
	 		
	 		if (agent.getName().equals(_next.getName())){
	 			_next.setOptions(Options);
	 		}
	 	}
		

	} //  end displayOptions
			
	protected void setup(){
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
	    // register with DF
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();   
		sd.setType(getAgentType()); 
		sd.setName(getName());
		dfd.setName(getAID());
		dfd.addServices(sd);
		try {
		    DFService.register(this,dfd);
		} catch (FIPAException e) {
		    System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
		    doDelete();
		}  
	  	System.out.println("GUI agent "+getLocalName()+" is alive and waiting...");
	  	
	  	reset();
	  	
	  	mySetup();

	
	}  // end setup
}