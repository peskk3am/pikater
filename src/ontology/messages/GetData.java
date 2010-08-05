package ontology.messages;

import jade.content.onto.basic.Action;

public class GetData extends Action {
	private String file_name;

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String fileName) {
		file_name = fileName;
	}
}
