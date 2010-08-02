import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Dimension;

public class ChooseXValuesOptionManagerOptionsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JTextField jTextField = null;

	/**
	 * This is the default constructor
	 */
	public ChooseXValuesOptionManagerOptionsPanel() {
		super();
		initialize();
	}
	
	public String getDefaultTries() {
		return jTextField.getText();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.gridx = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		jLabel = new JLabel();
		jLabel.setText("Default Number of Values to Try");
		this.setSize(242, 20);
		this.setLayout(new GridBagLayout());
		this.add(jLabel, gridBagConstraints);
		this.add(getJTextField(), gridBagConstraints1);
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new Dimension(40, 20));
			jTextField.setText("5");
		}
		return jTextField;
	}

}  //  @jve:decl-index=0:visual-constraint="0,0"
