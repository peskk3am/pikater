package pikater.ontology.messages;

import jade.content.Concept;
import jade.util.leap.List;

public class Results implements Concept {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3411423409276645995L;
	private String _problem_id;
	private String _computation_id;

	private float _avg_error_rate = -1;
	private float _avg_kappa_statistic = -1;
	private float _avg_mean_absolute_error = -1;
	private float _avg_root_mean_squared_error = -1;
	private float _avg_relative_absolute_error = -1;
	private float _avg_root_relative_squared_error = -1;

	private List _results;
	
	public void setProblem_id(String problem_id) {
		_problem_id = problem_id;
	}

	public String getProblem_id() {
		return _problem_id;
	}

	public void setComputation_id(String computation_id) {
		_computation_id = computation_id;
	}

	public String getComputation_id() {
		return _computation_id;
	}

	public void setResults(List results) {
		_results = results;
	}

	public List getResults() {
		return _results;
	}

	public void setAvg_error_rate(float avg_error_rate) {
		_avg_error_rate = avg_error_rate;
	}

	public float getAvg_error_rate() {
		return _avg_error_rate;
	}

	public void setAvg_kappa_statistic(float avg_kappa_statistic) {
		_avg_kappa_statistic = avg_kappa_statistic;
	}

	public float getAvg_kappa_statistic() {
		return _avg_kappa_statistic;
	}

	public void setAvg_mean_absolute_error(float avg_mean_absolute_error) {
		_avg_mean_absolute_error = avg_mean_absolute_error;
	}

	public float getAvg_mean_absolute_error() {
		return _avg_mean_absolute_error;
	}

	public void setAvg_root_mean_squared_error(float avg_root_mean_squared_error) {
		_avg_root_mean_squared_error = avg_root_mean_squared_error;
	}

	public float getAvg_root_mean_squared_error() {
		return _avg_root_mean_squared_error;
	}

	public void setAvg_relative_absolute_error(float avg_relative_absolute_error) {
		_avg_relative_absolute_error = avg_relative_absolute_error;
	}

	public float getAvg_relative_absolute_error() {
		return _avg_relative_absolute_error;
	}

	public void setAvg_root_relative_squared_error(
			float avg_root_relative_squared_error) {
		_avg_root_relative_squared_error = avg_root_relative_squared_error;
	}

	public float getAvg_root_relative_squared_error() {
		return _avg_root_relative_squared_error;
	}
	
}