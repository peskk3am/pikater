package ontology.messages;

import jade.content.Concept;

public class Data implements Concept{
	private String _train_file_name;
	private String _test_file_name;
	
	public void setTrain_file_name(String train_file_name) {
		_train_file_name=train_file_name;
	}
	public String getTrain_file_name() {
		return _train_file_name;
	}
	public void setTest_file_name(String test_file_name) {
		_test_file_name=test_file_name;
	}
	public String getTest_file_name() {
		return _test_file_name;
	}

}
