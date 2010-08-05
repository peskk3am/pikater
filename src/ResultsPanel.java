import jade.content.onto.basic.Result;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ontology.messages.Task;

import java.awt.GridBagConstraints;
import java.util.Vector;

public class ResultsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private String[] columns = {"Agent type", "Options", "Train file", "Test file", "Error rate", "Mean absolute error", "MSE", "Kappa"};
	private DefaultTableModel tableModel = new DefaultTableModel(columns, 0);  //  @jve:decl-index=0:visual-constraint="12,223"

	/**
	 * This is the default constructor
	 */
	public ResultsPanel() {
		super();
		initialize();
	}

	public void addResult(Task t) {
		
		Vector<String> data = new Vector<String>();
		
		data.add(t.getAgent().getType());
		data.add(t.getAgent().optionsToString());
		data.add(t.getData().getTrain_file_name());
		data.add(t.getData().getTest_file_name());
		data.add(String.valueOf(t.getResult().getError_rate()));
		data.add(String.valueOf(t.getResult().getMean_absolute_error()));
		data.add(String.valueOf(t.getResult().getRoot_mean_squared_error()));
		data.add(String.valueOf(t.getResult().getKappa_statistic()));
		
		tableModel.addRow(data);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridx = 0;
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(getJScrollPane(), gridBagConstraints);
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable(tableModel);
		}
		return jTable;
	}

}
