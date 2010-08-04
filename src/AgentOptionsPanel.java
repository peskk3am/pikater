import java.awt.Container;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Dimension;

public class AgentOptionsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JComboBox jComboBox = null;
	private JTextField jTextField = null;
	private JButton jButton = null;
	private String[] agentTypes = null;
	
	/**
	 * This is the default constructor
	 */
	public AgentOptionsPanel() {
		super();
		initialize();
	}

	public AgentOptionsPanel(String[] agentTypes) {
		super();
		this.agentTypes = agentTypes;
		initialize();
	}
	
	public String getAgentType() {
		return jComboBox.getSelectedItem().toString();
	}
	
	public String getAgentParams() {
		return jTextField.getText();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.gridy = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.gridx = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.gridx = 0;
		this.setSize(420, 24);
		this.setLayout(new GridBagLayout());
		this.setPreferredSize(new Dimension(420, 24));
		this.add(getJComboBox(), gridBagConstraints);
		this.add(getJTextField(), gridBagConstraints1);
		this.add(getJButton(), gridBagConstraints2);
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox(agentTypes);
			jComboBox.setPreferredSize(new Dimension(120, 24));
		}
		return jComboBox;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new Dimension(200, 24));
		}
		return jTextField;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setPreferredSize(new Dimension(100, 24));
			jButton.setText("Remove");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Container parent = getParent();
					parent.remove(AgentOptionsPanel.this);
					((JPanel)parent).revalidate();
					parent.repaint();
				}
			});
		}
		return jButton;
	}

}  //  @jve:decl-index=0:visual-constraint="0,0"
