package ontology.messages;

import jade.content.onto.*;
import jade.content.schema.*;
import java.util.*;

import javax.print.attribute.SupportedValuesAttribute;


public class MessagesOntology extends Ontology {

	  
	  //  A symbolic constant, containing the name of this ontology.
	  
	  public static final String NAME = "messages-ontology";

	  // VOCABULARY
	  // Concepts

	  public static final String TASK = "TASK";
	  public static final String TASK_ID = "id";
	  public static final String TASK_COMPUTATION_ID = "computation_id";
	  public static final String TASK_PROBLEM_ID = "problem_id";
	  public static final String TASK_AGENT = "agent";
	  public static final String TASK_DATA = "data";
	  public static final String TASK_RESULT = "result";
	  
	  public static final String DATA = "data";
	  public static final String DATA_TRAIN_FILE_NAME = "train_file_name";
	  public static final String DATA_TEST_FILE_NAME = "test_file_name";
	  public static final String DATA_EXTERNAL_TRAIN_FILE_NAME = "external_train_file_name";
	  public static final String DATA_EXTERNAL_TEST_FILE_NAME = "external_test_file_name";
	  public static final String DATA_METADATA = "metadata";
	  public static final String DATA_OUTPUT = "output";
	  public static final String DATA_MODE = "mode";
	  
	  public static final String COMPUTATION = "COMPUTATION";
	  public static final String COMPUTATION_ID = "id";
	  public static final String COMPUTATION_PROBLEM_ID = "problem_id";
	  public static final String COMPUTATION_AGENT = "agent";
	  public static final String COMPUTATION_DATA = "data";
	  public static final String COMPUTATION_TIMEOUT = "timeout";
	  public static final String COMPUTATION_METHOD = "method";
	  
	  public static final String PROBLEM = "PROBLEM";
	  public static final String PROBLEM_ID = "id";
	  public static final String PROBLEM_GUI_ID = "gui_id";
	  public static final String PROBLEM_SENT = "sent";
	  public static final String PROBLEM_AGENTS = "agents";
	  public static final String PROBLEM_DATA = "data";
	  public static final String PROBLEM_TIMEOUT = "timeout";
	  public static final String PROBLEM_METHOD = "method";

	  public static final String METHOD = "METHOD";
	  public static final String METHOD_NAME = "name";
	  public static final String METHOD_ERROR_RATE = "error_rate";
	  public static final String METHOD_MAXIMUM_TRIES = "maximum_tries";

	  public static final String EVALUATION = "EVALUATION";
	  public static final String EVALUATION_ERROR_RATE = "error_rate";
	  public static final String EVALUATION_KAPPA_STATISTIC = "kappa_statistic";
	  public static final String EVALUATION_MEAN_ABSOLUTE_ERROR = "mean_absolute_error";
	  public static final String EVALUATION_MEAN_SQUARED_ERROR = "root_mean_squared_error";
	  public static final String EVALUATION_RELATIVE_ABSOLUTE_ERROR = "relative_absolute_error";
	  public static final String EVALUATION_RELATIVE_SQUARED_ERROR = "root_relative_squared_error";
	  public static final String EVALUATION_DATA_TABLE = "data_table";
	  
	  public static final String RESULTS = "RESULTS";
	  public static final String RESULTS_COMPUTATION_ID = "computation_id";
	  public static final String RESULTS_PROBLEM_ID = "problem_id";
	  public static final String RESULTS_RESULTS = "results";
	  public static final String RESULTS_AVG_ERROR_RATE = "avg_error_rate";
	  public static final String RESULTS_AVG_KAPPA_STATISTIC = "avg_kappa_statistic";
	  public static final String RESULTS_AVG_MEAN_ABSOLUTE_ERROR = "avg_mean_absolute_error";
	  public static final String RESULTS_AVG_MEAN_SQUARED_ERROR = "avg_root_mean_squared_error";
	  public static final String RESULTS_AVG_RELATIVE_ABSOLUTE_ERROR = "avg_relative_absolute_error";
	  public static final String RESULTS_AVG_RELATIVE_SQUARED_ERROR = "avg_root_relative_squared_error";
	  
	  public static final String OPTION = "OPTION";
	  public static final String OPTION_MUTABLE = "mutable";
	  public static final String OPTION_RANGE = "range";
	  public static final String OPTION_SET = "set";
	  public static final String OPTION_IS_A_SET = "is_a_set";
	  public static final String OPTIONS_NUM_ARGS = "number_of_args";
	  public static final String OPTION_DATA_TYPE = "data_type";
	  public static final String OPTION_WEKA_DESTRIPTION = "description";
	  public static final String OPTION_WEKA_NAME = "name";
	  public static final String OPTION_WEKA_SYNOPSIS = "synopsis";
	  public static final String OPTION_VALUE = "value";
	  public static final String OPTION_DEFAULT_VALUE = "default_value";
	  public static final String OPTION_USER_VALUE = "user_value";
	  public static final String OPTION_NUMBER_OF_VALUES_TO_TRY = "number_of_values_to_try";

	  public static final String AGENT = "AGENT";
	  public static final String AGENT_NAME = "name";
	  public static final String AGENT_TYPE = "type";
	  public static final String AGENT_GUI_ID = "gui_id";
	  public static final String AGENT_OPTIONS = "options";
	  	  
	  public static final String INTERVAL = "INTERVAL";
	  public static final String INTERVAL_MIN = "min";
	  public static final String INTERVAL_MAX = "max";
	  
	  public static final String DATA_INSTANCES = "DATA-INSTANCES";
	  public static final String DATA_INSTANCES_ATTRIBUTES = "attributes";
	  public static final String DATA_INSTANCES_INSTANCES = "instances";
	  public static final String DATA_INSTANCES_NAME = "name";
	  public static final String DATA_INSTANCES_CLASS_INDEX = "class_index";

	  public static final String ATTRIBUTE = "ATTRIBUTE";
	  public static final String ATTRIBUTE_NAME = "name";
	  public static final String ATTRIBUTE_TYPE = "type";
	  public static final String ATTRIBUTE_VALUES = "values";
	  public static final String ATTRIBUTE_DATE_FORMAT = "date_format";

	  public static final String INSTANCE = "INSTANCE";
	  public static final String INSTANCE_VALUES = "values";
	  public static final String INSTANCE_MISSING = "missing";
	  
	  public static final String METADATA = "METADATA";
	  public static final String METADATA_INTERNAL_NAME = "internal_name";
	  public static final String METADATA_EXTERNAL_NAME = "external_name";
	  public static final String METADATA_NUMBER_OF_INSTANCES = "number_of_instances";
	  public static final String METADATA_NUMBER_OF_ATTRIBUTES = "number_of_attributes";
	  public static final String METADATA_MISSING_VALUES = "missing_values";
	  public static final String METADATA_DEFAULT_TASK = "default_task";
	  public static final String METADATA_ATTRIBUTE_TYPE = "attribute_type";
	  public static final String METADATA_NUMBER_OF_TASKS_IN_DB = "number_of_tasks_in_db";
		
	  // Predicates
	  public static final String PARTIALRESULTS = "PARTIALRESULTS";
	  public static final String PARTIALRESULTS_TASK = "task";
	  public static final String PARTIALRESULTS_TASK_ID = "task_id";
	  public static final String PARTIALRESULTS_RESULTS = "results";

	  // Actions
	  public static final String COMPUTE = "COMPUTE";
	  public static final String COMPUTE_COMPUTATION = "computation";
	  
	  public static final String EXECUTE = "EXECUTE";
	  public static final String EXECUTE_TASK = "task";

	  public static final String IMPORT_FILE = "IMPORT_FILE";
	  public static final String IMPORT_USER = "userID";
	  public static final String IMPORT_FILENAME = "externalFilename";
	  public static final String IMPORT_FILECONTENT = "fileContent";
	  
	  public static final String TRANSLATE = "TRANSLATE";
	  public static final String TRANSLATE_USER = "userID";
	  public static final String TRANSLATE_EXTERNAL_FILENAME = "externalFilename";
	  public static final String TRANSLATE_INTERNAL_FILENAME = "internalFilename";
	  
	  public static final String SOLVE = "SOLVE";
	  public static final String SOLVE_PROBLEM = "problem";
	  
	  public static final String GET_OPTIONS = "GET-OPTIONS";

	  public static final String SAVE_RESULTS = "SAVE-RESULTS";
	  public static final String SAVE_RESULTS_TASK = "task";
	  
	  public static final String GET_DATA = "GET-DATA";
	  public static final String GET_DATA_FILE_NAME = "file_name";

	  public static final String SAVE_METADATA = "SAVE-METADATA";
	  public static final String SAVE_METADATA_METADATA = "metadata";
	  
	  public static final String GET_ALL_METADATA = "GET-ALL-METADATA";
	  	  
	  public static final String GET_THE_BEST_AGENT = "GET-THE-BEST-AGENT";
	  public static final String GET_THE_BEST_AGENT_NEAREST_FILE_NAME = "nearest_file_name";
	  
	  public static final String GET_FILE_INFO = "GET-FILE-INFO";
	  public static final String GET_FILE_INFO_USERID = "userID";
	  
	  public static final String UPDATE_METADATA = "UPDATE-METADATA";
	  public static final String UPDATE_METADATA_METADATA = "metadata";
	  
	  public static final String GET_FILES = "GET-FILES";
	  public static final String GET_FILES_USERID = "userID";
	  
	  // public static final String SEND_OPTIONS = "SEND-OPTIONS";
	  // public static final String SEND_OPTIONS_OPTIONS = "options";
	  
	  
	  private static Ontology theInstance = new MessagesOntology();
		
	  /**
	     This method grants access to the unique instance of the
	     ontology.
	     @return An <code>Ontology</code> object, containing the concepts
	     of the ontology.
	  */
	   public static Ontology getInstance() {
			return theInstance;
	   }
		
	  /**
	   * Constructor
	   */
	  private MessagesOntology() {
	    //__CLDC_UNSUPPORTED__BEGIN
	  	super(NAME, BasicOntology.getInstance());


	    try {
			add(new ConceptSchema(TASK), Task.class);
			add(new ConceptSchema(DATA), Data.class);
			add(new ConceptSchema(COMPUTATION), Computation.class);
			add(new ConceptSchema(OPTION), Option.class);			
			add(new ConceptSchema(INTERVAL), Interval.class);
			add(new ConceptSchema(AGENT), Agent.class);
			add(new ConceptSchema(PROBLEM), Problem.class);
			add(new ConceptSchema(METHOD), Method.class);
			add(new ConceptSchema(EVALUATION), Evaluation.class);
			add(new ConceptSchema(RESULTS), Results.class);
			add(new ConceptSchema(DATA_INSTANCES), DataInstances.class);
			add(new ConceptSchema(ATTRIBUTE), Attribute.class);
			add(new ConceptSchema(INSTANCE), Instance.class);
			add(new ConceptSchema(METADATA), Metadata.class);
			add(new PredicateSchema(PARTIALRESULTS),PartialResults.class);
			add(new AgentActionSchema(COMPUTE), Compute.class);
			add(new AgentActionSchema(GET_OPTIONS), GetOptions.class);
			add(new AgentActionSchema(EXECUTE), Execute.class);
			add(new AgentActionSchema(SOLVE), Solve.class);
			add(new AgentActionSchema(IMPORT_FILE), ImportFile.class);
			add(new AgentActionSchema(TRANSLATE), TranslateFilename.class);
			add(new AgentActionSchema(SAVE_RESULTS), SaveResults.class);
			add(new AgentActionSchema(SAVE_METADATA), SaveMetadata.class);
			add(new AgentActionSchema(GET_DATA), GetData.class);
			add(new AgentActionSchema(GET_ALL_METADATA), GetAllMetadata.class);
			add(new AgentActionSchema(GET_THE_BEST_AGENT), GetTheBestAgent.class);
			add(new AgentActionSchema(GET_FILE_INFO), GetFileInfo.class);
			add(new AgentActionSchema(UPDATE_METADATA), UpdateMetadata.class);
			add(new AgentActionSchema(GET_FILES), GetFiles.class);
			// add(new AgentActionSchema(SEND_OPTIONS), SendOptions.class);
			
			
	    	ConceptSchema cs = (ConceptSchema)getSchema(COMPUTATION);
	    	cs.add(COMPUTATION_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    	cs.add(COMPUTATION_PROBLEM_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(COMPUTATION_AGENT, (ConceptSchema)getSchema(AGENT));
			cs.add(COMPUTATION_DATA, (ConceptSchema)getSchema(DATA));
			cs.add(COMPUTATION_TIMEOUT, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
			cs.add(COMPUTATION_METHOD, (ConceptSchema)getSchema(METHOD));
			
			cs = (ConceptSchema)getSchema(PROBLEM);
			cs.add(PROBLEM_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(PROBLEM_GUI_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			cs.add(PROBLEM_SENT, (PrimitiveSchema)getSchema(BasicOntology.BOOLEAN));
			cs.add(PROBLEM_AGENTS, (ConceptSchema)getSchema(AGENT), 1, ObjectSchema.UNLIMITED);
	    	cs.add(PROBLEM_DATA, (ConceptSchema)getSchema(DATA), 1, ObjectSchema.UNLIMITED);
			cs.add(PROBLEM_TIMEOUT, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
			cs.add(PROBLEM_METHOD, (ConceptSchema)getSchema(METHOD));

			cs = (ConceptSchema)getSchema(METHOD);
			cs.add(METHOD_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(METHOD_ERROR_RATE, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
			cs.add(METHOD_MAXIMUM_TRIES, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
			
			cs = (ConceptSchema)getSchema(TASK);
			cs.add(TASK_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    	cs.add(TASK_COMPUTATION_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    	cs.add(TASK_PROBLEM_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(TASK_AGENT, (ConceptSchema)getSchema(AGENT));
			cs.add(TASK_DATA,  (ConceptSchema)getSchema(DATA));
	    	cs.add(TASK_RESULT, (ConceptSchema)getSchema(EVALUATION), ObjectSchema.OPTIONAL);

			cs = (ConceptSchema)getSchema(DATA);
			cs.add(DATA_TRAIN_FILE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(DATA_TEST_FILE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(DATA_EXTERNAL_TEST_FILE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(DATA_EXTERNAL_TRAIN_FILE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			cs.add(DATA_METADATA, (ConceptSchema)getSchema(METADATA), ObjectSchema.OPTIONAL);
			cs.add(DATA_OUTPUT, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);			
			cs.add(DATA_MODE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
			
	    	cs = (ConceptSchema)getSchema(INTERVAL);
	    	cs.add(INTERVAL_MIN, (PrimitiveSchema)getSchema(BasicOntology.FLOAT));
	    	cs.add(INTERVAL_MAX, (PrimitiveSchema)getSchema(BasicOntology.FLOAT));	
	    	
	    	cs = (ConceptSchema)getSchema(OPTION);
	    	cs.add(OPTION_MUTABLE, (PrimitiveSchema)getSchema(BasicOntology.BOOLEAN));
	    	cs.add(OPTION_RANGE, (ConceptSchema)getSchema(INTERVAL), ObjectSchema.OPTIONAL);
	    	cs.add(OPTION_SET, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
	    	cs.add(OPTION_IS_A_SET, (PrimitiveSchema)getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);  	
	    	cs.add(OPTIONS_NUM_ARGS, (ConceptSchema)getSchema(INTERVAL), ObjectSchema.OPTIONAL);
	    	cs.add(OPTION_DATA_TYPE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(OPTION_WEKA_DESTRIPTION, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(OPTION_WEKA_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    	cs.add(OPTION_WEKA_SYNOPSIS, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(OPTION_VALUE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(OPTION_DEFAULT_VALUE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(OPTION_USER_VALUE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(OPTION_NUMBER_OF_VALUES_TO_TRY, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
  	
	    	cs = (ConceptSchema)getSchema(EVALUATION);
	    	cs.add(EVALUATION_ERROR_RATE, (PrimitiveSchema)getSchema(BasicOntology.FLOAT));
	    	cs.add(EVALUATION_KAPPA_STATISTIC, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
	    	cs.add(EVALUATION_MEAN_ABSOLUTE_ERROR, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
	    	cs.add(EVALUATION_MEAN_SQUARED_ERROR, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
	    	cs.add(EVALUATION_RELATIVE_ABSOLUTE_ERROR, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
	    	cs.add(EVALUATION_RELATIVE_SQUARED_ERROR, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);   	
	    	cs.add(EVALUATION_DATA_TABLE, (ConceptSchema)getSchema(DATA_INSTANCES), ObjectSchema.OPTIONAL);
    	
	    	cs = (ConceptSchema)getSchema(RESULTS);
	    	cs.add(RESULTS_COMPUTATION_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    	cs.add(RESULTS_PROBLEM_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    	cs.add(RESULTS_AVG_ERROR_RATE, (PrimitiveSchema)getSchema(BasicOntology.FLOAT));
	    	cs.add(RESULTS_AVG_KAPPA_STATISTIC, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
	    	cs.add(RESULTS_AVG_MEAN_ABSOLUTE_ERROR, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
	    	cs.add(RESULTS_AVG_MEAN_SQUARED_ERROR, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
	    	cs.add(RESULTS_AVG_RELATIVE_ABSOLUTE_ERROR, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
	    	cs.add(RESULTS_AVG_RELATIVE_SQUARED_ERROR, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);   	
	    	cs.add(RESULTS_RESULTS, (ConceptSchema)getSchema(TASK), 0, ObjectSchema.UNLIMITED);

	    	cs = (ConceptSchema)getSchema(AGENT);
	    	cs.add(AGENT_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(AGENT_TYPE, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    	cs.add(AGENT_GUI_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(AGENT_OPTIONS, (ConceptSchema)getSchema(OPTION), 0, ObjectSchema.UNLIMITED);

	    	cs = (ConceptSchema)getSchema(DATA_INSTANCES);
	    	cs.add(DATA_INSTANCES_ATTRIBUTES, (ConceptSchema)getSchema(ATTRIBUTE), 0, ObjectSchema.UNLIMITED);
	    	cs.add(DATA_INSTANCES_INSTANCES, (ConceptSchema)getSchema(INSTANCE), 0, ObjectSchema.UNLIMITED);
	    	cs.add(DATA_INSTANCES_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(DATA_INSTANCES_CLASS_INDEX, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));

	    	cs = (ConceptSchema)getSchema(ATTRIBUTE);
	    	cs.add(ATTRIBUTE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(ATTRIBUTE_TYPE, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    	cs.add(ATTRIBUTE_VALUES, (PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
	    	cs.add(ATTRIBUTE_DATE_FORMAT, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

	    	cs = (ConceptSchema)getSchema(INSTANCE);
	    	cs.add(INSTANCE_VALUES, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), 0, ObjectSchema.UNLIMITED);
	    	cs.add(INSTANCE_MISSING, (PrimitiveSchema)getSchema(BasicOntology.BOOLEAN), 0, ObjectSchema.UNLIMITED);
	    	
	    	cs = (ConceptSchema)getSchema(METADATA);
	    	cs.add(METADATA_INTERNAL_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(METADATA_EXTERNAL_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(METADATA_NUMBER_OF_INSTANCES, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	    	cs.add(METADATA_NUMBER_OF_ATTRIBUTES, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	    	cs.add(METADATA_MISSING_VALUES, (PrimitiveSchema)getSchema(BasicOntology.BOOLEAN), ObjectSchema.OPTIONAL);
	    	cs.add(METADATA_DEFAULT_TASK, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(METADATA_ATTRIBUTE_TYPE, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	cs.add(METADATA_NUMBER_OF_TASKS_IN_DB, (PrimitiveSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);

	    	
			PredicateSchema ps = (PredicateSchema)getSchema(PARTIALRESULTS);
	    	ps.add(PARTIALRESULTS_TASK, (ConceptSchema)getSchema(TASK), ObjectSchema.OPTIONAL);
			ps.add(PARTIALRESULTS_TASK_ID, (PrimitiveSchema)getSchema(BasicOntology.STRING));
			ps.add(PARTIALRESULTS_RESULTS, (ConceptSchema)getSchema(EVALUATION), 0, ObjectSchema.UNLIMITED);
			
	    	
	    	AgentActionSchema as = (AgentActionSchema)getSchema(COMPUTE);
			as.add(COMPUTE_COMPUTATION, (ConceptSchema)getSchema(COMPUTATION));

			as = (AgentActionSchema)getSchema(SOLVE);
	    	as.add(SOLVE_PROBLEM, (ConceptSchema)getSchema(PROBLEM));

			as = (AgentActionSchema)getSchema(GET_OPTIONS);
			
			as = (AgentActionSchema)getSchema(EXECUTE);
	    	as.add(EXECUTE_TASK, (ConceptSchema)getSchema(TASK), ObjectSchema.OPTIONAL);

	    	as = (AgentActionSchema)getSchema(IMPORT_FILE);
	    	as.add(IMPORT_USER, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	    	as.add(IMPORT_FILENAME, (PrimitiveSchema)getSchema(BasicOntology.STRING));
	    	as.add(IMPORT_FILECONTENT, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	as.setResult((PrimitiveSchema)getSchema(BasicOntology.STRING)); //the internal filename 
	    	
	    	as = (AgentActionSchema)getSchema(TRANSLATE);
	    	as.add(TRANSLATE_USER, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	    	as.add(TRANSLATE_EXTERNAL_FILENAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	as.add(TRANSLATE_INTERNAL_FILENAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	as.setResult((PrimitiveSchema)getSchema(BasicOntology.STRING)); //the internal filename
	    	
	    	as = (AgentActionSchema)getSchema(SAVE_RESULTS);
	    	as.add(SAVE_RESULTS_TASK, (ConceptSchema)getSchema(TASK));
	    	
	    	as = (AgentActionSchema)getSchema(SAVE_METADATA);	    	
	    	as.add(SAVE_METADATA_METADATA, (ConceptSchema)getSchema(METADATA));
	    	
	    	as = (AgentActionSchema)getSchema(GET_DATA);
	    	as.add(GET_DATA_FILE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

	    	as = (AgentActionSchema)getSchema(GET_ALL_METADATA);
			
	    	as = (AgentActionSchema)getSchema(GET_THE_BEST_AGENT);
	    	as.add(GET_THE_BEST_AGENT_NEAREST_FILE_NAME, (PrimitiveSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
	    	
	    	as = (AgentActionSchema)getSchema(GET_FILE_INFO);
	    	as.add(GET_FILE_INFO_USERID, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	    	as.setResult((ConceptSchema)getSchema(METADATA), 0, ObjectSchema.UNLIMITED);
	    	
	    	as = (AgentActionSchema)getSchema(UPDATE_METADATA);
	    	as.add(UPDATE_METADATA_METADATA, (ConceptSchema)getSchema(Metadata.class));
	    	
	    	as = (AgentActionSchema)getSchema(GET_FILES);
	    	as.add(GET_FILES_USERID, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
	    	as.setResult((PrimitiveSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
	    	
			//as = (AgentActionSchema)getSchema(SEND_OPTIONS);
			//as.add(SEND_OPTIONS_OPTIONS, (ConceptSchema)getSchema(OPTION), 1, ObjectSchema.UNLIMITED);
			
	    }
	    catch(OntologyException oe) {
	      oe.printStackTrace();
	    }
	  }	  

}	  