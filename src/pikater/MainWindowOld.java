package pikater;

import jade.gui.GuiEvent;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import pikater.gui.java.Agent_GUI_Java;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class MainWindowOld extends javax.swing.JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5169701096206767491L;
	public final static int ONLOAD = 0;
	public final static int SET_PROBLEM = 1;

	{
		// Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
					.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JTabbedPane mainPane;
	private Agent_GUI_Java myAgent;
	private JPanel agentsPanel;
	private JPanel filesPanel;
	private JPanel resultsPanel;
	private JPanel jPanel1;
	private JTextField agentParams2;
	private JComboBox agentClass2;
	private JComboBox agentClass1;
	private JTextField agentParams1;
	private JTextField file2;
	private JTable resultsTable;
	private JScrollPane jScrollPane1;
	private JButton startButton;
	private JTextField file1;
	private DefaultTableModel resultsTableModel;

	/**
	 * Auto-generated main method to display this JFrame
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainWindowOld inst = new MainWindowOld();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}

	public MainWindowOld(Agent_GUI_Java myAgent) {
		super();
		this.myAgent = myAgent;
		initGUI();
	}

	public MainWindowOld() {
		super();
		initGUI();
	}

	public void setAgents(Vector<String> agents) {
		agentClass1.removeAllItems();
		agentClass2.removeAllItems();
		for (String s : agents) {
			agentClass1.addItem(new String(s));
			agentClass2.addItem(new String(s));
		}
		agentClass1.validate();
	}

	public void addResult(String agentName, String agentParams, String error) {
		resultsTableModel
				.addRow(new Object[] { agentName, agentParams, error });
		// resultsTable.setModel(resultsTableModel);
	}

	private void initGUI() {
		resultsTableModel = new DefaultTableModel(0, 3);
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jPanel1 = new JPanel();
				getContentPane().add(jPanel1, BorderLayout.CENTER);
				jPanel1.setLayout(null);
				jPanel1.setPreferredSize(new java.awt.Dimension(617, 397));
				{
					mainPane = new JTabbedPane();
					jPanel1.add(mainPane);
					mainPane.setBounds(10, 14, 591, 308);
					{
						agentsPanel = new JPanel();
						mainPane.addTab("Agents", null, agentsPanel, null);
						agentsPanel.setLayout(null);
						{
							agentParams1 = new JTextField();
							agentsPanel.add(agentParams1);
							agentParams1.setBounds(187, 12, 135, 19);
						}
						{
							ComboBoxModel agentClass1Model = new DefaultComboBoxModel();
							agentClass1 = new JComboBox();
							agentsPanel.add(agentClass1);
							agentClass1.setModel(agentClass1Model);
							agentClass1.setBounds(12, 10, 169, 23);
						}
						{
							ComboBoxModel agentClass2Model = new DefaultComboBoxModel();
							agentClass2 = new JComboBox();
							agentsPanel.add(agentClass2);
							agentClass2.setModel(agentClass2Model);
							agentClass2.setBounds(12, 45, 169, 22);
						}
						{
							agentParams2 = new JTextField();
							agentsPanel.add(agentParams2);
							agentParams2.setBounds(187, 47, 135, 19);
						}
					}
					{
						filesPanel = new JPanel();
						mainPane.addTab("Files", null, filesPanel, null);
						filesPanel.setLayout(null);
						filesPanel.setPreferredSize(new java.awt.Dimension(385,
								243));
						{
							file1 = new JTextField();
							filesPanel.add(file1);
							file1.setBounds(12, 12, 122, 33);
						}
						{
							file2 = new JTextField();
							filesPanel.add(file2);
							file2.setBounds(12, 53, 122, 33);
						}
					}
					{
						resultsPanel = new JPanel();
						mainPane.addTab("Results", null, resultsPanel, null);
						{
							jScrollPane1 = new JScrollPane();
							resultsPanel.add(jScrollPane1);
							jScrollPane1
									.setPreferredSize(new java.awt.Dimension(
											577, 272));
							{
								resultsTable = new JTable(resultsTableModel);
								jScrollPane1.setViewportView(resultsTable);
								resultsTable
										.setPreferredSize(new java.awt.Dimension(
												577, 272));
							}
						}
					}
				}
				{
					startButton = new JButton();
					jPanel1.add(startButton);
					startButton.setText("Start");
					startButton.setBounds(257, 352, 63, 25);
					startButton.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent evt) {
							startButtonMouseClicked(evt);
						}
					});
				}
			}
			pack();

			GuiEvent ge = new GuiEvent(agentClass1, ONLOAD);
			myAgent.postGuiEvent(ge);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startButtonMouseClicked(MouseEvent evt) {
		System.out.println("startButton.mouseClicked, event=" + evt);
		GuiEvent ge = new GuiEvent(evt.getSource(), SET_PROBLEM);
		ge.addParameter(new String[] { file1.getText(), file2.getText() });
		ge.addParameter(new Object[][] {
				{ agentClass1.getSelectedItem(), agentParams1.getText() },
				{ agentClass2.getSelectedItem(), agentParams2.getText() } });
		myAgent.postGuiEvent(ge);
	}

}
