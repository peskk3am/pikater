package ontology.messages;

import jade.content.Concept;

public class Data implements Concept{
	private String _train_file_name;
	private String _test_file_name;
	private Metadata _metadata;
	
	private int _gui_id;  // not included in ontology
	
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
	public void setMetadata(Metadata metadata) {
		_metadata = metadata;
	}
	public Metadata getMetadata() {
		return _metadata;
	}
	public void setGui_id(int _gui_id) {
		this._gui_id = _gui_id;
	}
	public int getGui_id() {
		return _gui_id;
	}

}
