import jade.gui.GuiEvent;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JList;
import java.awt.GridBagLayout;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.JDesktopPane;
import java.awt.ComponentOrientation;
import javax.swing.JInternalFrame;
import java.awt.Rectangle;
import javax.swing.JWindow;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.JMenu;
import java.awt.GridBagConstraints;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import ontology.messages.Task;

public class MainWindow extends JFrame {

	public static final int GET_FILES_INFO = 0;
	public static final int UPDATE_METADATA = 1;
	public static final int ON_LOAD = 2;
	public static final int START_EXPERIMENT = 3;
	
	private class TreePanel {
		
		String name;
		JPanel panel;
		
		public TreePanel(String name, JPanel panel) {
			this.name = name;
			this.panel = panel;
		}
		
		public String toString() {
			return name;
		}
		
		public JPanel getPanel() {
			return panel;
		}
	}
	
	private static final long serialVersionUID = 1L;
	private JSplitPane jSplitPane = null;
	private JPanel jPanel1 = null;
	private JTree jTree = null;
	private TreeModel menuModel = null;  //  @jve:decl-index=0:
	private Agent_GUI myAgent = null;
	private FileManagerPanel fileManager = null;
	private NewExperimentPanel experimentPanel = null;
	private ResultsPanel resultsPanel = null;
	
	
	public MainWindow(Agent_GUI myAgent) {
		super();
		this.myAgent = myAgent;
		fileManager = new FileManagerPanel(myAgent);
		experimentPanel = new NewExperimentPanel(myAgent);
		resultsPanel = new ResultsPanel();
		initialize();
	}
	
	public void addResult(Task t) {
		resultsPanel.addResult(t);
	}
	
	public void setAgents(Vector<String> agents) {};
	/**
	 * This is the default constructor
	 */
	public MainWindow() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(800, 600);
		this.setContentPane(getJSplitPane());
		this.setName("mainFrame");
		this.setTitle("Pikater");
		this.setVisible(true);	
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setDividerLocation(120);
			jSplitPane.setLeftComponent(getJTree());
			jSplitPane.setRightComponent(getJPanel1());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setPreferredSize(new Dimension(520, 480));
		}
		return jPanel1;
	}

	/**
	 * This method initializes jTree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	private JTree getJTree() {
		if (jTree == null) {
			jTree = new JTree();
			jTree.setRootVisible(false);
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
			DefaultMutableTreeNode files = new DefaultMutableTreeNode(new TreePanel("Files", fileManager));
			DefaultMutableTreeNode experiments = new DefaultMutableTreeNode(new TreePanel("Experiments", experimentPanel));
			experiments.add(new DefaultMutableTreeNode(new TreePanel("New", experimentPanel)));
			experiments.add(new DefaultMutableTreeNode(new TreePanel("Results", resultsPanel)));
			root.add(files);
			root.add(experiments);
			menuModel = new DefaultTreeModel(root);
			jTree.setModel(menuModel);
			jTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
					TreePanel selectedPanel = (TreePanel)((DefaultMutableTreeNode)jTree.getLastSelectedPathComponent()).getUserObject();
					jSplitPane.setRightComponent(selectedPanel.getPanel());
				}
			});
		}
		return jTree;
	}

}  //  @jve:decl-index=0:visual-constraint="220,5"
