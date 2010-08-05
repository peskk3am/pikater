package ontology.messages;

import jade.content.Concept;

public class Data implements Concept{
	private String _train_file_name;
	private String _external_train_file_name;
	private String _test_file_name;
	private String _external_test_file_name;
	private Metadata _metadata;
	private String _output = "evaluation_only"; // "predictions"
	private String _mode = "train_test";  // test_only, train_test
	
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
	public void setOutput(String _output) {
		this._output = _output;
	}
	public String getOutput() {
		return _output;
	}
	public void setGui_id(int _gui_id) {
		this._gui_id = _gui_id;
	}
	public int getGui_id() {
		return _gui_id;
	}
	public void setMode(String mode) {
		_mode = mode;
	}
	public String getMode() {
		return _mode;
	}
	public void setExternal_train_file_name(String _external_train_file_name) {
		this._external_train_file_name = _external_train_file_name;
	}
	public String getExternal_train_file_name() {
		return _external_train_file_name;
	}
	public void setExternal_test_file_name(String _external_test_file_name) {
		this._external_test_file_name = _external_test_file_name;
	}
	public String getExternal_test_file_name() {
		return _external_test_file_name;
	}

}
