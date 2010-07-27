import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.regex.Pattern;

import ontology.messages.GetAllMetadata;
import ontology.messages.GetTheBestAgent;
import ontology.messages.ImportFile;
import ontology.messages.MessagesOntology;
import ontology.messages.Metadata;
import ontology.messages.SaveMetadata;
import ontology.messages.SaveResults;
import ontology.messages.Task;
import ontology.messages.TranslateFilename;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Agent_DataManager extends Agent {
	
	private static final long serialVersionUID = 1L;
	
	Connection db;
	Logger log;
	Codec codec = new SLCodec();
	Ontology ontology = MessagesOntology.getInstance();
	
	public Agent_DataManager() {
		super();
		try {
			db = DriverManager.getConnection("jdbc:hsqldb:file:data/db/pikaterdb", "", "");
			
			Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout("%r [%t] %-5p %c - %m%n"), "log"));
			
			log = Logger.getLogger(Agent_DataManager.class);
			log.setLevel(Level.TRACE);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	@Override
	protected void setup() {
		super.setup();
				
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		LinkedList<String> tableNames = new LinkedList<String>();
		try {
			String[] types = {"TABLE"};
			ResultSet tables = db.getMetaData().getTables(null, null, "%", types);
			while (tables.next()) {
				tableNames.add(tables.getString(3));
			}
		} catch (SQLException e) {
			log.error("Error getting tables list: " + e.getMessage());
			e.printStackTrace();
		}
		
		log.info("Found the following tables: ");
		for (String s : tableNames) {
			log.info(s);
		}
		
		File data = new File("data"+System.getProperty("file.separator")+"files");
		if (!data.exists()) {
			log.info("Creating directory data/files");
			if (data.mkdirs()) {
				log.info("Succesfully created directory data/files");
			} else {
				log.error("Error creating directory data/files");
			}
		}
		
		try {
			if (!tableNames.contains("FILEMAPPING")) {
				log.info("Creating table FILEMAPPING");
				db.createStatement().executeUpdate("CREATE TABLE fileMapping (userID INTEGER NOT NULL, externalFilename VARCHAR(256) NOT NULL, internalFilename CHAR(32) NOT NULL, PRIMARY KEY (userID, externalFilename))");
			}
		} catch (SQLException e) {
			log.fatal("Error creating table FILEMAPPING: " + e.getMessage());
			e.printStackTrace();
		}
		
		try {
			if (!tableNames.contains("METADATA")) {
				log.info("Creating table METADATA");
				db.createStatement().executeUpdate("CREATE TABLE metadata (" +
						"externalFilename VARCHAR(256) NOT NULL, " +
						"internalFilename CHAR(32) NOT NULL, " +
						"defaultTask VARCHAR(256), " +
						"attributeType VARCHAR(256), " +
						"numberOfInstances INTEGER, " +
						"numberOfAttributes INTEGER, " +
						"missingValues BOOLEAN, " +
						"PRIMARY KEY (internalFilename))");
			}
		} catch (SQLException e) {
			log.fatal("Error creating table METADATA: " + e.getMessage());
			e.printStackTrace();
		}
		
		try {
			if (!tableNames.contains("RESULTS")) {
				log.info("Creating table RESULTS");
				db.createStatement().executeUpdate("CREATE TABLE results (" +
						"agentName VARCHAR (256), " +
						"agentType VARCHAR (256), " +
						"options VARCHAR (256), " +
						"dataFile VARCHAR (50), " +
						"testFile VARCHAR (50), " +
						"errorRate DOUBLE, " +
						"kappaStatistic DOUBLE, " +
						"meanAbsoluteError DOUBLE, " +
						"rootMeanSquaredError DOUBLE, " +
						"relativeAbsoluteError DOUBLE, " +
						"rootRelativeSquaredError DOUBLE)" );
			}
		} catch (SQLException e) {
			log.fatal("Error creating table RESULTS: " + e.getMessage());
		}
		
		MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchOntology(ontology.getName()),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		
		addBehaviour(new AchieveREResponder(this, mt) {

			private static final long serialVersionUID = 1L;
			
			@Override
			protected ACLMessage handleRequest(ACLMessage request)
					throws NotUnderstoodException, RefuseException {
				
				log.info("Agent " + getLocalName() + " received request: " + request.getContent());
				
				try {
					Action a = (Action)getContentManager().extractContent(request);
					
					if (a.getAction() instanceof ImportFile) {
						ImportFile im = (ImportFile)a.getAction();
						
						String path = System.getProperty("user.dir") + System.getProperty("file.separator");
						path += "incoming" + System.getProperty("file.separator") + im.getExternalFilename();

						String internalFilename = md5(path);
						
						File f = new File(path);		
						
						Statement stmt = db.createStatement();
						String query = "SELECT COUNT(*) AS num FROM fileMapping WHERE internalFilename = \'" + internalFilename + "\'";
						log.info("Executing query " + query);
						
						ResultSet rs = stmt.executeQuery(query);
						
						rs.next();
						int count = rs.getInt("num");
			
						stmt.close();
						
																		
						if (count > 0) {
							f.delete();
							log.info("File " +  internalFilename + " already present in the database");
						} 
						else {
							
							
							stmt = db.createStatement();
							
							log.info("Executing query: " + query);
							query = "INSERT into fileMapping (userId, externalFilename, internalFilename) VALUES (" + im.getUserID() + ",\'" + im.getExternalFilename() + "\',\'" + internalFilename + "\')";
							
							stmt.executeUpdate(query);
							stmt.close();

							
							// insert the same file into the metadata table, 
							// other values will be filled in when the file is read by a reader agent
							stmt = db.createStatement();
							
							query = "INSERT INTO metadata (externalFilename, internalFilename" +
									// ", defaultTask, attributeType, missingValues
									") VALUES (\'" + im.getExternalFilename() + "\', \'" + internalFilename + "\')";
													 													
							log.info("Executing query: " + query);						
							stmt.executeQuery(query);
							
							
							// move the file to db\files directory
							String newName = System.getProperty("user.dir")+System.getProperty("file.separator")+"data"+System.getProperty("file.separator")+"files"+System.getProperty("file.separator")+internalFilename;
							// Boolean res = f.renameTo(new File(newName));
							f.renameTo(new File(newName));						
							// move(f, new File(newName));								
							
						}
						
						
						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						
						Result r = new Result(im, internalFilename);
						getContentManager().fillContent(reply, r);
						
						return reply;
					}
					if (a.getAction() instanceof TranslateFilename) {
						
						TranslateFilename tf = (TranslateFilename)a.getAction();
						
						Statement stmt = db.createStatement();
						
						String query = "SELECT internalFilename FROM fileMapping WHERE userID=" + tf.getUserID() + " AND externalFilename=\'" + tf.getExternalFilename() + "\'";
						
						log.info("Executing query: " + query);
						
						ResultSet rs = stmt.executeQuery(query);
						
						if (rs.next()) { //should return single line (or none, if file does not exist)
							String internalFilename = rs.getString("internalFilename");
							
							ACLMessage reply = request.createReply();
							reply.setPerformative(ACLMessage.INFORM);
							
							Result r = new Result(tf, internalFilename);
							getContentManager().fillContent(reply, r);
							
							return reply;
						}	
						
					}
					if (a.getAction() instanceof SaveResults) {
						
						SaveResults sr = (SaveResults)a.getAction();
						Task res = sr.getTask();
						
						Statement stmt = db.createStatement();
						
						String query = "INSERT INTO results (agentName, agentType, options, dataFile, testFile," +
								"errorRate, kappaStatistic, meanAbsoluteError, rootMeanSquaredError, relativeAbsoluteError," +
								"rootRelativeSquaredError) VALUES (";
						
						query += "\'" + res.getAgent().getName() + "\',";
						query += "\'" + res.getAgent().getType() + "\',";
						query += "\'" + res.getAgent().optionsToString() + "\',";						
						query += "\'" + (res.getData().getTrain_file_name().split(Pattern.quote(System.getProperty("file.separator"))))[2] + "\',";
						query += "\'" + (res.getData().getTest_file_name().split(Pattern.quote(System.getProperty("file.separator"))))[2] + "\',";
						
						query += res.getResult().getError_rate() + ",";
						query += res.getResult().getKappa_statistic() + ",";
						query += res.getResult().getMean_absolute_error() + ",";
						query += res.getResult().getRoot_mean_squared_error() + ",";
						query += res.getResult().getRelative_absolute_error() + ",";
						query += res.getResult().getRoot_relative_squared_error() + ")";						
						
						log.info("Executing query: " + query);
					
						stmt.executeUpdate(query);
						
						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						return reply;
					}
					if (a.getAction() instanceof SaveMetadata) {
						
						SaveMetadata saveMetadata = (SaveMetadata)a.getAction();
						Metadata metadata = saveMetadata.getMetadata();
						
						Statement stmt = db.createStatement();

						String query = "UPDATE metadata SET "; 												 						
						query += "numberOfInstances=" + metadata.getNumber_of_instances()+ ", ";
						query += "numberOfAttributes=" + metadata.getNumber_of_attributes();
						// the external file name contains part o the path (db/files/name) -> split and use only the [2] part
						query += " WHERE internalFilename =\'"+metadata.getInternal_name().split(Pattern.quote(System.getProperty("file.separator")))[2]+"\'"; 						
						
						log.info("Executing query: " + query);
					
						stmt.executeUpdate(query);
						
						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						return reply;
					}
					if (a.getAction() instanceof GetAllMetadata) {
						Statement stmt = db.createStatement();

						String query = "SELECT * FROM metadata";	
						
						List allMetadata = new ArrayList();
						
						ResultSet rs = stmt.executeQuery(query);
						while(rs.next()){
							Metadata m = new Metadata();
							m.setAttribute_type(rs.getString("attributeType"));
							m.setDefault_task(rs.getString("defaultTask"));
							m.setExternal_name(rs.getString("externalFilename"));
							m.setInternal_name(rs.getString("internalFilename"));
							m.setMissing_values(rs.getBoolean("missingValues"));
							m.setNumber_of_attributes(rs.getInt("numberOfAttributes"));
							m.setNumber_of_instances(rs.getInt("numberOfInstances"));
							allMetadata.add(m);
						}
						
						log.info("Executing query: " + query);
																			
						
						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						
						Result _result = new Result(a.getAction(), allMetadata);
						getContentManager().fillContent(reply, _result);
						
						return reply;						
					}
					
					if (a.getAction() instanceof GetTheBestAgent) {
						GetTheBestAgent g = (GetTheBestAgent)a.getAction();
						String name = g.getNearest_file_name();

						Statement stmt = db.createStatement();

						String query = "SELECT * FROM results " +
								"WHERE dataFile =\'"+name+"\'" +
										"AND errorRate = (SELECT MIN(errorRate) FROM results " +
															"WHERE dataFile =\'"+name+"\')";									
						
						ResultSet rs = stmt.executeQuery(query);
						rs.next();
						
						ontology.messages.Agent agent = new ontology.messages.Agent(); 
						agent.setName(rs.getString("agentName"));
						agent.setType(rs.getString("agentType"));
						System.out.println("**** options: "+rs.getString("options"));
						agent.setOptions(agent.stringToOptions(rs.getString("options")));
						
						log.info("Executing query: " + query);																			
						
						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						
						Result _result = new Result(a.getAction(), agent);
						getContentManager().fillContent(reply, _result);
						
						return reply;						
					}
				} catch (OntologyException e) {
					e.printStackTrace();
					log.error("Problem extracting content: " + e.getMessage());
				} catch (CodecException e) {
					e.printStackTrace();
					log.error("Codec problem: " + e.getMessage());
				} catch (SQLException e) {
					e.printStackTrace();
					log.error("SQL error: " + e.getMessage());			
				}
				
				
				ACLMessage failure = request.createReply();
				failure.setPerformative(ACLMessage.FAILURE);
				log.error("Failure responding to request: " + request.getContent());
				return failure;
			}
			
		});
		
	} 
	
	private String md5(String path) {
	
		StringBuffer sb = null;
		
		try {
			FileInputStream fs = new FileInputStream(path);
			sb = new StringBuffer();
			
			int ch;
			while ((ch = fs.read()) != -1) {
				sb.append((char)ch);
			}
			fs.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error("File not found: " + path + " -- " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error reading file: " + path + " -- " + e.getMessage());
		}
		
		String md5 = DigestUtils.md5Hex(sb.toString()); 
		
		log.info("MD5 hash of file " + path + " is " + md5);
		
		return md5;
	}
		
}
