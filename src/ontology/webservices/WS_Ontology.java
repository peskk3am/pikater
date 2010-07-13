package ontology.webservices;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
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
	public static final String GET_RESULTS = "get-results";
	public static final String GET_OPTIONS = "get-options";
	public static final String AGENT_NAME = "agentName";
	public static final String OPTION = "option";
	public static final String OPTION_VALUE = "value";
	public static final String OPTION_SYNOPIS = "synopsis";
	public static final String OPTION_NAME = "name";
	public static final String OPTION_DESCRIPTION = "description";
	public static final String RESULTS = "results";
	public static final String RESULT_OPTIONS = "options";
	public static final String RESULT_ERROR_RATE = "errorRate";
	public static final String RESULT_PCT_INCORRECT = "pctIncorrect";
	
	private static Ontology theInstance = new WS_Ontology();
	
	public static Ontology getInstance() {

		return theInstance;
	}
	
	private WS_Ontology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance());
		
		try {
			//add(MessagesOntology.getInstance().getSchema(ontology.messages.Results.class));
			//add(MessagesOntology.getInstance().getSchema(ontology.messages.Agent.class));
			add(new ConceptSchema(OPTION), Option.class);
			add(new ConceptSchema(RESULTS), WS_Results.class);
			add(new AgentActionSchema(SET_PROBLEM), SetProblem.class);
			add(new AgentActionSchema(GET_AGENTS), GetAgents.class);
			add(new AgentActionSchema(GET_RESULTS), GetResults.class);
			add(new AgentActionSchema(GET_OPTIONS), GetOptions.class);
			
			ConceptSchema cs = (ConceptSchema)getSchema(OPTION);
			cs.add(OPTION_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(OPTION_DESCRIPTION, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(OPTION_SYNOPIS, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(OPTION_VALUE, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			
			cs = (ConceptSchema)getSchema(RESULTS);
			cs.add(RESULT_OPTIONS, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(RESULT_ERROR_RATE, (PrimitiveSchema)getSchema(BasicOntology.FLOAT));
			cs.add(RESULT_PCT_INCORRECT, (PrimitiveSchema)getSchema(BasicOntology.FLOAT));
			
			AgentActionSchema as = (AgentActionSchema)getSchema(SET_PROBLEM);
			as.add(AGENT_DESCRIPTIONS, (PrimitiveSchema)getSchema(BasicOntology.STRING), 1, ObjectSchema.UNLIMITED);
			as.add(FILE_NAMES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 1, ObjectSchema.UNLIMITED);
			as.setResult((PrimitiveSchema)getSchema(BasicOntology.STRING));
			
			as = (AgentActionSchema)getSchema(GET_AGENTS);
			as.setResult((PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
			
			as = (AgentActionSchema)getSchema(GET_RESULTS);
			as.setResult((ConceptSchema)getSchema(RESULTS), 0, ObjectSchema.UNLIMITED);
			
			as = (AgentActionSchema)getSchema(GET_OPTIONS);
			as.add(AGENT_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			as.setResult((ConceptSchema)getSchema(OPTION), 0, ObjectSchema.UNLIMITED);
			
		}
		catch (Exception e) {}
	}
	

}
