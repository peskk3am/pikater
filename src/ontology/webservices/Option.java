package ontology.webservices;

import jade.content.Concept;

public class Option implements Concept {
	
	private static final long serialVersionUID = -7947161664815178689L;
	
	private String value;
	private String description;
	private String synopsis;
	private String name;
	
	public Option(){}
	
	public Option(String name, String synopsis, String description, String value) {
		this.name = name;
		this.synopsis = synopsis;
		this.description = description;
		setValue(value);
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		if (value == null) {
			value = "";
		}
		this.value = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSynopsis() {
		return synopsis;
	}
	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	

}
