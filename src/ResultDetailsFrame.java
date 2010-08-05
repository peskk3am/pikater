import jade.util.leap.List;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import ontology.messages.Attribute;
import ontology.messages.DataInstances;

public class ResultDetailsFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private DataInstances instances = null;

	/**
	 * This is the default constructor
	 */
	public ResultDetailsFrame() {
		super();
		initialize();
	}

	public ResultDetailsFrame(DataInstances instances) {
		super();
		this.instances = instances;
		initialize();
	}
	
	private class DataInstancesTableModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return instances.getAttributes().size();
		}

		@Override
		public int getRowCount() {
			return instances.getInstances().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return instances.toString(rowIndex, columnIndex);
		}
		
		@Override
		public String getColumnName(int column) {
			return ((Attribute)instances.getAttributes().get(column)).getName();
		}
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
		this.setTitle("Result details");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
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
			jTable = new JTable();
		}
		jTable.setModel(new DataInstancesTableModel());
		return jTable;
	}

}
