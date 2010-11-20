package pikater.gui.java;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FilePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JComboBox jComboBox = null;
	private JLabel jLabel1 = null;
	private JComboBox jComboBox1 = null;
	private String[] filesList = null;
	private JButton jButton = null;

	/**
	 * This is the default constructor
	 */
	public FilePanel() {
		super();
		initialize();
	}

	public FilePanel(String[] filesList) {
		super();
		this.filesList = filesList;
		initialize();
	}

	public void addFile(String name) {
		jComboBox1.addItem(name);
		jComboBox1.setSelectedItem(name);
		jComboBox.addItem(name);
		jComboBox.setSelectedItem(name);
	}

	public String getTestFile() {
		return jComboBox1.getSelectedItem().toString();
	}

	public String getTrainFile() {
		return jComboBox.getSelectedItem().toString();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 3;
		gridBagConstraints11.gridy = 0;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints3.gridy = 0;
		gridBagConstraints3.weightx = 1.0;
		gridBagConstraints3.gridx = 3;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.gridy = 0;
		jLabel1 = new JLabel();
		jLabel1.setText("Test File");
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.gridx = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		jLabel = new JLabel();
		jLabel.setText("Train File");
		this.setSize(311, 24);
		this.setLayout(new GridBagLayout());
		this.add(jLabel, gridBagConstraints);
		this.add(getJComboBox(), gridBagConstraints1);
		this.add(jLabel1, gridBagConstraints2);
		this.add(getJComboBox1(), gridBagConstraints3);
		this.add(getJButton());
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox(filesList);
			jComboBox.setPreferredSize(new Dimension(130, 24));
			jComboBox.setEditable(true);
		}
		return jComboBox;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBox1() {
		if (jComboBox1 == null) {
			jComboBox1 = new JComboBox(filesList);
			jComboBox1.setPreferredSize(new Dimension(130, 24));
			jComboBox1.setEditable(true);
		}
		return jComboBox1;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Remove");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Container parent = getParent();
					parent.remove(FilePanel.this);
					((JPanel) parent).revalidate();
					parent.repaint();
				}
			});
		}
		return jButton;
	}

} // @jve:decl-index=0:visual-constraint="0,0"
