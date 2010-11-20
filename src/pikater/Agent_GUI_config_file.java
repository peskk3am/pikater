package pikater;

import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Result;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import jade.util.leap.List;

import java.io.IOException;

import org.jdom.JDOMException;

import pikater.ontology.messages.Problem;
import pikater.ontology.messages.Results;
import pikater.ontology.messages.Task;

public class Agent_GUI_config_file extends Agent_GUI {

	/**
	 * 
	 */
	private static final long serialVersionUID = -709390383325209787L;
	private String path = System.getProperty("user.dir")
			+ System.getProperty("file.separator");
	private String configFileName;

	@Override
	protected void displayOptions(Problem problem, int performative) {
		String msg = "Failed";
		if (performative == ACLMessage.INFORM) {
			msg = "OK";
		}
		System.out.println("Agent :" + getName()
				+ ": Displaying the options ;) " + msg);
	} // end displayOptions

	@Override
	protected void displayResult(ACLMessage inform) {
		ContentElement content;
		try {
			content = getContentManager().extractContent(inform);
			if (content instanceof Result) {
				Result result = (Result) content;
				if (result.getValue() instanceof Results) {
					List tasks = ((Results) result.getValue()).getResults();
					if (tasks != null) {
						Iterator itr = tasks.iterator();
						while (itr.hasNext()) {
							Task task = (Task) itr.next();
							System.out.println("Agent " + getLocalName()
									+ ": options for agent "
									+ task.getAgent().getName() + " were "
									+ task.getAgent().optionsToString()
									+ " error_rate: "
									+ task.getResult().getError_rate());
						}
					} else {
						System.out.println("Agent " + getLocalName()
								+ ": there were no tasks in this computation.");
					}
				}
			}

		} catch (UngroundedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CodecException e) {
			System.out.println("Agent " + getLocalName() + " "
					+ inform.getContent());
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void DisplayWrongOption(int problemGuiId, String agentName,
			String optionName, String errorMessage) {
		System.out.println("Agent :" + getName() + " " + problemGuiId + " "
				+ agentName + " " + optionName + " " + errorMessage);
	}

	@Override
	protected void allOptionsReceived(int problem_id) {
		sendProblem(problem_id);
	}

	@Override
	protected String getAgentType() {
		return "GUI config file";
	}

	@Override
	protected void mySetup() {
		setDefault_number_of_values_to_try(3);
		setDefault_error_rate(0.01);

		doWait(1000);

		/*
		 * // test: int newId = createNewProblem("1000"); try {
		 * //addAgentToProblem(newId, null, "MultilayerPerceptron",
		 * "-L 0.4 -D -M ? -H ?,?"); // addAgentToProblem(newId, null,
		 * "RBFNetwork", "-B 4"); addAgentToProblem(newId, null, "?", null);
		 * 
		 * } catch (FailureException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } addDatasetToProblem(newId, "iris.arff",
		 * "iris.arff", null, null); // getAgentOptions("mp1"); //
		 */

		System.out.println("Agent types: " + offerAgentTypes());

		configFileName = getConfigFileName();
		try {
			getProblemsFromXMLFile(configFileName);
		}
		// indicates a well-formedness error
		catch (JDOMException e) {
			System.out.println(configFileName + " is not well-formed. "
					+ e.getMessage());
		} catch (IOException e) {
			System.out.print("Could not check " + configFileName);
			System.out.println(" because " + e.getMessage());
		}
		// */

	} // end mySetup

	@Override
	protected void displayPartialResult(ACLMessage inform) {
		System.out.println("Partial results");
	}

	private String getConfigFileName() {
		return (String) getArguments()[0];
	}

	@Override
	protected void onGuiEvent(GuiEvent arg0) {
		// TODO Auto-generated method stub

	}

}
