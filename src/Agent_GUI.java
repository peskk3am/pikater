import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Random;
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
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;


public abstract class Agent_GUI extends GuiAgent {	
	
	private String path = System.getProperty("user.dir")+System.getProperty("file.separator");
	
	private Codec codec = new SLCodec();
	private Ontology ontology = MessagesOntology.getInstance();
	
	private int default_timeout = 30000;  // 30s 
	
	protected Vector<Problem> problems = new Vector<Problem>();
		
	private int problem_id = 0;
	private int agent_id = 0;
	private int data_id = 0;
	
	private long timeout = 10000; 
	
	private int default_number_of_values_to_try = 10;
	private float default_error_rate = (float) 0.3;
	protected String default_method = "Random";
	private int default_maximum_tries = 10;
	
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
	
	protected abstract void displayOptions(Problem problem, int performative);
		/* method should be used to display agent options,
		 * it is called automatically after receiving the message from a computing agent
		 * performative ... ACLMessage.getPerformative
		 */
	
	protected abstract void displayResult(ACLMessage inform);
		/* method should be used to display the result,
		 * it is called automatically after receiving the message from a manager */
	
	protected abstract void mySetup();
		/* it should call
		 * ... int createNewProblem() - returns the _problem_id 
		 * ... addAgentToProblem(int _problem_id, String name, String type)
		 * 		- either name or type is set, the other parameter should be null
		 * 		- throws FailureExeption, if the agent could not be found / created 
		 * ... addAgentToProblemWekaStyle(int _problem_id, String agentName, String agentType, String [] agentParams)
		 * 		- similar to addAgentToProblem, but it adds also agent options 
		 * 		- throws FailureExeption, if the agent could not be found / created
		 * ... addOptionToAgent(int _problem_id, String agent_name, String option_name, String option_value )
		 * ... addFileToProblem(int _problem_id, String _fileName)
		 * ... addMethodToProblem(int problem_id, String name, String errorRate) - name...{ChooseXValue, Random}
		 * 
		 * ... getAgentOptions(String agentName) to receive the options from each computing agent 
		 * */
	
	protected abstract void allOptionsReceived(int problem_id);
		/* automatically called after all replies from computing agents are received */
	
	protected abstract void displayPartialResult(ACLMessage inform);
		/* Process the partial results received from computing agents 
		 *  maybe only the content would be better as a parameter */ 
	
	protected abstract void DisplayWrongOption(int problemGuiId, String agentName, String optionName, String errorMessage);
		/* This method should handle missing value of the agent option */
		 
	
	protected void setDefault_number_of_values_to_try(int number){
		/* default_number_of_values_to_try - when ChooseXValues method is selected;
		 *  should be set in GUI agent setup */
		default_number_of_values_to_try = number;
	}
	protected void setDefault_error_rate(double value){
		default_error_rate = (float) value;
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
			                	
			                	refreshOptions(agent, inform.getPerformative());			                	
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

				refreshOptions(agent, refuse.getPerformative());
				checkProblems();
				displayResult(refuse);
			}
			
			protected void handleFailure(ACLMessage failure) {
				
				String requestKey = (String)REQUEST_KEY;
				ACLMessage request = (ACLMessage) getDataStore().get(requestKey);
				Iterator receivers = request.getAllIntendedReceiver();
				String agentName = ((AID)receivers.next()).getLocalName();
				
				ontology.messages.Agent agent = new ontology.messages.Agent();
				agent.setName(agentName);
				
				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver
					// does not exist
					System.out.println("Agent "+myAgent.getLocalName()+"Responder "+agentName+" does not exist.");
				}
				else {
					System.out.println("Agent "+myAgent.getLocalName()+": Agent "+agentName+" failed to perform the requested action");
				}
				
				refreshOptions(agent, failure.getPerformative());
				checkProblems();
				displayResult(failure);

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
		
		msg.setConversationId(problem.getGui_id()+getLocalName());
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
				displayResult(refuse);
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
				displayResult(failure);
			}

		};
		
		addBehaviour(send_problem);
		
		problem.setSent(true);
		
		
		ACLMessage subscrmsg = new ACLMessage(ACLMessage.SUBSCRIBE);
		subscrmsg.addReceiver(new AID("manager", AID.ISLOCALNAME));    // TODO find manager in yellow pages
		subscrmsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		subscrmsg.setConversationId("subscription"+msg.getConversationId());
		subscrmsg.setLanguage(codec.getName());
		subscrmsg.setOntology(ontology.getName());
		
		SubscriptionInitiator receive_results = new SubscriptionInitiator(this, subscrmsg){
			// receive the sequence of replies
			
			protected void handleInform(ACLMessage inform) {
				System.out.println(getLocalName()+": Agent "+inform.getSender().getName()+" replied.");					
				displayResult(inform);				
				
			}

			protected void handleRefuse(ACLMessage refuse) {
				System.out.println(getLocalName()+": Agent "+refuse.getSender().getName()+" refused to perform the requested action");
				displayResult(refuse);
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
				displayResult(failure);
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
		
		Method method = new Method();
		method.setName(default_method);
		method.setError_rate(default_error_rate);
		method.setMaximum_tries(default_maximum_tries);
		problem.setMethod(method);
		
		problem.setTimeout(_timeout);
		problem.setAgents(new ArrayList());
		problem.setData(new ArrayList());
		problem.setSent(false);
 		problems.add(problem);
		
		return problem_id++;
	}
	
	
	/* protected void addAgentToProblemWekaStyle(int _problem_id, String agentName, String agentType,
		String agentParams) throws FailureException{
		
		addAgentToProblem(_problem_id, agentName, agentType, agentParams);				
	}
	*/
	
	protected int addAgentToProblem(int _problem_id, String name,
			String type, String optString) throws FailureException{		
		AID aid = null;
		String newName = null;
		
		if (type != null){
			if (type.contains("?")){
				addAgent(_problem_id, agent_id, name, type, optString);
				checkProblems();
				return agent_id++;
			}
		}
		
		long _timeout = timeout + System.currentTimeMillis();
		if (type != null){
			// create an agent of a given type to get its options
			while (aid == null && System.currentTimeMillis() < _timeout){
				// try until you find agent of the given type or you manage to create it
				 
				aid = getAgentByType(type);
				if (aid == null){
					// agent of given type doesn't exist
					newName = generateName(type);
					aid = createAgent("Agent_"+type, newName);
					doWait(100);
				}
			}
			if (aid == null){
				throw new FailureException("Agent of the "+type+" type could not be found or created.");
			}
			newName = aid.getLocalName();
		}
		
		if (name != null){
			// check if the agent exists
			if (!exists(newName)){
				throw new FailureException("Agent "+name+" could not be found.");
			}
			else{
				newName = name;
			}
		}
		
		addAgent(_problem_id, agent_id, name, type, optString);

		getAgentOptions(newName);
		
		return agent_id++;
	}

	protected boolean exists(String name){
		DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName(name);
        template.addServices(sd);
        try {
        	DFAgentDescription[] result = DFService.search(this, template);
        	if (result.length > 0){
        		return true;
        	}          
        }
        catch (FIPAException fe) {
          fe.printStackTrace();
        }
        return false;
	}
	
	protected void removeAgentFromAllProblems(int _agent_id){

		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
				// find the given agent
				Iterator itr = next_problem.getAgents().iterator();	 		   		 
	   		 	while (itr.hasNext()) {
	   		 		ontology.messages.Agent next_agent = (ontology.messages.Agent) itr.next();
	   		 		if (Integer.parseInt(next_agent.getGui_id()) == _agent_id){
	   		 			next_problem.getAgents().remove(next_agent);
	   		 		}
	   		 	}
			}
		}		
	}
	
	private String generateName(String agentType){
		int number = 0;
		String name = agentType + number;
		boolean success = false;
		while (!success){
			// try to find an agent with "name"
			DFAgentDescription template = new DFAgentDescription();
	        ServiceDescription sd = new ServiceDescription();
	        sd.setName(name);
	        template.addServices(sd);
	        try {
	        	DFAgentDescription[] result = DFService.search(this, template);
	        	// if the agent with this name already exists, increase number
	        	if (result.length > 0){
	        		number++;
	        		name = agentType + number;
	        	}          
	        	else {
	        		success = true;
	        		return name;
	        	}
	        }
	        catch (FIPAException fe) {
	          fe.printStackTrace();
	        }
		}
		return null;		
	}
		
	private void addAgent(int _problem_id, int _agent_id, String name, String type, String optString){
			
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
				if (Integer.parseInt(next_problem.getGui_id()) == _problem_id) {
					ontology.messages.Agent agent = new ontology.messages.Agent();	
					agent.setName(name);
					agent.setType(type);
					agent.setGui_id(Integer.toString(_agent_id));
					if (optString == null){
						agent.setOptions(new ArrayList());
					}
					else {
						List options = agent.stringToOptions(optString);
						Iterator it = options.iterator();
						
						while (it.hasNext()) {
							Option opt = (Option)it.next();
							opt.setNumber_of_values_to_try(default_number_of_values_to_try);
						}
						
						agent.setOptions(options);
						
					}
					List agents = next_problem.getAgents();
					agents.add(agent);
					next_problem.setAgents(agents);
				}
			}
		}
	}
	
	protected void addOptionToAgent(int _problem_id, int _agent_id, String option_name,
			String option_value, String lower, String upper, String number_of_values_to_try, String set){
		// TODO add interval ... 
		System.err.println("Add option to agent");
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
	
				if (Integer.parseInt(next_problem.getGui_id()) == _problem_id) {
					Iterator itr = next_problem.getAgents().iterator();	 		   		 
		   		 	while (itr.hasNext()) {
		   		 		ontology.messages.Agent next_agent = (ontology.messages.Agent) itr.next();
		   		 		// find the right agent
		   		 		if (Integer.parseInt(next_agent.getGui_id()) == _agent_id){		   		 			
		   		 			
		   		 			Option option = new Option();
		   		 			option.setName(option_name);
		   		 			
			   		 		if (option_value == null){
	   		 					option_value = "True";
	   		 				}
			   		 		
			   		 		if (option_value.indexOf("?") > -1){
			   		 		// if (option_value.equals("?")){
		   		 				option.setMutable(true);
		   		 				option.setUser_value(option_value);	
		   		 				if (lower != null && upper != null){
		   		 					Interval interval = new Interval();
		   		 					interval.setMin(Float.valueOf(lower));
		   		 					interval.setMax(Float.valueOf(upper));
		   		 					option.setRange(interval);
		   		 				}
		   		 			}	   		 				
			   		 		option.setValue(option_value);
			   		 		
		   		 		
			   		 		if (set != null){
			   		 			String[] set_array = (set.replace(" ", "")).split(",");
			   		 			List set_list = new ArrayList();
			   		 			for (int i=0; i<set_array.length; i++){
			   		 				set_list.add(set_array[i]);
			   		 			}
			   		 			option.setSet(set_list);
			   		 		}
			   		 		
			   		 		if (next_problem.getMethod().getName().equals("ChooseXValues")){
			   		 			if (number_of_values_to_try == null){
			   		 				option.setNumber_of_values_to_try(default_number_of_values_to_try);
			   		 			}
			   		 			else{
			   		 				option.setNumber_of_values_to_try(Integer.parseInt(number_of_values_to_try));
			   		 			}
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
	protected int addDatasetToProblem(int _problem_id, String _train, String _test, String _output, String _mode){		
		// get the problem
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
				if (Integer.parseInt(next_problem.getGui_id()) == _problem_id){
					List data = next_problem.getData();
					Data d = new Data();
					d.setExternal_test_file_name(_test);
					d.setExternal_train_file_name(_train);
					d.setTrain_file_name("data" + System.getProperty("file.separator") + "files" + System.getProperty("file.separator") + DataManagerService.translateFilename(this, 1, _train, null));
					d.setTest_file_name("data" + System.getProperty("file.separator") + "files" + System.getProperty("file.separator") + DataManagerService.translateFilename(this, 1, _test, null));
					if (_output != null){
						d.setOutput(_output);
					}
					if (_mode != null){
						d.setMode(_mode);
					}
					data.add(d);
			        next_problem.setData(data);
				}
			}
		}
		return data_id++;
	}
	
	protected void addMetadataToDataset(int d_id, String file_name, String missing_values, String number_of_attributes,
	   			String number_of_instances, String attribute_type, String default_task ){
		
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			
			Iterator itr = next_problem.getData().iterator();	 		   		 
   		 	while (itr.hasNext()){
   		 		Data next_data = (Data) itr.next();
   		 		if (next_data.getGui_id() == d_id){
   		 			Metadata m = new Metadata();
   		 			m.setAttribute_type(attribute_type);
   		 			m.setDefault_task(default_task);
   		 			m.setExternal_name(file_name);
   		 			if (missing_values != null){
	   		 			if (missing_values.equals("True")){
	   		 				m.setMissing_values(true);
	   		 			}
	   		 			else {
	   		 				m.setMissing_values(false);
	   		 			}
   		 			}
   		 			m.setNumber_of_attributes(Integer.parseInt(number_of_attributes));
   		 			m.setNumber_of_instances(Integer.parseInt(number_of_instances));
   		 			
   		 			next_data.setMetadata(m);
   		 		}
   		 	}
		}
	}
	
	protected void addMethodToProblem(int problem_id, String name, String errorRate, String maximumTries){
		// get the problem
		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {
			Problem next_problem = (Problem)pe.nextElement();
			if (Integer.parseInt(next_problem.getGui_id()) == problem_id
					&& !next_problem.getSent()){
				
				Method method = new Method();
				method.setName(name);
				
				if (name.equals("Random")){
					if (errorRate == null){
						method.setError_rate(default_error_rate);
					}
					else{
						method.setError_rate(Float.parseFloat(errorRate));	
					}
					if (maximumTries == null){
						method.setMaximum_tries(default_maximum_tries);
					}
					else{
						method.setMaximum_tries(Integer.parseInt(maximumTries));	
					}
				}	
				next_problem.setMethod(method);
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
	   		 		
	   		 		String type = "";
	   		 		if (next_agent.getType() != null){ type = next_agent.getType();}
	   		 		
	   		 		if (!(type.contains("?") && next_agent.getName() == null)){
		   		 	// if we will recomend the type of the agent, we don't wait for the options to be received
	   		 			if (next_agent.getOptions() == null) {next_agent.setOptions(new ArrayList());}
	   		 			if (next_agent.getOptions().size() > 0 ){ // if there is at least one option
			   		 		// if data_type is set it means that the options from a computing agent have
			   		 		// been received already
			   		 		// it's enough to test the first option
			   	   		 	if ( ((Option)(next_agent.getOptions().iterator().next())).getData_type() == null ){
			   	   		 		done = false;
			   		 		}
			   		 	}
	   		 		}
	   		 	}
	   			if (done){
	   				allOptionsReceived(Integer.parseInt(next_problem.getGui_id()));
	   			}
			}
		}
	}
	
	private void refreshOptions(ontology.messages.Agent agent, int performative) {
		// refresh options in all problems, where the agent is involved

		for (Enumeration pe = problems.elements() ; pe.hasMoreElements() ;) {	
			Problem next_problem = (Problem)pe.nextElement();
			if (!next_problem.getSent()){
				if (performative == ACLMessage.INFORM) { 			
					
					Iterator aitr = next_problem.getAgents().iterator();	 		   		 
		   		 	while (aitr.hasNext()) {
		   		 		ontology.messages.Agent next_agent = (ontology.messages.Agent) aitr.next(); 		 	
						
		   		 		// all problems where the agent (input parameter) figures
		   		 		if (next_agent.getName() != null){
			   		 		if ( next_agent.getName().equals(agent.getName()) ){
			   		 			next_agent.setType(agent.getType());
			   		 			next_agent.setOptions(_refreshOptions(next_agent, agent, next_problem));
			   		 			// System.out.println("DT "+((Option)(next_agent.getOptions().iterator().next())).getData_type());
			   		 		}
						} // end if getName != null
		   		 		if (next_agent.getType() != null){
		   		 			// System.out.println("type1 "+next_agent.getType()+" type2 "+agent.getType());
		   		 			if ( next_agent.getType().equals(agent.getType()) ){
			   		 			next_agent.setOptions(_refreshOptions(next_agent, agent, next_problem));
			   		 		}
		   		 		} // end if getType != null
		   		 	}  // end while - iterate over agents
				}  // end if performative = inform

	 			else{
	 				// TODO remove the agent from the problem and let the user know	 				
	 				removeAgentFromAllProblems(Integer.parseInt(agent.getGui_id()));
	 			}
				// display the options for a selected problem
	   		 	displayOptions(next_problem, performative);
			}  // end if ! sent
		}	
	} //  end refreshOptions
	
	private List _refreshOptions(ontology.messages.Agent next_agent, ontology.messages.Agent agent,
			Problem next_problem){
		List newOptions = null;
		
		if (agent.getOptions() != null) {
	 		// update the options (merge them)
	 			
			// copy agent's options
			java.util.List mergedOptions = new java.util.ArrayList();					
			Iterator oitr = agent.getOptions().iterator();	 		   		 
   		 	while (oitr.hasNext()) {
   		 		Option next_option = (Option) oitr.next();
   		 		// next_option.setValue(next_option.getDefault_value());
   		 		Option o = new Option();
   		 		o.setData_type(next_option.getData_type());
   		 		o.setDefault_value(next_option.getDefault_value());
   		 		o.setIs_a_set(next_option.getIs_a_set());
   		 		o.setName(next_option.getName());
   		 		o.setNumber_of_args(next_option.getNumber_of_args());
   		 		o.setRange(next_option.getRange());
   		 		o.setSet(next_option.getSet());
   		 		o.setValue(next_option.getDefault_value());
   		 		// mergedOptions.add(next_option);
   		 		mergedOptions.add(o);
   		 	}
			
			// go through the options set in the problem 
   		 	// and replace the options send by an computing agent
			Iterator opitr = next_agent.getOptions().iterator();	 		   		 
   		 	while (opitr.hasNext()) {
   		 		Option next_problem_option = (Option) opitr.next();
	   		 	ListIterator ocaitr = mergedOptions.listIterator();	 		   		 
	   		 	while (ocaitr.hasNext()) {
	   		 		Option next_merged_option = (Option) ocaitr.next();
	   		 		if (next_problem_option.getName().equals(next_merged_option.getName())
	   		 				//&& (next_problem_option.getValue() != null 
	   		 				//	|| next_problem_option.getUser_value() != null )
	   		 			) {
	   		 			// copy all the parameters (problem -> merged)	   		 				   		 				   		 			
	   		 			if (next_problem_option.getMutable()){
	   		 				next_merged_option.setMutable(true);
	   		 				next_merged_option.setUser_value(next_problem_option.getValue());
	   		 				if (next_problem_option.getRange() != null){
	   		 					next_merged_option.getRange().setMin(next_problem_option.getRange().getMin());
	   		 					next_merged_option.getRange().setMax(next_problem_option.getRange().getMax());
	   		 				}
	   		 				next_merged_option.setNumber_of_values_to_try(
	   		 						next_problem_option.getNumber_of_values_to_try() );
	   		 			}
	   		 			// check the value
	   		 			if (!next_merged_option.getData_type().equals("BOOLEAN")
	   		 					&& next_problem_option.getValue().equals("True")){
	   		 				DisplayWrongOption(Integer.parseInt(next_problem.getGui_id()),
	   		 						next_agent.getName(), next_problem_option.getName(),
	   		 						next_problem_option.getName()+ " is not a BOOLEAN type option.");
	   		 			}
	   		 			else{
   		 					next_merged_option.setValue(next_problem_option.getValue());
	   		 			}
	   		 			
	   		 			if (next_problem_option.getSet() != null){
	   		 				next_merged_option.setSet(next_problem_option.getSet());
	   		 			}
	   		 			
	   		 			if (next_problem_option.getNumber_of_args() != null){
	   		 				next_merged_option.setNumber_of_args(next_problem_option.getNumber_of_args());
	   		 			}
	   		 				
	   		 			ocaitr.set(next_merged_option);
	   		 		}
	   		 	}
   		 	}  // end while - iterate over options
   		 	// create jade.util.leap.ArrayList again
			ArrayList mergedOptionsArrayList = new ArrayList();
			mergedOptionsArrayList.fromList(mergedOptions);
   		 	// next_agent.setOptions(mergedOptionsArrayList);
   		 	newOptions = mergedOptionsArrayList;
		} // end if (empty option list)
		return newOptions;
	}  // end function refreshOption
		
		
	protected Vector<String> offerAgentTypes(){
		// read agent types from file
		 	
			Vector<String> AgentTypes = new Vector<String>();
			
			//  Sets up a file reader to read the agent_types file 
			FileReader input;
			try {
				input = new FileReader(path+"agent_types");
	            // Filter FileReader through a Buffered read to read a line at a time
	            BufferedReader bufRead = new BufferedReader(input);
	            String line = bufRead.readLine();
	         
	            // Read through file one line at time
	            while (line != null){
	            	AgentTypes.add(line);
	                line = bufRead.readLine();
	            }
	            
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            return AgentTypes;            
	}
	
	protected AID createAgent(String type, String name){
		// get a container controller for creating new agents
		PlatformController container = getContainerController();
		
		try{	
			AgentController agent = container.createNewAgent(name, type, new Object[0]);
			agent.start();
			return new AID((String) name, AID.ISLOCALNAME);
		}
		catch (ControllerException e) {
	        // System.err.println( "Exception while adding agent: " + e );
	        // e.printStackTrace();
	        return null;
	    }	
	}
	
	protected AID getAgentByType(String agentType){
		 AID[] Agents;
		 
		 // Make the list of agents of given type
		 DFAgentDescription template = new DFAgentDescription();
         ServiceDescription sd = new ServiceDescription();
         sd.setType(agentType);
         template.addServices(sd);
         try {
         	DFAgentDescription[] result = DFService.search(this, template); 
         	System.out.println("Found the following "+agentType+" agents:");
         	Agents = new AID[result.length];
           
           for (int i = 0; i < result.length; ++i) {
        	   Agents[i] = result[i].getName();
	          	System.out.println(Agents[i].getName());
           }

           if (Agents.length > 0){
               // choose one
               Random generator = new Random();
        	   int rnd = generator.nextInt(Agents.length);
	           return Agents[rnd];
           }
           else {
        	   return null;
           }
         }
         catch (FIPAException fe) {
             fe.printStackTrace();
             return null;
         }
	}
	
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
  	
		String incomingFilesPath = System.getProperty("user.dir") + System.getProperty("file.separator") + "incoming" +System.getProperty("file.separator");
		File incomingFiles = new File(incomingFilesPath);
		
		for (String fileName : incomingFiles.list()) {
			DataManagerService.importFile(this, 1, fileName, null);
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
	           
	           java.util.List method = next_problem.getChildren("method");
	           java.util.Iterator m_itr = method.iterator();	
	           if (method.size() == 0 ){
	        	   // TODO select default
	           }
	           if (method.size() > 1 ){
	        	   // TODO error
	           }
	           while (m_itr.hasNext()) {
	        	   Element next_method = (Element) m_itr.next();
	        	   addMethodToProblem(p_id, next_method.getAttributeValue("name"),
	        			   next_method.getAttributeValue("error_rate"), next_method.getAttributeValue("maximum_tries"));
	           }
	           
	           java.util.List dataset = next_problem.getChildren("dataset");
	           java.util.Iterator ds_itr = dataset.iterator();	 
	           while (ds_itr.hasNext()) {
	        	   Element next_dataset = (Element) ds_itr.next();
	        	   int d_id = addDatasetToProblem(p_id, next_dataset.getAttributeValue("train"),
	        			   next_dataset.getAttributeValue("test"),
	        			   next_dataset.getAttributeValue("output"),
	        			   next_dataset.getAttributeValue("mode"));
	        	   
	        	   java.util.List metadata = next_dataset.getChildren("metadata");
		           if (metadata.size() > 0){
		        	   java.util.Iterator md_itr = metadata.iterator();
			           Element next_metadata = (Element)md_itr.next();
			      
			           addMetadataToDataset(d_id, next_dataset.getAttributeValue("train"),
		        			   next_metadata.getAttributeValue("missing_values"),
		        			   next_metadata.getAttributeValue("number_of_attributes"),
		        			   next_metadata.getAttributeValue("number_of_instances"),
		        			   next_metadata.getAttributeValue("attribute_type"),
		        			   next_metadata.getAttributeValue("default_task")
		        	   );
		           }	        	   
	           }
	           
	           java.util.List _agents = next_problem.getChildren("agent");
	           java.util.Iterator a_itr = _agents.iterator();	 
	           while (a_itr.hasNext()) {
	        	   Element next_agent = (Element) a_itr.next();
	        	   
	        	   String agent_name = next_agent.getAttributeValue("name");
	           	   String agent_type = next_agent.getAttributeValue("type");
	           	   int a_id = -1;
	           	   try {
	           		   a_id = addAgentToProblem(p_id, agent_name, agent_type, null);
	           	   } catch (FailureException e) {
	           		   System.err.println(e.getLocalizedMessage());
	           		   // e.printStackTrace();
	           	   } 

	 
	        	   java.util.List _options = next_agent.getChildren("parameter");
		           java.util.Iterator o_itr = _options.iterator();	 
		           while (o_itr.hasNext()) {
		        	   Element next_option = (Element) o_itr.next();
		        	   addOptionToAgent(p_id, a_id, next_option.getAttributeValue("name"),
		        			   next_option.getAttributeValue("value"),
		        			   next_option.getAttributeValue("lower"), next_option.getAttributeValue("upper"),
		        			   next_option.getAttributeValue("number_of_values_to_try"),
		        			   next_option.getAttributeValue("set") );
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