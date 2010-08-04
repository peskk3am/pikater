package ontology.messages;

import jade.content.Concept;
import jade.util.leap.*;

public class Agent implements Concept{
	private String _name;
	private String _type;
	private List _options;	
	private String _gui_id;
	
	// Methods required to use this class to represent the OPTIONS role
	public void setOptions(List options) {
		_options=options;
	}
	public List getOptions() {
		return _options;
	}	
	public void setName(String name) {
		_name=name;
	}
	public String getName() {
		return _name;
	}
	public void setType(String type) {
		_type=type;
	}
	public String getType() {
		return _type;
	} 
	public void setGui_id(String gui_id) {
		_gui_id=gui_id;
	}
	public String getGui_id() {
		return _gui_id;
	}
	// -----------------------------
	
	public List stringToOptions(String optString){
    	String[] optArray = optString.split("[ ]+");
		List optList = new ArrayList();
    	for (int i=0; i < optArray.length; i++){
    		if (optArray[i].startsWith("-")){
    			String name = optArray[i].replaceFirst("-", "");
    			// if the next array element is again an option name, 
    			// (or it is the last element)
    			// => it's a boolean parameter
    			
    			Option opt = new Option();
    			String value;
    			if (i == optArray.length-1){
    				value = "True";
    				opt.setData_type("BOOLEAN");
    			}
    			else {
    				// if (optArray[i+1].startsWith("-")){
    				if (optArray[i+1].matches("\\-[A-Z]")){
    					value = "True";
    					opt.setData_type("BOOLEAN");
    				}
    				else{
    					value = optArray[i+1];    				
    				}
    			}
    			
    			opt.setName(name);
    			opt.setValue(value);
    		
    			if (opt.getUser_value() == null){
    				// first string -> options, parsing the string from user 
    				if (value.contains("?")){
    					opt.setMutable(true);
    				}
    				opt.setUser_value(value);
    				
    			}
    			optList.add(opt);	
    		}
    	}
		return optList;
	}
	
	public String optionsToString(){
		String str = "";
		if(_options == null){
			return str;
		}
		Iterator itr = _options.iterator();	 		   		 
	 	while (itr.hasNext()) {
	 		ontology.messages.Option next_opt = (ontology.messages.Option) itr.next();
	 		if (next_opt.getData_type().equals("BOOLEAN")){
	 			if (next_opt.getValue().equals("True")){
	 				str += "-"+next_opt.getName()+" ";	
	 			}
	 		}
	 		else{
	 			str += "-"+next_opt.getName()+" "+next_opt.getValue()+" ";
	 		}
	 	}
	 	return str;
	}
	
}