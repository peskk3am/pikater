package pikater;

import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;

import java.util.Random;

import pikater.ontology.messages.Evaluation;
import pikater.ontology.messages.Option;

public class Agent_Random extends Agent_OptionsManager {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2777277001533605329L;
	private int number_of_tries = 0;

	@Override
	protected String getAgentType() {
		return "Random";
	}

	@Override
	protected boolean finished() {
		if (number_of_tries >= maximum_tries) {
			return true;
		}

		if (evaluation != null) {
			if (evaluation.getError_rate() < error_rate) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void generateNewOptions(Evaluation result) {
		// go through the Options Vector, find mutable options, generate random
		// values, make it a string

		List newOptions = new ArrayList();

		Random generator = new Random();
		Iterator itr = Options.iterator();
		while (itr.hasNext()) {
			Option next = (Option) itr.next();

			if (next.getMutable()) {

				// String optionName = " -"+next.getName()+" ";

				// int numArgs = (int)(next.getNumber_of_args().getMin()
				// +generator.nextInt((int)(next.getNumber_of_args().getMax()-next.getNumber_of_args().getMin()+1)));

				String[] values = next.getUser_value().split(",");
				int numArgs = values.length;

				if (!next.getIs_a_set()) {
					if (next.getData_type().equals("INT") || next.getData_type().equals("MIXED")) {

						String si = "";
						for (int i = 1; i < numArgs; i++) {
							if (values[i - 1].equals("?")) {
								int rInt = (int) (next.getRange().getMin() + generator
										.nextInt((int) (next.getRange().getMax() - next
												.getRange().getMin())));
								si += Integer.toString(rInt) + ",";
							}
							else {
								si += values[i - 1] + ",";
							}							
						}
						if (values[numArgs - 1].equals("?")) {
							int rInt = (int) (next.getRange().getMin() + generator
									.nextInt((int) (next.getRange().getMax() - next
											.getRange().getMin())));
							si += Integer.toString(rInt);
						}
						else {
							si += values[numArgs - 1] + ",";
						}							
						
						next.setValue(si);
					}
					if (next.getData_type().equals("FLOAT")) {
						String sf = "";
						for (int i = 1; i < numArgs; i++) {
							if (values[i - 1].equals("?")) {							
								float rFloat = next.getRange().getMin()
										+ (float) (generator.nextDouble())
										* (next.getRange().getMax() - next
												.getRange().getMin());
								sf += Float.toString(rFloat) + ",";
							}
							else {
								sf += values[i - 1] + ",";
							}
						}
						if (values[numArgs - 1].equals("?")) {
							float rFloat = next.getRange().getMin()
									+ (float) (generator.nextDouble())
									* (next.getRange().getMax() - next.getRange()
											.getMin());
							sf += Float.toString(rFloat);
						}
						else {
							sf += values[numArgs - 1];
						}
						next.setValue(sf);
					}
					if (next.getData_type().equals("BOOLEAN")) {
						int rInt2 = generator.nextInt(2);
						if (rInt2 == 1) {
							next.setValue("True");
						} else {
							next.setValue("False");
						}
					}
				} else {
					String s = "";
					for (int i = 1; i < numArgs; i++) {
						if (values[i - 1].equals("?")) {
							int index = generator.nextInt(next.getSet().size());
							s += next.getSet().get(index) + ",";
						} else {
							s += values[i - 1] + ",";
						}

					}
					if (values[numArgs - 1].equals("?")) {
						int index = generator.nextInt(next.getSet().size());
						s += next.getSet().get(index);
					} else {
						s += values[numArgs - 1];
					}
					next.setValue(s);
				}
			}
		}
		number_of_tries++;
	}
}