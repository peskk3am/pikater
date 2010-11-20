package pikater.agents.computing;

import pikater.agents.computing.Agent_ComputingAgent.states;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;

public class Agent_J48 extends Agent_WekaCA {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6657139587728202137L;
	private J48 cls = new J48();

	@Override
	protected J48 getModelObject() {
		return cls;
	}

	@Override
	protected String getOptFileName() {
		return "/options/J48.opt";
	}

	@Override
	protected boolean setModelObject(Classifier _cls) {
		try {
			cls = (J48) _cls;
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}

	@Override
	public String getAgentType() {
		return "J48";
	}

	@Override
	protected void train() throws Exception {
		working = true;
		System.out.println("Agent " + getLocalName() + ": Training...");

		cls = new J48();
		if (OPTIONS.length > 0) {
			cls.setOptions(OPTIONS);
		}
		cls.buildClassifier(train);
		state = states.TRAINED; // change agent state
		OPTIONS = cls.getOptions();

		// write out net parameters
		System.out.println(getLocalName() + " " + getOptions());

		working = false;
	} // end train

	@Override
	protected Evaluation test() {
		working = true;
		System.out.println("Agent " + getLocalName() + ": Testing...");

		// evaluate classifier and print some statistics
		Evaluation eval = null;
		try {
			eval = new Evaluation(train);
			eval.evaluateModel(cls, test);
			System.out.println(eval.toSummaryString(getLocalName() + " agent: "
					+ "\nResults\n=======\n", false));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		working = false;
		return eval;
	} // end test
}
