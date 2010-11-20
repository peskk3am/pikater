package pikater.agents.computing;

import pikater.agents.computing.Agent_ComputingAgent.states;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.NNge;

public class Agent_NNge extends Agent_WekaCA {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4060650602467450724L;
	private NNge cls = new NNge();

	@Override
	protected NNge getModelObject() {
		return cls;
	}

	@Override
	protected String getOptFileName() {
		return "/options/NNge.opt";
	}

	@Override
	protected boolean setModelObject(Classifier _cls) {
		try {
			cls = (NNge) _cls;
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}

	@Override
	public String getAgentType() {
		return "NNge";
	}

	@Override
	protected void train() throws Exception {
		working = true;
		System.out.println("Agent " + getLocalName() + ": Training...");

		cls = new NNge();
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