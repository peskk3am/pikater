package ontology;

import jade.content.onto.*;
import jade.content.schema.*;

public class O_Ontology extends Ontology{
	// The name identifying this ontology
	public static final String ONTOLOGY_NAME = "O_Ontology";

	// VOCABULARY
	public static final String COMPUTING_AGENT = "Computing Agent";
	public static final String COMPUTING_AGENT_AID = "AID";
	public static final String COMPUTING_AGENT_NAME = "name";	

	public static final String RBF_NETWORK = "RBF Network";

	public static final String MULTILAYER_PERCEPTRON = "Multilayer Perceptron";


	// The singleton instance of this ontology
	private static Ontology theInstance = new O_Ontology();
	
	// This is the method to access the singleton O_Ontology object
	public static Ontology getInstance() {
		return theInstance;
	}
	
	// Private constructor
	private O_Ontology() {
		// The O_Ontology extends the basic ontology
		super(ONTOLOGY_NAME, BasicOntology.getInstance());
		try {
			add(new ConceptSchema(COMPUTING_AGENT), ComputingAgent.class);
			add(new ConceptSchema(RBF_NETWORK), RBFNetwork.class);
			add(new ConceptSchema(MULTILAYER_PERCEPTRON), MultilayerPerceptron.class);

			// Structure of the schema for the Computing Agent concept
			ConceptSchema cs = (ConceptSchema) getSchema(COMPUTING_AGENT);
			cs.add(COMPUTING_AGENT_NAME, (PrimitiveSchema) getSchema(BasicOntology.AID));

			
			// Structure of the schema for the RBF Network concept
			cs = (ConceptSchema) getSchema(RBF_NETWORK);
			cs.addSuperSchema((ConceptSchema) getSchema(COMPUTING_AGENT));
			
			// Structure of the schema for the Multilayer Perceptron concept
			cs = (ConceptSchema) getSchema(MULTILAYER_PERCEPTRON);
			cs.addSuperSchema((ConceptSchema) getSchema(COMPUTING_AGENT));		
		}
		catch (OntologyException oe) {
			oe.printStackTrace();
		}
	} // end constructor

}
