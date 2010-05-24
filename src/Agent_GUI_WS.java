import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import ontology.messages.Agent;


public class Agent_GUI_WS extends Agent_GUI {

	private static final long serialVersionUID = -5322630455326259706L;

	@Override
	protected void allOptionsReceived() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void displayOptions(Agent agent) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void displayResult(ACLMessage inform) {
		// TODO Auto-generated method stub

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
		sd.setName("SetProblem");
		
		df.addOntologies(o.getName());
		df.addServices(sd);
		
		try {
			DFService.modify(this, df);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
	}

}
