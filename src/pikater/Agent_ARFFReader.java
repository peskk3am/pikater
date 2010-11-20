package pikater;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import pikater.ontology.messages.DataInstances;
import pikater.ontology.messages.GetData;
import pikater.ontology.messages.Instance;
import pikater.ontology.messages.MessagesOntology;
import pikater.ontology.messages.Metadata;
import weka.core.Instances;

public class Agent_ARFFReader extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7116837600070711675L;
	private Codec codec = new SLCodec();
	private Ontology ontology = MessagesOntology.getInstance();
	// File name
	private String fileName;
	// data read from file
	protected Instances data;
	// path to the file
	// private String path = "D:/diplomka/eclipse/diplomka/";
	private String path = System.getProperty("user.dir")
			+ System.getProperty("file.separator");
	private boolean working = false;

	boolean ReadFromFile(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return false;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			data = new Instances(reader);
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Reading of data from file " + fileName
					+ " failed.");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Reading of data from file " + fileName
					+ " failed.");
			return false;
		}
		System.out.println("Reading of data from file " + fileName
				+ " succesful.");
		return true;
	}

	@Override
	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		// register with DF
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();

		sd.setType("ARFFReader");
		sd.setName(getName());
		dfd.setName(getAID());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			System.err.println(getLocalName()
					+ " registration with DF unsucceeded. Reason: "
					+ e.getMessage());
			doDelete();
		}

		MessageTemplate template = MessageTemplate
				.MatchPerformative(ACLMessage.REQUEST);
		addBehaviour(new GetDataResponder(this, template));
		System.out.println("Agent " + getLocalName() + " is ready!");

	} // end Setup

	protected ACLMessage sendData(ACLMessage request) {
		// responding message
		ACLMessage msgOut = request.createReply();
		msgOut.setPerformative(ACLMessage.INFORM);
		try {
			ContentElement content = getContentManager()
					.extractContent(request);
			String file_name = ((GetData) ((Action) content).getAction())
					.getFile_name();
			DataInstances instances = new DataInstances();
			// Read the file
			working = true;
			boolean file_read = ReadFromFile(file_name);
			working = false;
			if (!file_read) {
				throw new FailureException(
						"File haven't been read. Wrong file-name?");
			}

			instances.fillWekaInstances(data);

			Metadata m = new Metadata();
			m.setInternal_name(file_name);
			int ninst = instances.getInstances().size();
			if (ninst > 0) {
				m.setNumber_of_attributes(((Instance) instances.getInstances()
						.iterator().next()).getValues().size());
			}
			m.setNumber_of_instances(instances.getInstances().size());
			// DataManagerService.saveMetadata(this, m);

			// Prepare the content
			Result result = new Result((Action) content, instances);
			try {
				getContentManager().fillContent(msgOut, result);
			} catch (CodecException ce) {
				ce.printStackTrace();
			} catch (OntologyException oe) {
				oe.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgOut;
	} // end SendData

	private class GetDataResponder extends AchieveREResponder {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4340928332826216394L;

		public GetDataResponder(Agent a, MessageTemplate mt) {
			super(a, mt);
		}

		@Override
		protected ACLMessage prepareResponse(ACLMessage request) {
			ACLMessage agree = request.createReply();
			agree.setPerformative(ACLMessage.AGREE);
			return agree;
			// return null;
		} // end prepareResponse

		@Override
		protected ACLMessage prepareResultNotification(ACLMessage request,
				ACLMessage response) {
			try {
				ContentElement content = getContentManager().extractContent(
						request);
				// GetData
				if (((Action) content).getAction() instanceof GetData) {
					return sendData(request);
				}
			} catch (CodecException ce) {
				ce.printStackTrace();
			} catch (OntologyException oe) {
				oe.printStackTrace();
			}

			ACLMessage notUnderstood = request.createReply();
			notUnderstood.setPerformative(ACLMessage.NOT_UNDERSTOOD);
			return notUnderstood;
		} // end prepareResultNotification

	}
}
