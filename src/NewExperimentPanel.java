import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JDesktopPane;
import java.awt.Rectangle;
import javax.swing.JButton;
import java.awt.Point;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.Vector;

public class NewExperimentPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JSplitPane jSplitPane = null;
	private RandomOptionsManagerOptionsPanel randomPanel = null;
	private ChooseXValuesOptionManagerOptionsPanel chooseXvaluesPanel = null;
	private GuiAgent myAgent = null;
	private String[] agentTypes = null;
	private JPanel jPanel = null;
	private JComboBox optionManagerType = null;
	private JPanel optionsPanel = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JPanel jPanel3 = null;
	private JButton jButton = null;
	private JTabbedPane jTabbedPane = null;
	private JPanel jPanel4 = null;
	private JPanel jPanel5 = null;
	private JPanel jPanel6 = null;
	private JPanel jPanel7 = null;
	private JButton addAgentButton = null;
	private JPanel jPanel8 = null;
	private JPanel jPanel9 = null;
	private JButton addFileButton = null;
	private JScrollPane filesScrollPane = null;
	private JScrollPane agentsScrollPane = null;
	private JPanel agentsPanel = null;
	private String[] filesList = null;
	private JPanel filesPanel = null;
	/**
	 * This is the default constructor
	 */
	public NewExperimentPanel() {
		super();
		initialize();
	}

	public NewExperimentPanel(GuiAgent myAgent) {
		super();
		this.myAgent = myAgent;
		randomPanel = new RandomOptionsManagerOptionsPanel();
		chooseXvaluesPanel = new ChooseXValuesOptionManagerOptionsPanel();
		initialize();
		optionsPanel.add(randomPanel);
		GuiEvent ge = new GuiEvent(this, MainWindow.ON_LOAD);
		myAgent.postGuiEvent(ge);
	}
	
	public void setAgentTypes(String[] agentTypes) {
		this.agentTypes = agentTypes;
		addAgentButton.setEnabled(true);
		if (agentsPanel.getComponentCount() == 0) {
			agentsPanel.add(new AgentOptionsPanel(agentTypes));
		}
	}
	
	public void setFilesList(String[] filesList) {
		this.filesList = filesList;
		addFileButton.setEnabled(true);
		if (filesPanel.getComponentCount() == 0)
			filesPanel.add(new FilePanel(filesList));
	}
	
	public void addFile(String name) {
		for (Component c : filesPanel.getComponents()) {
			FilePanel fp = (FilePanel)c;
			fp.addFile(name);
		}
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
		this.setSize(542, 600);
		this.setLayout(new GridBagLayout());
		this.add(getJSplitPane(), gridBagConstraints);
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane.setPreferredSize(new Dimension(542, 600));
			jSplitPane.setTopComponent(getJPanel());
			jSplitPane.setBottomComponent(getJPanel1());
			jSplitPane.setDividerLocation(160);
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridwidth = 2;
			gridBagConstraints3.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridx = 1;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "Option Manager", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
			jPanel.setPreferredSize(new Dimension(540, 140));
			jPanel.add(getOptionManagerType(), gridBagConstraints2);
			jPanel.add(getOptionsPanel(), gridBagConstraints3);
		}
		return jPanel;
	}

	/**
	 * This method initializes optionManagerType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getOptionManagerType() {
		String[] optionManagers = {"Random","ChooseXValues"};
		if (optionManagerType == null) {
			optionManagerType = new JComboBox(optionManagers);
			optionManagerType.setPreferredSize(new Dimension(200, 24));
			optionManagerType.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					optionsPanel.removeAll();
					if (e.getItem().equals("Random")) {
						optionsPanel.add(randomPanel);	
					} 
					else {
						optionsPanel.add(chooseXvaluesPanel);
					}
					NewExperimentPanel.this.validate();
					NewExperimentPanel.this.repaint();
				}
			});
			optionManagerType.setPreferredSize(new Dimension(200, 24));
		}
		return optionManagerType;
	}

	/**
	 * This method initializes optionsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOptionsPanel() {
		if (optionsPanel == null) {
			optionsPanel = new JPanel();
			optionsPanel.setLayout(new GridBagLayout());
			optionsPanel.setBorder(BorderFactory.createTitledBorder(null, "Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
			optionsPanel.setPreferredSize(new Dimension(480, 80));
		}
		return optionsPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints5.gridy = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridwidth = 3;
			gridBagConstraints4.gridy = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setPreferredSize(new Dimension(480, 400));
			jPanel1.add(getJPanel2(), gridBagConstraints4);
			jPanel1.add(getJPanel3(), gridBagConstraints5);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.gridx = 0;
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.setPreferredSize(new Dimension(500, 280));
			jPanel2.setBorder(BorderFactory.createTitledBorder(null, "Agent and Files", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
			jPanel2.add(getJTabbedPane(), gridBagConstraints7);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 0;
			jPanel3 = new JPanel();
			jPanel3.setLayout(new GridBagLayout());
			jPanel3.setPreferredSize(new Dimension(500, 40));
			jPanel3.add(getJButton(), gridBagConstraints6);
		}
		return jPanel3;
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
			jButton.setText("Run");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					Vector<String> agents = new Vector<String>();
					Vector<String> agentOptions = new Vector<String>();
					Vector<String> trainFiles = new Vector<String>();
					Vector<String> testFiles = new Vector<String>();
					
					for (int i = 0; i < agentsPanel.getComponentCount(); i++) {
						AgentOptionsPanel aop = (AgentOptionsPanel)agentsPanel.getComponent(i);
						
						agents.add(aop.getAgentType());
						agentOptions.add(aop.getAgentParams());
					}
					
					for (int i = 0; i < filesPanel.getComponentCount(); i++) {
						FilePanel fp = (FilePanel)filesPanel.getComponent(i);
						
						trainFiles.add(fp.getTrainFile());
						testFiles.add(fp.getTestFile());
					}
					
					Vector<String> optionManager = new Vector<String>();
					
					optionManager.add(optionManagerType.getSelectedItem().toString());
					
					if (optionManager.get(0).equals("Random")) {
						RandomOptionsManagerOptionsPanel rp = (RandomOptionsManagerOptionsPanel)optionsPanel.getComponent(0);
						
						optionManager.add(rp.getErrorRate());
						optionManager.add(rp.getMaxTries());
						
					}
					
					if (optionManager.get(0).equals("ChooseXValues")) {
						ChooseXValuesOptionManagerOptionsPanel cxv = (ChooseXValuesOptionManagerOptionsPanel)optionsPanel.getComponent(0);
						
						System.err.println("Default tries: " + cxv.getDefaultTries());
						optionManager.add(cxv.getDefaultTries());
					}
					
					GuiEvent ge = new GuiEvent(this, MainWindow.START_EXPERIMENT);
					ge.addParameter(agents);
					ge.addParameter(agentOptions);
					ge.addParameter(trainFiles);
					ge.addParameter(testFiles);
					ge.addParameter(optionManager);
					myAgent.postGuiEvent(ge);
					
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("Agents", null, getJPanel4(), null);
			jTabbedPane.addTab("Files", null, getJPanel5(), null);
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.gridy = 1;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 0;
			jPanel4 = new JPanel();
			jPanel4.setLayout(new GridBagLayout());
			jPanel4.add(getJPanel6(), gridBagConstraints8);
			jPanel4.add(getJPanel7(), gridBagConstraints9);
		}
		return jPanel4;
	}

	/**
	 * This method initializes jPanel5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 0;
			jPanel5 = new JPanel();
			jPanel5.setLayout(new GridBagLayout());
			jPanel5.add(getJPanel8(), gridBagConstraints11);
			jPanel5.add(getJPanel9(), gridBagConstraints12);
		}
		return jPanel5;
	}

	/**
	 * This method initializes jPanel6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.fill = GridBagConstraints.BOTH;
			gridBagConstraints15.gridy = 0;
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.weighty = 1.0;
			gridBagConstraints15.gridx = 0;
			jPanel6 = new JPanel();
			jPanel6.setLayout(new GridBagLayout());
			jPanel6.setPreferredSize(new Dimension(480, 180));
			jPanel6.add(getAgentsScrollPane(), gridBagConstraints15);
		}
		return jPanel6;
	}

	/**
	 * This method initializes jPanel7	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel7() {
		if (jPanel7 == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.gridy = 0;
			jPanel7 = new JPanel();
			jPanel7.setLayout(new GridBagLayout());
			jPanel7.setPreferredSize(new Dimension(480, 40));
			jPanel7.add(getAddAgentButton(), gridBagConstraints10);
		}
		return jPanel7;
	}

	/**
	 * This method initializes addAgentButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddAgentButton() {
		if (addAgentButton == null) {
			addAgentButton = new JButton();
			addAgentButton.setPreferredSize(new Dimension(100, 24));
			addAgentButton.setText("Add agent");
			addAgentButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					agentsPanel.add(new AgentOptionsPanel(agentTypes));
					agentsPanel.revalidate();
				}
			});
		}
		return addAgentButton;
	}

	/**
	 * This method initializes jPanel8	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel8() {
		if (jPanel8 == null) {
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.BOTH;
			gridBagConstraints14.gridy = 0;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.weighty = 1.0;
			gridBagConstraints14.gridx = 0;
			jPanel8 = new JPanel();
			jPanel8.setLayout(new GridBagLayout());
			jPanel8.setPreferredSize(new Dimension(480, 180));
			jPanel8.add(getFilesScrollPane(), gridBagConstraints14);
		}
		return jPanel8;
	}

	/**
	 * This method initializes jPanel9	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel9() {
		if (jPanel9 == null) {
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.gridy = 0;
			jPanel9 = new JPanel();
			jPanel9.setLayout(new GridBagLayout());
			jPanel9.setPreferredSize(new Dimension(480, 40));
			jPanel9.add(getAddFileButton(), gridBagConstraints13);
		}
		return jPanel9;
	}

	/**
	 * This method initializes addFileButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddFileButton() {
		if (addFileButton == null) {
			addFileButton = new JButton();
			addFileButton.setPreferredSize(new Dimension(100, 24));
			addFileButton.setEnabled(false);
			addFileButton.setText("Add file");
			addFileButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					filesPanel.add(new FilePanel(filesList));
					filesPanel.revalidate();
				}
			});
		}
		return addFileButton;
	}

	/**
	 * This method initializes filesScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getFilesScrollPane() {
		if (filesScrollPane == null) {
			filesScrollPane = new JScrollPane();
			filesScrollPane.setViewportView(getFilesPanel());
		}
		return filesScrollPane;
	}

	/**
	 * This method initializes agentsScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getAgentsScrollPane() {
		if (agentsScrollPane == null) {
			agentsScrollPane = new JScrollPane();
			agentsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			agentsScrollPane.setViewportView(getAgentsPanel());
		}
		return agentsScrollPane;
	}

	/**
	 * This method initializes agentsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getAgentsPanel() {
		if (agentsPanel == null) {
			agentsPanel = new JPanel();
			agentsPanel.setLayout(new BoxLayout(getAgentsPanel(), BoxLayout.Y_AXIS));
		}
		return agentsPanel;
	}

	/**
	 * This method initializes filesPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getFilesPanel() {
		if (filesPanel == null) {
			filesPanel = new JPanel();
			filesPanel.setLayout(new BoxLayout(getFilesPanel(), BoxLayout.Y_AXIS));
		}
		return filesPanel;
	}

}