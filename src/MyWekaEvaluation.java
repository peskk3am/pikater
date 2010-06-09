import java.io.*;
import weka.classifiers.Evaluation;

public class MyWekaEvaluation implements Serializable{
	public double errorRate;
	public double pctIncorrect;
	
	
	public MyWekaEvaluation(Evaluation eval){		
		// double errorRate() Returns the estimated error rate or the root mean squared error (if the class is numeric).
		errorRate = eval.errorRate();
		// double pctIncorrect() Gets the percentage of instances incorrectly classified (that is, for which an incorrect prediction was made).
		pctIncorrect = eval.pctIncorrect();
	}
	
}
