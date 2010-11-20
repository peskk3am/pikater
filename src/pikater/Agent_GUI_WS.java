package pikater;

import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

import java.util.Vector;

import pikater.ontology.messages.Agent;
import pikater.ontology.messages.Option;
import pikater.ontology.messages.Problem;
import pikater.ontology.messages.Results;
import pikater.ontology.messages.Task;
import pikater.ontology.webservices.GetAgents;
import pikater.ontology.webservices.GetOptions;
import pikater.ontology.webservices.GetResults;
import pikater.ontology.webservices.SetProblem;
import pikater.ontology.webservices.WS_Ontology;
import pikater.ontology.webservices.WS_Results;

public class Agent_GUI_WS extends Agent_GUI {

	private static final long serialVersionUID = -5322630455326259706L;
	private jade.util.leap.LinkedList results = new jade.util.leap.LinkedList();
	private Codec codec;
	private Ontology wsOntology;
	private Ontology messagesOntology;

	@Override
	protected void displayResult(ACLMessage inform) {
		displayPartialResult(inform);
		System.out.println("Displaying the result ;)");
	}

	@Override
	protected String getAgentType() {
		return "WS GUI Agent";
	}

	@Override
	protected void mySetup() {

		wsOntology = pikater.ontology.webservices.WS_Ontology.getInstance();
		messagesOntology = pikater.ontology.messages.MessagesOntology
				.getInstance();
		codec = new SLCodec();
		getContentManager().registerOntology(wsOntology);
		getContentManager().registerOntology(messagesOntology);

		DFAgentDescription df = new DFAgentDescription();
		df.setName(this.getAID());

		// TODO: fix this
		try {
			df = DFService.search(this, df)[0];
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		ServiceDescription sd = new ServiceDescription();
		sd
				.addOntologies(pikater.ontology.webservices.WS_Ontology.ONTOLOGY_NAME);
		sd.addLanguages(codec.getName());
		sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
		sd.addProperties(new Property("wsig", "true"));
		sd.setType("WS_GUI_Agent");
		sd.setName("WS_GUI");

		df.addOntologies(wsOntology.getName());
		df.addServices(sd);

		try {
			DFService.modify(this, df);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		MessageTemplate mt = MessageTemplate
				.MatchOntology(WS_Ontology.ONTOLOGY_NAME);

		addBehaviour(new AchieveREResponder(this, mt) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5929855198528929475L;

			@Override
			protected ACLMessage prepareResponse(ACLMessage request)
					throws NotUnderstoodException, RefuseException {
				return null;
			}

			@Override
			protected ACLMessage handleRequest(ACLMessage request) {

				try {
					System.err.print("Agent " + myAgent.getName() + ":");

					Action a = (Action) getContentManager().extractContent(
							request);

					if (a.getAction() instanceof SetProblem) {
						SetProblem sp = (SetProblem) (a.getAction());

						jade.util.leap.Iterator it = sp.getFileNames()
								.iterator();

						int problemID = createNewProblem("30000");

						while (it.hasNext()) {
							String s = (String) it.next();
							addDatasetToProblem(problemID, s, s, null, null);
						}

						it = sp.getAgentDescriptions().iterator();

						try {
							while (it.hasNext()) {
								String[] params = ((String) it.next()).split(
										"[ ]+", 2);
								addAgentToProblem(problemID, null, params[0],
										params[1]);
							}
						} catch (FailureException e) {
							e.printStackTrace();
						}

						ACLMessage response = request.createReply();
						response.setPerformative(ACLMessage.INFORM);
						response.setContent("OK");

						return response;

					} else if (a.getAction() instanceof GetAgents) {

						// String[] agents = getComputingAgents();
						Vector<String> agents = offerAgentTypes();

						jade.util.leap.ArrayList agentsList = new ArrayList();

						for (String s : agents) {
							agentsList.add(s);
						}

						ACLMessage response = request.createReply();
						response.setPerformative(ACLMessage.INFORM);
						Result r = new Result(a.getAction(), agentsList);

						getContentManager().fillContent(response, r);

						return response;
					} else if (a.getAction() instanceof GetResults) {

						ACLMessage response = request.createReply();
						response.setPerformative(ACLMessage.INFORM);

						ArrayList res = new ArrayList();

						Iterator it = results.iterator();

						while (it.hasNext()) {
							Results r = (Results) it.next();

							Iterator rit = r.getResults().iterator();

							while (rit.hasNext()) {
								Task t = (Task) rit.next();

								WS_Results wres = new WS_Results();
								wres.setOptions(t.getAgent().optionsToString());
								wres
										.setErrorRate(t.getResult()
												.getError_rate());
								// wres.setPctIncorrect(t.getResult().getPct_incorrect());

								res.add(wres);

							}

						}

						Result r = new Result(a.getAction(), res);

						getContentManager().fillContent(response, r);

						return response;
					} else if (a.getAction() instanceof GetOptions) {

						ACLMessage response = request.createReply();

						GetOptions go = (GetOptions) a.getAction();
						String agentName = getAgentByType(go.getAgentName())
								.getLocalName();

						Agent ag = onlyGetAgentOptions(agentName);

						if (ag == null) {
							response.setPerformative(ACLMessage.FAILURE);
							return response;
						}

						List mOptions = ag.getOptions();
						ArrayList options = new ArrayList();

						Iterator it = mOptions.iterator();

						while (it.hasNext()) {
							pikater.ontology.messages.Option o = (pikater.ontology.messages.Option) it
									.next();
							String name = o.getName();
							String synopsis = o.getSynopsis();
							String description = o.getDescription();
							String value = o.getValue();

							options
									.add(new pikater.ontology.webservices.Option(
											name, synopsis, description, value));
						}

						response.setPerformative(ACLMessage.INFORM);
						Result r = new Result(a.getAction(), options);

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

	private Agent onlyGetAgentOptions(String agent) {

		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(new AID(agent, AID.ISLOCALNAME));

		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		request.setLanguage(codec.getName());
		request.setOntology(messagesOntology.getName());

		pikater.ontology.messages.GetOptions get = new pikater.ontology.messages.GetOptions();
		Action a = new Action();
		a.setAction(get);
		a.setActor(this.getAID());

		try {
			// Let JADE convert from Java objects to string
			getContentManager().fillContent(request, a);

			ACLMessage inform = FIPAService.doFipaRequestClient(this, request);

			if (inform == null) {
				return null;
			}

			Result r = (Result) getContentManager().extractContent(inform);

			return (Agent) r.getItems().get(0);

		} catch (CodecException ce) {
			ce.printStackTrace();
		} catch (OntologyException oe) {
			oe.printStackTrace();
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		return null;

	}

	@Override
	protected void displayPartialResult(ACLMessage inform) {
		try {
			Result r = (Result) getContentManager().extractContent(inform);
			results.add(r.getValue());
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

	@Override
	protected void allOptionsReceived(int problemId) {
		sendProblem(problemId);
	}

	@Override
	protected void DisplayWrongOption(int problemGuiId, String agentName,
			String optionName, String errorMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void displayOptions(Problem problem, int performative) {
		// TODO Auto-generated method stub

	}

	private String optionsToWekaString(List options) {

		Iterator it = options.iterator();
		String str = "";

		while (it.hasNext()) {
			Option o = (Option) it.next();
			str += "-" + o.getName() + " " + o.getValue() + " ";
		}

		return str.trim();
	}

	@Override
	protected void onGuiEvent(GuiEvent arg0) {
		// TODO Auto-generated method stub

	}

}
