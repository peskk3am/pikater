package ontology.webservices;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;


public class WS_Ontology extends Ontology {

	private static final long serialVersionUID = 1L;
	
	public static final String ONTOLOGY_NAME = "WS_Ontology";
	public static final String SET_PROBLEM = "SetProblem";
	public static final String THE_PROBLEM = "theProblem";
	
	private static Ontology theInstance = new WS_Ontology();
	
	public static Ontology getInstance() {

		return theInstance;
	}
	
	private WS_Ontology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance());
		try {
			add(new AgentActionSchema(SET_PROBLEM), SetProblem.class);
			add(new ConceptSchema(ontology.messages.MessagesOntology.PROBLEM), ontology.messages.Problem.class);
			
			AgentActionSchema as = (AgentActionSchema)getSchema(SET_PROBLEM);
			as.add(THE_PROBLEM, (ConceptSchema)getSchema(ontology.messages.MessagesOntology.PROBLEM));
			
		}
		catch (Exception e) {}
	}
	

}
