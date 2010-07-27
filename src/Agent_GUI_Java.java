import java.awt.TextArea;
import java.awt.event.ActionEvent;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Result;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import ontology.messages.Problem;
import ontology.messages.Results;
import ontology.messages.Task;

import javax.swing.*;

import sun.awt.AWTAccessor;


public class Agent_GUI_Java extends Agent_GUI {

	transient protected MainWindow myGUI;
	
	public Agent_GUI_Java() {
		
		myGUI = new MainWindow(this);
		
		myGUI.setVisible(true);
	}
	
	@Override
	protected void DisplayWrongOption(int problemGuiId, String agentName,
			String optionName, String errorMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void allOptionsReceived(int problemId) {
		sendProblem(problemId);
	}

	@Override
	protected void displayOptions(Problem problem, int performative) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void displayPartialResult(ACLMessage inform) {
		try {
			Result r = (Result)getContentManager().extractContent(inform);
			Results res = (Results)r.getValue();
			List tasks = res.getResults();
			
			Iterator it = tasks.iterator();
			
			while (it.hasNext()) {
				Task t = (Task)it.next();
				myGUI.addResult(t.getAgent().getName(), t.getAgent().optionsToString(), Float.toString(t.getResult().getError_rate()));
			}
		} catch (UngroundedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void displayResult(ACLMessage inform) {
		displayPartialResult(inform);
	}

	@Override
	protected String getAgentType() {
		return "Java GUI Agent";
	}

	@Override
	protected void mySetup() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onGuiEvent(GuiEvent ev) {
		switch (ev.getType()) {
		case MainWindow.ONLOAD :
			myGUI.setAgents(offerAgentTypes()); break;
		case MainWindow.SET_PROBLEM :
			int probID = createNewProblem("30000");
			for (String s: (String [])ev.getParameter(0)) {
				addDatasetToProblem(probID, s, s);
			}
			//addMethodToProblem(probID, "Random", "0.2", "4");
			
			for (Object[] os:(Object[][])ev.getParameter(1)){
				try {
					addAgentToProblem(probID, null, (String)os[0], ((String)os[1]));
				} catch (FailureException e) {
					e.printStackTrace();
				}
				// getAgentOptions((String)os[0]);
			}
			break;
			
		}
		
	}

}
