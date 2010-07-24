package ontology.messages;

import jade.content.onto.basic.Action;

public class SaveMetadata extends Action{

	private String _file_name;  // internal
	private Metadata _metadata;	
	
	public Metadata getMetadata() {
		return _metadata;
	}
	public void setMetadata(Metadata metadata) {
		_metadata = metadata;
	}
	public void setFile_name(String _file_name) {
		this._file_name = _file_name;
	}
	public String getFile_name() {
		return _file_name;
	}
	
}