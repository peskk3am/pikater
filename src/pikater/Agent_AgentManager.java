package pikater;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.sun.org.apache.bcel.internal.classfile.InnerClass;

import pikater.agents.computing.Agent_ComputingAgent;
import pikater.agents.computing.Agent_ComputingAgent.states;
import pikater.ontology.messages.Execute;
import pikater.ontology.messages.GetAllMetadata;
import pikater.ontology.messages.GetFileInfo;
import pikater.ontology.messages.GetFiles;
import pikater.ontology.messages.GetSavedAgents;
import pikater.ontology.messages.GetTheBestAgent;
import pikater.ontology.messages.ImportFile;
import pikater.ontology.messages.LoadAgent;
import pikater.ontology.messages.MessagesOntology;
import pikater.ontology.messages.Metadata;
import pikater.ontology.messages.SaveAgent;
import pikater.ontology.messages.SaveMetadata;
import pikater.ontology.messages.SaveResults;
import pikater.ontology.messages.Task;
import pikater.ontology.messages.TranslateFilename;
import pikater.ontology.messages.UpdateMetadata;
import weka.classifiers.Classifier;
import weka.core.Instances;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.LifeCycle;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.persistence.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.SimpleAchieveREInitiator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import jade.wrapper.StaleProxyException;

public class Agent_AgentManager extends Agent {

	
	Connection db;
	Logger log;
	Codec codec = new SLCodec();
	Ontology ontology = MessagesOntology.getInstance();

	public Agent_AgentManager() {
		super();
		try {
			db = DriverManager.getConnection(
					"jdbc:hsqldb:file:data/db/pikaterdb", "", "");

			Logger.getRootLogger()
					.addAppender(
							new FileAppender(new PatternLayout(
									"%r [%t] %-5p %c - %m%n"), "log"));

			log = Logger.getLogger(Agent_AgentManager.class);
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

		
		MessageTemplate mt = MessageTemplate.and(MessageTemplate
				.MatchOntology(ontology.getName()), MessageTemplate
				.MatchPerformative(ACLMessage.REQUEST));

		addBehaviour(new AchieveREResponder(this, mt) {

			private static final long serialVersionUID = 7L;

			@Override
			protected ACLMessage handleRequest(ACLMessage request)
					throws NotUnderstoodException, RefuseException {

				log.info("Agent " + getLocalName() + " received request: "
						+ request.getContent());

				try {
					Action a = (Action) getContentManager().extractContent(
							request);

					if (a.getAction() instanceof LoadAgent) {
						LoadAgent la = (LoadAgent) a.getAction();
							Action fa = la.getFirst_action();
							
							Agent newAgent = null;
							
							if (la.getObject() != null){
								newAgent = (Agent) toObject(la.getObject());
							}
							else {
														
								// read agent from file 
							    String filename = "saved" + System.getProperty("file.separator") 
							    	+  la.getFilename() + ".model";
							    System.out.println(filename);						  
							        
							    //Construct the ObjectInputStream object
							    ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));
							            
							    newAgent = (Agent) inputStream.readObject();
							} 
						    
						    System.out.print("Ozivenej: "+newAgent);
						    // TODO kdyz se ozivuje 2x ten samej -> chyba
						    
						    
						    if (newAgent != null){
								// get a container controller for creating new agents						    	
						    	
						    	ContainerController container = getContainerController();
						    	AgentController controller = container.acceptNewAgent(la.getFilename(), newAgent);
						    	controller.start();						    	
						    							    	
							}
						    else {
						    	throw new ControllerException("Agent not created.");
						    }
																					
							log.info("Loaded agent:   " + la.getFilename());
														
							ACLMessage reply = null;								
														
							if (fa != null){
								// send message with fa action to the loaded agent
								
								Action ac = new Action();
								ac.setAction(fa);
								ac.setActor(request.getSender());								
								
								ACLMessage first_message = new ACLMessage(ACLMessage.REQUEST);								
								first_message.setLanguage(codec.getName());
								first_message.setOntology(ontology.getName());
								first_message.addReceiver(new AID(la.getFilename(), AID.ISLOCALNAME));
								first_message.clearAllReplyTo();
								first_message.addReplyTo(request.getSender());
								first_message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);								
								first_message.setConversationId(request.getConversationId());
								
								getContentManager().fillContent(first_message, ac);

								send(first_message);
							}
							else{							
								reply = request.createReply();
								reply.setContent("Agent "+newAgent.getLocalName()+" resurected.");
								reply.setPerformative(ACLMessage.INFORM);
							}
														
							return reply;
					}
					
					if (a.getAction() instanceof SaveAgent) {
							// write it into database
							SaveAgent sa = (SaveAgent) a.getAction();

							int userID = sa.getUserID();
							// pikater.ontology.messages.Data data = sa.getData();
							
							pikater.ontology.messages.Agent agent = sa.getAgent();							
							
							String name = agent.getName(); // TODO - zajistit unikatni pro konkretniho uzivatele
							Timestamp currentTimestamp = 
								new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());


							String filename = userID + "_" + name + "_" 
								+ currentTimestamp.toString().replace(":", "-").replace(" ", "_");
							
							
							// save serialized object to file
							byte [] object = sa.getAgent().getObject();
							ObjectOutputStream oos = new ObjectOutputStream(
									new FileOutputStream("saved" + System.getProperty("file.separator") + filename + ".model"));												
							
							Agent newAgent = (Agent) (toObject(object));						    							
														
							oos.writeObject(toObject(object));
							oos.flush();
							oos.close();
							log.info("Agent "+ name +" saved to file" + filename + ".model");
																					
							/*
							String query = "UPDATE results SET (finish, objectFilename) " +
									"VALUES ("								
								+ "\'" + currentTimestamp + "\',"								
								+ "\'" + filename
								+ "\')";  						
												
							Statement stmt = db.createStatement();
							log.info("Executing query: " + query);							

							stmt.executeUpdate(query);
							*/							
							
							ACLMessage reply = request.createReply();
							reply.setContent(filename);
							reply.setPerformative(ACLMessage.INFORM);

							return reply;
							
					} // end of SaveAgent

					/* 
					 	if (a.getAction() instanceof GetSavedAgents){
						GetSavedAgents gsa = (GetSavedAgents) a.getAction();
						
						int userID = gsa.getUserID();
													
						String query = "SELECT * FROM results WHERE userID = " + userID;
						log.info("Executing query " + query);

						Statement stmt = db.createStatement();
						ResultSet rs = stmt.executeQuery(query);
						
						List agents = new ArrayList();			
						
						while( rs.next() ){
							pikater.ontology.messages.Agent agent = new pikater.ontology.messages.Agent();
							agent.setName(rs.getString("name"));
							agent.setSaved_timestamp(rs.getString("finish"));
							agent.setSaved_object_filename(rs.getString("objectFilename"));
							agent.setType(rs.getString("type"));
							agent.setSaved_train_filename(rs.getString("trainFilename"));
							
							agents.add(agent);
						}					
						
						stmt.close();
						
						
						ACLMessage reply = request.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						
						Result r = new Result(a.getAction(), agents); 
						getContentManager().fillContent(reply, r);

						return reply;												
					}	
					*/									
				
				} catch (OntologyException e) {
					e.printStackTrace();
					log.error("Problem extracting content: " + e.getMessage());
				} catch (CodecException e) {
					e.printStackTrace();
					log.error("Codec problem: " + e.getMessage());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				// } catch (FIPAException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				}

				ACLMessage failure = request.createReply();
				failure.setPerformative(ACLMessage.FAILURE);
				log.error("Failure responding to request: "
						+ request.getContent());
				return failure;
			}
		});
	}

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
    
    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException{
    	Object object = null;
    	
    	object = new java.io.ObjectInputStream(new
    			java.io.ByteArrayInputStream(bytes)).readObject(); 
    	
    	return object;
    } 
    

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH.mm.ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
}



