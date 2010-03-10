package ontology.messages;
import jade.content.Concept;

public class Execute implements Concept{
	private String _options;
	
	public void setOptions(String options) {
		_options=options;
	}
	public String getOptions() {
		return _options;
	}
}
