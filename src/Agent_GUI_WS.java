import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import ontology.messages.Agent;
import ontology.webservices.GetAgents;
import ontology.webservices.SetProblem;
import ontology.webservices.WS_Ontology;


public class Agent_GUI_WS extends Agent_GUI {

	private static final long serialVersionUID = -5322630455326259706L;

	@Override
	protected void allOptionsReceived() {
		sendProblem();
	}

	@Override
	protected void displayOptions(Agent agent) {
		refreshOptions(agent);
	}

	@Override
	protected void displayResult(ACLMessage inform) {
		System.out.println("Displaying the result ;)");
	}

	@Override
	protected String getAgentType() {
		return "WS GUI Agent";
	}

	@Override
	protected void mySetup() {
		
		Ontology o = ontology.webservices.WS_Ontology.getInstance();
		Codec c = new SLCodec();
		getContentManager().registerOntology(o);
		
		DFAgentDescription df = new DFAgentDescription();
		df.setName(this.getAID());
		
		//TODO: fix this
		try {
			df = DFService.search(this, df)[0];
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		ServiceDescription sd = new ServiceDescription();
		sd.addOntologies(ontology.webservices.WS_Ontology.ONTOLOGY_NAME);
		sd.addLanguages(c.getName());
		sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
		sd.addProperties(new Property("wsig", "true"));
		sd.setType("WS_GUI_Agent");
		sd.setName("WS_GUI");
		
		df.addOntologies(o.getName());
		df.addServices(sd);
		
		try {
			DFService.modify(this, df);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		MessageTemplate mt = MessageTemplate.MatchOntology(WS_Ontology.ONTOLOGY_NAME);
		
		addBehaviour(new AchieveREResponder(this, mt) {
			
			@Override
			protected ACLMessage prepareResponse(ACLMessage request)
					throws NotUnderstoodException, RefuseException {
				return null;
			}
			
			@Override
			protected ACLMessage handleRequest(ACLMessage request) {
				
				try {
					System.err.print("Agent " + myAgent.getName() + ":");
					
					Action a = (Action)getContentManager().extractContent(request);
					
					if ( a.getAction() instanceof SetProblem) {
						SetProblem sp = (SetProblem)(a.getAction());
						
						jade.util.leap.Iterator it = sp.getFileNames().iterator();
						
						while (it.hasNext()) {
							String s = (String)it.next();
							addFileToProblem(s);
						}
						
						it = sp.getAgentDescriptions().iterator();
						
						while (it.hasNext()) {
							String[] params =  ((String)it.next()).split("[ ]+");
							addAgentToProblem(params);
							getAgentOptions(params[0]);
						}
						
						ACLMessage response = request.createReply();
						response.setPerformative(ACLMessage.INFORM);
						response.setContent("OK");
						
						return response;
						
					}
					else if (a.getAction() instanceof GetAgents) {
						
						String[] agents = getComputingAgents();
						
						jade.util.leap.ArrayList agentsList = new ArrayList();
						
						for (String s: agents) {
							agentsList.add(s);
						}
						
						ACLMessage response = request.createReply();
						response.setPerformative(ACLMessage.INFORM);
						Result r = new Result(a.getAction(), agentsList);
						
						getContentManager().fillContent(response, r);
						
						return response;
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
			
		
		});
		
	}

	@Override
	protected void displayPartialResult(ACLMessage inform) {
		// TODO Auto-generated method stub
		
	}

}
