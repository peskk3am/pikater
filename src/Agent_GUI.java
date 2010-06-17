import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Vector;


import org.jdom.*;
import org.jdom.input.SAXBuilder;

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
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;


public abstract class Agent_GUI extends Agent {	
	
	private Codec codec = new SLCodec();
	private Ontology ontology = MessagesOntology.getInstance();
	
	private int default_timeout = 30000;  // 30s 
	
	protected Vector<Problem> problems = new Vector<Problem>();
		
	private int problem_id = 0;
	
	
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
	
	protected abstract void displayOptions(Problem problem, String message);
		/* method should be used to display agent options,
		 * it is called automatically after receiving the message from a computing agent */
	
	protected abstract void displayResult(ACLMessage inform);
		/* method should be used to display the result,
		 * it is called automatically after receiving the message from a manager */
	
	protected abstract void mySetup();
		/* it should call
		 * ... int createNewProblem() - returns the _problem_id 
		 * ... addAgentToProblem(int _problem_id, String name)
		 * ... addOptionToAgent(int _problem_id, String agent_name, String option_name, String option_value )
		 * ... addFileToProblem(int _problem_id, String _fileName)
		 * 
		 * ... getAgentOptions(String agentName) to receive the options from each computing agent 
		 * */
	
	protected abstract void allOptionsReceived(int problem_id);
		/* automatically called after all replies from computing agents are received */
	
	protected abstract void displayPartialResult(ACLMessage inform);
	/* Process the partial results received from computing agents 
	 *  maybe only the content would be better as a parameter */ 	
	
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
			              
			                	refreshOptions(agent, "OK");			                	
			                	checkProblems();
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
				ontology.messages.Agent agent = new ontology.messages.Agent();
				agent.setName(refuse.getSender().getName());
				
				System.out.println(getLocalName()+": Agent "+refuse.getSender().getName()+" refused to perform the requested action");
				refreshOptions(agent, "refuse");
				checkProblems();
			}
			
			protected void handleFailure(ACLMessage failure) {
				ontology.messages.Agent agent = new ontology.messages.Agent();
				agent.setName(failure.getSender().getName());
				
				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver
					// does not exist
					System.out.println("Responder does not exist");
				}
				else {
					System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
				}
				refreshOptions(agent, "failure");
				checkProblems();

			}

		};
		
		addBehaviour(behav);
		
	} // end getAgentOptions
	
	protected void sendProblem(int _problem_id){
		// find the problem according to a _problem_id
		Problem problem = null;

		// TODO what if the problem could not be found
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (Integer.parseInt(next_problem.getGui_id()) == _problem_id 
					&& !next_problem.getSent()) {
				problem = next_problem;
			}
		}
		
		if (problem == null){  // TODO exception
			return;
		}
		
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
		
		
	  	AchieveREInitiator send_problem = new AchieveREInitiator(this, msg) {
	  		// send a problem
	  		
	  		protected void handleAgree(ACLMessage agree){
	  			System.out.println(getLocalName()+": Agent "+agree.getSender().getName()+" agreed.");
	  			updateProblemId(agree.getContent());
	  		}
	  		
			protected void handleInform(ACLMessage inform) {
				System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" replied.");
				
				// remove problem from problems vector
				// problems.remove(problem);
				
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
		
		addBehaviour(send_problem);
		
		problem.setSent(true);
		
		
		msg = new ACLMessage(ACLMessage.SUBSCRIBE);
		msg.addReceiver(new AID("manager", AID.ISLOCALNAME));    // TODO find manager in yellow pages
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
	
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());
		
		SubscriptionInitiator receive_results = new SubscriptionInitiator(this, msg){
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
					
		};
		
		addBehaviour(receive_results);
		
	}
	
	protected int createNewProblem(String timeout){
		int _timeout;
		Problem problem = new Problem();
		problem.setGui_id(Integer.toString(problem_id));   // agent manager changes the id afterwards
		if (timeout == null){
			_timeout = default_timeout;
		}
		else{
			_timeout = Integer.parseInt(timeout);
		}
		
		problem.setTimeout(_timeout);
		problem.setAgents(new ArrayList());
		problem.setData(new ArrayList());
		problem.setSent(false);
 		problems.add(problem);
		
		return problem_id++;
	}
	
	
	protected void addAgentToProblem(int _problem_id, String name){
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
				if (Integer.parseInt(next_problem.getGui_id()) == _problem_id) {
					ontology.messages.Agent agent = new ontology.messages.Agent();	
					agent.setName(name);
					agent.setOptions(new ArrayList());
					List agents = next_problem.getAgents();
					agents.add(agent);
					next_problem.setAgents(agents);
				}
			}
		}
	}
	
	protected void addOptionToAgent(int _problem_id, String agent_name, String option_name, String option_value ){
		// TODO add interval ... 
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
	
				if (Integer.parseInt(next_problem.getGui_id()) == _problem_id) {
					Iterator itr = next_problem.getAgents().iterator();	 		   		 
		   		 	while (itr.hasNext()) {
		   		 		ontology.messages.Agent next_agent = (ontology.messages.Agent) itr.next();
		   		 		// find the right agent
		   		 		if (next_agent.getName().equals(agent_name)){
		   		 			Option option = new Option();
		   		 			option.setName(option_name);
		   		 			if (option_value.equals("?")){
		   		 				option.setMutable(true);
		   		 			}
		   		 			else{
		   		 				option.setValue(option_value);
		   		 			}
		   		 			
		   		 			List options = next_agent.getOptions();
		   		 			options.add(option);
		   		 			next_agent.setOptions(options);
		   		 		}
		   		 	}
				}
			}
		}
		
	}

	protected void addDatasetToProblem(int _problem_id, String _train, String _test){
		// get the problem
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
				if (Integer.parseInt(next_problem.getGui_id()) == _problem_id){
					List data = next_problem.getData();
					Data d = new Data();
					d.setTrain_file_name(_train);
					d.setTest_file_name(_test);
					data.add(d);
			        next_problem.setData(data);
				}
			}
		}
	}
	

	private void checkProblems(){
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
				boolean done = true;
				Iterator aitr = next_problem.getAgents().iterator();	 		   		 
	   		 	while (aitr.hasNext()) {
	   		 		ontology.messages.Agent next_agent = (ontology.messages.Agent) aitr.next();
	   		 		
	   		 		// if data_type is set it means that the options from a computing agent have
	   		 		// been received already
	   		 		// it's enough to test the first option
	   	   		 	if ( ((Option)(next_agent.getOptions().iterator().next())).getData_type() == null ){
	   	   		 		done = false;
	   		 		}
	   		 	}
	   			if (done){
	   				allOptionsReceived(Integer.parseInt(next_problem.getGui_id()));
	   			}
			}
		}
	}
	
	private void refreshOptions(ontology.messages.Agent agent, String message) {
		// refresh options in all problems, where the agent is involved
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {	
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
				Iterator aitr = next_problem.getAgents().iterator();	 		   		 
	   		 	while (aitr.hasNext()) {
	   		 		ontology.messages.Agent next_agent = (ontology.messages.Agent) aitr.next(); 		 	
					
	   		 		// all problems where the agent (input parameter) figures
	   		 		if ( next_agent.getName().equals(agent.getName()) ){
						
	   		 			if (message.equals("OK")) { 			
		   		 			
							// update the options (merge them)
		   		 			
							// copy agent's options
	   		 				java.util.List mergedOptions = new java.util.ArrayList();					
							Iterator oitr = agent.getOptions().iterator();	 		   		 
				   		 	while (oitr.hasNext()) {
				   		 		Option next_option = (Option) oitr.next();
				   		 		mergedOptions.add(next_option);
				   		 	}
							
							// go through the options set in the problem 
				   		 	// and replace the options send by an computing agent
							Iterator opitr = next_agent.getOptions().iterator();	 		   		 
				   		 	while (opitr.hasNext()) {
				   		 		Option next_problem_option = (Option) opitr.next();
					   		 	ListIterator ocaitr = mergedOptions.listIterator();	 		   		 
					   		 	while (ocaitr.hasNext()) {
					   		 		Option next_merged_option = (Option) ocaitr.next();
					   		 		if (next_problem_option.getName().equals(next_merged_option.getName())) {
					   		 			// copy all the parameters (problem -> merged)
					   		 			if (next_problem_option.getMutable()){
					   		 				next_merged_option.setMutable(true);
					   		 			}
					   		 			if (next_problem_option.getValue() != null ){
					   		 				next_merged_option.setValue(next_problem_option.getValue());
					   		 			}
	
					   		 			ocaitr.set(next_merged_option);
					   		 		}
					   		 	}
				   		 	}
				   		 	// create jade.util.leap.ArrayList again
	   		 				ArrayList mergedOptionsArrayList = new ArrayList();
	   		 				mergedOptionsArrayList.fromList(mergedOptions);
				   		 	next_agent.setOptions(mergedOptionsArrayList);
	   		 			}
	   		 			else{
	   		 				// TODO remove the agent from the problem and let the use know
	   		 			}
	   		 		}
				}
	   	 		// display the options for a selected problem
	   		 	displayOptions(next_problem, message);
			}
		}	

	} //  end refreshOptions
	
	
	
	protected void setup(){
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		//Partial results handler
		addBehaviour(new CompAgentResultsServer(this)); 
		
	    // register with DF
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();   
		sd.setType(getAgentType()); 
		sd.setName(getName());
		dfd.setName(getAID());
		dfd.addServices(sd);
		
		//Name of general GUI service:
		sd = new ServiceDescription();   
	    sd.setType("GUIAgent"); 
	    sd.setName(getName());
	    dfd.addServices(sd);
	    
		try {
		    DFService.register(this,dfd);
		} catch (FIPAException e) {
		    System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
		    doDelete();
		}  
	  	System.out.println("GUI agent "+getLocalName()+" is alive and waiting...");
	  	
	  	
	  	mySetup();

	
	}  // end setup
	

	/* This behavior captures partial results from computating agents */
	protected class CompAgentResultsServer extends CyclicBehaviour{
		private MessageTemplate resMsgTemplate = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.INFORM),
			MessageTemplate.MatchConversationId("partial-results"));
		public CompAgentResultsServer(Agent agent) {
			super(agent);
		}

		@Override
		public void action() {
			ACLMessage msg = receive(resMsgTemplate);
			if (msg != null) {
				displayPartialResult(msg);
			}else{
				block();
			}
		}
	}
	
	
	protected void getProblemsFromXMLFile(String fileName) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder(); 
	    Document doc = builder.build(fileName);
	    Element root_element = doc.getRootElement();	    
	    
	    java.util.List _problems = root_element.getChildren("experiment"); // return all children by name
	    java.util.Iterator p_itr = _problems.iterator();	 
		while (p_itr.hasNext()) {
	           Element next_problem = (Element) p_itr.next();
	           
	           int p_id = createNewProblem(next_problem.getAttributeValue("timeout"));
	           
	           java.util.List dataset = next_problem.getChildren("dataset");
	           java.util.Iterator fn_itr = dataset.iterator();	 
	           while (fn_itr.hasNext()) {
	        	   Element next_dataset = (Element) fn_itr.next();
	        	   addDatasetToProblem(p_id, next_dataset.getAttributeValue("train"), next_dataset.getAttributeValue("test"));
	           }
	           
	           java.util.List _agents = next_problem.getChildren("agent");
	           java.util.Iterator a_itr = _agents.iterator();	 
	           while (a_itr.hasNext()) {
	        	   Element next_agent = (Element) a_itr.next();
	        	   
	        	   String agent_name = next_agent.getAttributeValue("name");
	        	   addAgentToProblem(p_id, agent_name);
	        	   
	        	   java.util.List _options = next_agent.getChildren("parameter");
		           java.util.Iterator o_itr = _options.iterator();	 
		           while (o_itr.hasNext()) {
		        	   Element next_option = (Element) o_itr.next();
		        	   addOptionToAgent(p_id,  agent_name, next_option.getAttributeValue("name"), next_option.getAttributeValue("value") );
		           }
	           }
		}
		
	}  // end _test_getProblemsFromXMLFile
	
	private void updateProblemId(String ids){
		String[] ID = ids.split(" ");
		String guiId = ID[0];
		String id = ID[1];
		// find problem with gui_id
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (next_problem.getGui_id().equals(guiId)){
				next_problem.setId(id);
			}
		}
	}
	
}