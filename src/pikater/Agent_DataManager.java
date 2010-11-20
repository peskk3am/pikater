package pikater;

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
import java.io.FileWriter;
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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import pikater.ontology.messages.GetAllMetadata;
import pikater.ontology.messages.GetFileInfo;
import pikater.ontology.messages.GetFiles;
import pikater.ontology.messages.GetTheBestAgent;
import pikater.ontology.messages.ImportFile;
import pikater.ontology.messages.MessagesOntology;
import pikater.ontology.messages.Metadata;
import pikater.ontology.messages.SaveMetadata;
import pikater.ontology.messages.SaveResults;
import pikater.ontology.messages.Task;
import pikater.ontology.messages.TranslateFilename;
import pikater.ontology.messages.UpdateMetadata;

public class Agent_DataManager extends Agent {

	private static final long serialVersionUID = 1L;

	Connection db;
	Logger log;
	Codec codec = new SLCodec();
	Ontology ontology = MessagesOntology.getInstance();

	public Agent_DataManager() {
		super();
		try {
			db = DriverManager.getConnection(
					"jdbc:hsqldb:file:data/db/pikaterdb", "", "");

			Logger.getRootLogger()
					.addAppender(
							new FileAppender(new PatternLayout(
									"%r [%t] %-5p %c - %m%n"), "log"));

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

		updateMetadata();

		LinkedList<String> tableNames = new LinkedList<String>();
		LinkedList<String> triggerNames = new LinkedList<String>();
		try {
			String[] types = { "TABLE", "VIEW" };
			ResultSet tables = db.getMetaData().getTables(null, null, "%",
					types);
			while (tables.next()) {
				tableNames.add(tables.getString(3));
			}

			ResultSet triggers = db.createStatement().executeQuery(
					"SELECT trigger_name FROM INFORMATION_SCHEMA.TRIGGERS");
			while (triggers.next()) {
				triggerNames.add(triggers.getString("trigger_name"));
			}

		} catch (SQLException e) {
			log.error("Error getting tables list: " + e.getMessage());
			e.printStackTrace();
		}

		log.info("Found the following tables: ");
		for (String s : tableNames) {
			log.info(s);
		}

		log.info("Found the following triggers: ");
		for (String s : triggerNames) {
			log.info(s);
		}

		File data = new File("data" + System.getProperty("file.separator")
				+ "files");
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
				db
						.createStatement()
						.executeUpdate(
								"CREATE TABLE fileMapping (userID INTEGER NOT NULL, externalFilename VARCHAR(256) NOT NULL, internalFilename CHAR(32) NOT NULL, PRIMARY KEY (userID, externalFilename))");
			}
		} catch (SQLException e) {
			log.fatal("Error creating table FILEMAPPING: " + e.getMessage());
			e.printStackTrace();
		}

		try {
			if (!tableNames.contains("METADATA")) {
				log.info("Creating table METADATA");
				db.createStatement().executeUpdate(
						"CREATE TABLE metadata ("
								+ "externalFilename VARCHAR(256) NOT NULL, "
								+ "internalFilename CHAR(32) NOT NULL, "
								+ "defaultTask VARCHAR(256), "
								+ "attributeType VARCHAR(256), "
								+ "numberOfInstances INTEGER, "
								+ "numberOfAttributes INTEGER, "
								+ "missingValues BOOLEAN, "
								+ "PRIMARY KEY (internalFilename))");
			}
		} catch (SQLException e) {
			log.fatal("Error creating table METADATA: " + e.getMessage());
			e.printStackTrace();
		}

		try {
			if (!tableNames.contains("RESULTS")) {
				log.info("Creating table RESULTS");
				db.createStatement().executeUpdate(
						"CREATE TABLE results (" + "agentName VARCHAR (256), "
								+ "agentType VARCHAR (256), "
								+ "options VARCHAR (256), "
								+ "dataFile VARCHAR (50), "
								+ "testFile VARCHAR (50), "
								+ "errorRate DOUBLE, "
								+ "kappaStatistic DOUBLE, "
								+ "meanAbsoluteError DOUBLE, "
								+ "rootMeanSquaredError DOUBLE, "
								+ "relativeAbsoluteError DOUBLE, "
								+ "rootRelativeSquaredError DOUBLE)");
			}
		} catch (SQLException e) {
			log.fatal("Error creating table RESULTS: " + e.getMessage());
		}

		try {
			if (!tableNames.contains("FILEMETADATA")) {
				log.info("Creating view FILEMETADATA");
				db
						.createStatement()
						.executeUpdate(
								"CREATE VIEW filemetadata AS "
										+ "SELECT userid, filemapping.internalfilename, filemapping.externalfilename, "
										+ "defaulttask, attributetype, numberofattributes, numberofinstances, missingvalues "
										+ "FROM filemapping JOIN metadata "
										+ "ON filemapping.internalfilename = metadata.internalfilename");
			}
		} catch (SQLException e) {
			log.fatal("Error creating table FILEMETADATA: " + e.getMessage());
		}

		try {
			if (!triggerNames.contains("PREPAREMETADATA")) {
				db
						.createStatement()
						.execute(
								"CREATE TRIGGER prepareMetadata AFTER INSERT ON filemapping "
										+ "REFERENCING NEW ROW AS newrow FOR EACH ROW "
										+ "INSERT INTO metadata (internalfilename, externalfilename) "
										+ "VALUES (newrow.internalfilename, newrow.externalfilename)");
			}
		} catch (SQLException e) {
			log.fatal("Error creating trigger prepareMetadata: "
					+ e.getMessage());
		}

		MessageTemplate mt = MessageTemplate.and(MessageTemplate
				.MatchOntology(ontology.getName()), MessageTemplate
				.MatchPerformative(ACLMessage.REQUEST));

		addBehaviour(new AchieveREResponder(this, mt) {

			private static final long serialVersionUID = 1L;

			@Override
			protected ACLMessage handleRequest(ACLMessage request)
					throws NotUnderstoodException, RefuseException {

				log.info("Agent " + getLocalName() + " received request: "
						+ request.getContent());

				try {
					Action a = (Action) getContentManager().extractContent(
							request);

					if (a.getAction() instanceof ImportFile) {
						ImportFile im = (ImportFile) a.getAction();

						if (im.getFileContent() == null) {
							String path = System.getProperty("user.dir")
									+ System.getProperty("file.separator");
							path += "incoming"
									+ System.getProperty("file.separator")
									+ im.getExternalFilename();

							String internalFilename = md5(path);

							File f = new File(path);

							Statement stmt = db.createStatement();
							String query = "SELECT COUNT(*) AS num FROM fileMapping WHERE internalFilename = \'"
									+ internalFilename + "\'";
							log.info("Executing query " + query);

							ResultSet rs = stmt.executeQuery(query);

							rs.next();
							int count = rs.getInt("num");

							stmt.close();

							if (count > 0) {
								f.delete();
								log.info("File " + internalFilename
										+ " already present in the database");
							} else {

								stmt = db.createStatement();

								log.info("Executing query: " + query);
								query = "INSERT into fileMapping (userId, externalFilename, internalFilename) VALUES ("
										+ im.getUserID()
										+ ",\'"
										+ im.getExternalFilename()
										+ "\',\'"
										+ internalFilename + "\')";

								stmt.executeUpdate(query);
								stmt.close();

								// insert the same file into the metadata table,
								// other values will be filled in when the file
								// is read by a reader agent
								// stmt = db.createStatement();

								// not needed anymore, there is now a trigger
								// which does the same
								/*
								 * query =
								 * "INSERT INTO metadata (externalFilename, internalFilename"
								 * + // ", defaultTask, attributeType,
								 * missingValues ") VALUES (\'" +
								 * im.getExternalFilename() + "\', \'" +
								 * internalFilename + "\')";
								 * 
								 * log.info("Executing query: " + query);
								 * stmt.execute(query);
								 */

								// move the file to db\files directory
								String newName = System.getProperty("user.dir")
										+ System.getProperty("file.separator")
										+ "data"
										+ System.getProperty("file.separator")
										+ "files"
										+ System.getProperty("file.separator")
										+ internalFilename;
								// Boolean res = f.renameTo(new File(newName));
								move(f, new File(newName));

								// move(f, new File(newName));

							}

							ACLMessage reply = request.createReply();
							reply.setPerformative(ACLMessage.INFORM);

							Result r = new Result(im, internalFilename);
							getContentManager().fillContent(reply, r);

							return reply;
						} else {

							String fileContent = im.getFileContent();
							String fileName = im.getExternalFilename();
							String internalFilename = DigestUtils
									.md5Hex(fileContent);

							Statement stmt = db.createStatement();
							String query = "SELECT COUNT(*) AS num FROM fileMapping WHERE internalFilename = \'"
									+ internalFilename + "\'";
							log.info("Executing query " + query);

							ResultSet rs = stmt.executeQuery(query);

							rs.next();
							int count = rs.getInt("num");

							stmt.close();

							if (count > 0) {
								log.info("File " + internalFilename
										+ " already present in the database");
							} else {

								stmt = db.createStatement();

								log.info("Executing query: " + query);
								query = "INSERT into fileMapping (userId, externalFilename, internalFilename) VALUES ("
										+ im.getUserID()
										+ ",\'"
										+ im.getExternalFilename()
										+ "\',\'"
										+ internalFilename + "\')";

								stmt.executeUpdate(query);
								stmt.close();

								String newName = System.getProperty("user.dir")
										+ System.getProperty("file.separator")
										+ "data"
										+ System.getProperty("file.separator")
										+ "files"
										+ System.getProperty("file.separator")
										+ internalFilename;

								FileWriter file = new FileWriter(newName);
								file.write(fileContent);
								file.close();

								log.info("Created file: " + newName);
							}

							ACLMessage reply = request.createReply();
							reply.setPerformative(ACLMessage.INFORM);

							Result r = new Result(im, internalFilename);
							getContentManager().fillContent(reply, r);

							return reply;
						}

					}
					if (a.getAction() instanceof TranslateFilename) {

						TranslateFilename tf = (TranslateFilename) a
								.getAction();

						Statement stmt = db.createStatement();

						String query = null;
						if (tf.getInternalFilename() == null) {
							query = "SELECT internalFilename AS filename FROM fileMapping WHERE userID="
									+ tf.getUserID()
									+ " AND externalFilename=\'"
									+ tf.getExternalFilename() + "\'";
						} else {
							query = "SELECT externalFilename AS filename FROM fileMapping WHERE userID="
									+ tf.getUserID()
									+ " AND internalFilename=\'"
									+ tf.getInternalFilename() + "\'";
						}

						log.info("Executing query: " + query);

						ResultSet rs = stmt.executeQuery(query);

						if (rs.next()) { // should return single line (or none,
											// if file does not exist)
							String internalFilename = rs.getString("filename");

							ACLMessage reply = request.createReply();
							reply.setPerformative(ACLMessage.INFORM);

							Result r = new Result(tf, internalFilename);
							getContentManager().fillContent(reply, r);

							return reply;
						}

					}
					if (a.getAction() instanceof SaveResults) {

						SaveResults sr = (SaveResults) a.getAction();
						Task res = sr.getTask();

						Statement stmt = db.createStatement();

						String query = "INSERT INTO results (agentName, agentType, options, dataFile, testFile,"
								+ "errorRate, kappaStatistic, meanAbsoluteError, rootMeanSquaredError, relativeAbsoluteError,"
								+ "rootRelativeSquaredError) VALUES (";

						query += "\'" + res.getAgent().getName() + "\',";
						query += "\'" + res.getAgent().getType() + "\',";
						query += "\'" + res.getAgent().optionsToString()
								+ "\',";
						query += "\'"
								+ (res.getData().getTrain_file_name()
										.split(Pattern.quote(System
												.getProperty("file.separator"))))[2]
								+ "\',";
						query += "\'"
								+ (res.getData().getTest_file_name()
										.split(Pattern.quote(System
												.getProperty("file.separator"))))[2]
								+ "\',";

						query += res.getResult().getError_rate() + ",";
						query += res.getResult().getKappa_statistic() + ",";
						query += res.getResult().getMean_absolute_error() + ",";
						query += res.getResult().getRoot_mean_squared_error()
								+ ",";
						query += res.getResult().getRelative_absolute_error()
								+ ",";
						query += res.getResult()
								.getRoot_relative_squared_error()
								+ ")";

						log.info("Executing query: " + query);

						stmt.executeUpdate(query);

						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						return reply;
					}
					if (a.getAction() instanceof SaveMetadata) {

						SaveMetadata saveMetadata = (SaveMetadata) a
								.getAction();
						Metadata metadata = saveMetadata.getMetadata();

						Statement stmt = db.createStatement();

						String query = "UPDATE metadata SET ";
						query += "numberOfInstances="
								+ metadata.getNumber_of_instances() + ", ";
						query += "numberOfAttributes="
								+ metadata.getNumber_of_attributes() + ", ";
						query += "missingValues="
								+ metadata.getMissing_values();
						if (metadata.getAttribute_type() != null) {
							query += ", attributeType=\'"
									+ metadata.getAttribute_type() + "\' ";
						}
						if (metadata.getDefault_task() != null) {
							query += ", defaultTask=\'"
									+ metadata.getDefault_task() + "\' ";
						}

						// the external file name contains part o the path
						// (db/files/name) -> split and use only the [2] part
						query += " WHERE internalFilename=\'"
								+ metadata
										.getInternal_name()
										.split(
												Pattern
														.quote(System
																.getProperty("file.separator")))[2]
								+ "\'";

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
						while (rs.next()) {
							Metadata m = new Metadata();
							m.setAttribute_type(rs.getString("attributeType"));
							m.setDefault_task(rs.getString("defaultTask"));
							m
									.setExternal_name(rs
											.getString("externalFilename"));
							m
									.setInternal_name(rs
											.getString("internalFilename"));
							m.setMissing_values(rs.getBoolean("missingValues"));
							m.setNumber_of_attributes(rs
									.getInt("numberOfAttributes"));
							m.setNumber_of_instances(rs
									.getInt("numberOfInstances"));

							// get the number of task with this file as training
							// set in the db
							query = "SELECT COUNT(*) AS n FROM results WHERE dataFile=\'"
									+ rs.getString("internalFilename") + "\'";
							System.out.println(query);
							ResultSet rs_number = stmt.executeQuery(query);
							rs_number.next();

							m.setNumber_of_tasks_in_db(rs_number.getInt("n"));
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
						GetTheBestAgent g = (GetTheBestAgent) a.getAction();
						String name = g.getNearest_file_name();

						Statement stmt = db.createStatement();

						String query = "SELECT * FROM results "
								+ "WHERE dataFile =\'"
								+ name
								+ "\'"
								+ "AND errorRate = (SELECT MIN(errorRate) FROM results "
								+ "WHERE dataFile =\'" + name + "\')";

						ResultSet rs = stmt.executeQuery(query);
						if (rs == null) {
							ACLMessage reply = request.createReply();
							reply.setPerformative(ACLMessage.FAILURE);
							reply
									.setContent("There are no results for this file in the database.");

							return reply;
						}
						rs.next();

						pikater.ontology.messages.Agent agent = new pikater.ontology.messages.Agent();
						agent.setName(rs.getString("agentName"));
						agent.setType(rs.getString("agentType"));
						System.out.println("**** options: "
								+ rs.getString("options"));
						agent.setOptions(agent.stringToOptions(rs
								.getString("options")));

						log.info("Executing query: " + query);

						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);

						Result _result = new Result(a.getAction(), agent);
						getContentManager().fillContent(reply, _result);

						return reply;
					}
					if (a.getAction() instanceof GetFileInfo) {

						GetFileInfo gfi = (GetFileInfo) a.getAction();

						String query = "SELECT * FROM filemetadata WHERE userid = "
								+ gfi.getUserID();

						Statement stmt = db.createStatement();

						log.info("Executing query: " + query);

						ResultSet rs = stmt.executeQuery(query);

						List fileInfos = new ArrayList();

						while (rs.next()) {
							Metadata m = new Metadata();
							m.setAttribute_type(rs.getString("attributeType"));
							m.setDefault_task(rs.getString("defaultTask"));
							m
									.setExternal_name(rs
											.getString("externalFilename"));
							m
									.setInternal_name(rs
											.getString("internalFilename"));
							m.setMissing_values(rs.getBoolean("missingValues"));
							m.setNumber_of_attributes(rs
									.getInt("numberOfAttributes"));
							m.setNumber_of_instances(rs
									.getInt("numberOfInstances"));
							fileInfos.add(m);
						}

						Result r = new Result(a.getAction(), fileInfos);
						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);

						getContentManager().fillContent(reply, r);

						return reply;
					}

					if (a.getAction() instanceof UpdateMetadata) {

						UpdateMetadata updateMetadata = (UpdateMetadata) a
								.getAction();
						Metadata metadata = updateMetadata.getMetadata();

						Statement stmt = db.createStatement();

						String query = "UPDATE metadata SET ";
						query += "numberOfInstances="
								+ metadata.getNumber_of_instances() + ", ";
						query += "numberOfAttributes="
								+ metadata.getNumber_of_attributes() + ", ";
						query += "missingValues="
								+ metadata.getMissing_values() + "";
						if (metadata.getAttribute_type() != null) {
							query += ", attributeType=\'"
									+ metadata.getAttribute_type() + "\' ";
						}
						if (metadata.getDefault_task() != null) {
							query += ", defaultTask=\'"
									+ metadata.getDefault_task() + "\' ";
						}
						query += " WHERE internalFilename =\'"
								+ metadata.getInternal_name() + "\'";

						log.info("Executing query: " + query);

						stmt.executeUpdate(query);

						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						return reply;
					}
					if (a.getAction() instanceof GetFiles) {

						GetFiles gf = (GetFiles) a.getAction();

						String query = "SELECT * FROM filemapping WHERE userid = "
								+ gf.getUserID();

						log.info("Executing query: " + query);

						Statement stmt = db.createStatement();
						ResultSet rs = stmt.executeQuery(query);

						ArrayList files = new ArrayList();

						while (rs.next()) {
							files.add(rs.getString("externalFilename"));
						}

						Result r = new Result(a.getAction(), files);
						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);

						getContentManager().fillContent(reply, r);

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
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				ACLMessage failure = request.createReply();
				failure.setPerformative(ACLMessage.FAILURE);
				log.error("Failure responding to request: "
						+ request.getContent());
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
				sb.append((char) ch);
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

	public void updateMetadata() {
		try {
			Statement stmt = db.createStatement();
			String query = "UPDATE metadata SET defaultTask='Classification', attributeType='Categorical', missingValues='False' WHERE externalFilename='car.arff';"
					+ " UPDATE metadata SET defaultTask='Regression', attributeType='Multivariate', missingValues='False' WHERE externalFilename='machine.arff';"
					+ " UPDATE metadata SET defaultTask='Regression', attributeType='Real', missingValues='True' WHERE externalFilename='communities.arff';"
					+ " UPDATE metadata SET defaultTask='Classification', attributeType='Integer', missingValues='True' WHERE externalFilename='lung-cancer.arff';"
					+ " UPDATE metadata SET defaultTask='Classification', attributeType='Integer', missingValues='False' WHERE externalFilename='haberman.arff';"
					+ " UPDATE metadata SET defaultTask='Classification', attributeType='Categorical', missingValues='False' WHERE externalFilename='tic-tac-toe.arff';"
					+ " UPDATE metadata SET defaultTask='Classification', attributeType='Integer', missingValues='False' WHERE externalFilename='letter-recognition.arff';"
					+ " UPDATE metadata SET defaultTask='Classification', attributeType='Real', missingValues='False' WHERE externalFilename='magic.arff';"
					+ " UPDATE metadata SET defaultTask='Classification', attributeType='Real', missingValues='False' WHERE externalFilename='iris.arff';"
					+ " UPDATE metadata SET defaultTask='Classification', attributeType='Multivariate', missingValues='False' WHERE externalFilename='weather.arff';";

			stmt.executeUpdate(query);
			log.info("Executing query: " + query);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Move file (src) to File/directory dest.
	public static synchronized void move(File src, File dest)
			throws FileNotFoundException, IOException {
		copy(src, dest);
		// src.delete();
	}

	// Copy file (src) to File/directory dest.
	public static synchronized void copy(File src, File dest)
			throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

}
