package ontology.webservices;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PrimitiveSchema;
import ontology.messages.*;


public class WS_Ontology extends Ontology {

	private static final long serialVersionUID = 1L;
	
	public static final String ONTOLOGY_NAME = "WS_Ontology";
	public static final String SET_PROBLEM = "set-problem";
	public static final String AGENT_DESCRIPTIONS = "agentDescriptions";
	public static final String FILE_NAMES = "fileNames";
	public static final String GET_AGENTS = "get-agents";
	
	private static Ontology theInstance = new WS_Ontology();
	
	public static Ontology getInstance() {

		return theInstance;
	}
	
	private WS_Ontology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance());
		
		try {
			add(new AgentActionSchema(SET_PROBLEM), SetProblem.class);
			add(new AgentActionSchema(GET_AGENTS), GetAgents.class);
			
			AgentActionSchema as = (AgentActionSchema)getSchema(SET_PROBLEM);
			as.add(AGENT_DESCRIPTIONS, (PrimitiveSchema)getSchema(BasicOntology.STRING), 1, ObjectSchema.UNLIMITED);
			as.add(FILE_NAMES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 1, ObjectSchema.UNLIMITED);
			
			as = (AgentActionSchema)getSchema(GET_AGENTS);
			as.setResult((PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
			
		}
		catch (Exception e) {}
	}
	

}
