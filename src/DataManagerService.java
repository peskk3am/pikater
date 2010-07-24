import java.security.MessageDigest;

import ontology.messages.ImportFile;
import ontology.messages.MessagesOntology;
import ontology.messages.Metadata;
import ontology.messages.SaveMetadata;
import ontology.messages.SaveResults;
import ontology.messages.Task;
import ontology.messages.TranslateFilename;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.lang.acl.ACLMessage;


public class DataManagerService extends FIPAService {

	static final Codec codec = new SLCodec();  
	
	public static String importFile(Agent agent, int userID, String path) {
		
		ImportFile im = new ImportFile();
		im.setUserID(userID);
		im.setExternalFilename(path);
		
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(new AID("dataManager", false));
		request.setOntology(MessagesOntology.getInstance().getName());
		request.setLanguage(codec.getName());
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		Action a = new Action();
		a.setActor(agent.getAID());
		a.setAction(im);
		
		try {
			agent.getContentManager().fillContent(request, a);
		} catch (CodecException e1) {
			e1.printStackTrace();
		} catch (OntologyException e1) {
			e1.printStackTrace();
		}
		
		try {
			return FIPAService.doFipaRequestClient(agent, request).getContent();
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String translateFilename(Agent agent, int user, String externalFilename) {
		
		TranslateFilename tf = new TranslateFilename();
		tf.setUserID(user);
		tf.setExternalFilename(externalFilename);
		
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(new AID("dataManager", false));
		request.setOntology(MessagesOntology.getInstance().getName());
		request.setLanguage(codec.getName());
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		Action a = new Action();
		a.setActor(agent.getAID());
		a.setAction(tf);
		
		try {
			agent.getContentManager().fillContent(request, a);
			
			ACLMessage inform = FIPAService.doFipaRequestClient(agent, request); 
			
			if (inform == null) {
				return null;
			}
			
			Result r = (Result)agent.getContentManager().extractContent(inform);
			
			return (String)r.getValue();
		} catch (CodecException e) {
			e.printStackTrace();
		} catch (OntologyException e) {
			e.printStackTrace();
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void saveResult (Agent agent, Task t) {
		
		SaveResults sr = new SaveResults();
		sr.setTask(t);
		
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(new AID("dataManager", false));
		request.setOntology(MessagesOntology.getInstance().getName());
		request.setLanguage(codec.getName());
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		Action a = new Action();
		a.setActor(agent.getAID());
		a.setAction(sr);
		
		try {
			agent.getContentManager().fillContent(request, a);
			
			FIPAService.doFipaRequestClient(agent, request); 
			
		} catch (CodecException e) {
			e.printStackTrace();
		} catch (OntologyException e) {
			e.printStackTrace();
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
	}

	public static void saveMetadata (Agent agent, Metadata m, String filename) {
		SaveMetadata saveMetadata = new SaveMetadata();
		saveMetadata.setMetadata(m);
		saveMetadata.setFile_name(filename);				
		
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(new AID("dataManager", false));
		request.setOntology(MessagesOntology.getInstance().getName());
		request.setLanguage(codec.getName());
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		
		Action a = new Action();
		a.setActor(agent.getAID());
		a.setAction(saveMetadata);
		
		try {
			agent.getContentManager().fillContent(request, a);
			
			FIPAService.doFipaRequestClient(agent, request); 
			
		} catch (CodecException e) {
			e.printStackTrace();
		} catch (OntologyException e) {
			e.printStackTrace();
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
	}
	
}
