package pikater.gui.java;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.util.leap.ArrayList;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import pikater.ontology.messages.Metadata;

public class FileManagerPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JSplitPane jSplitPane = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private JTextField jTextField = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	private GuiAgent myAgent = null;
	private FilesTableModel filesModel = null;

	public FileManagerPanel(GuiAgent a) {
		super();
		myAgent = a;
		filesModel = new FilesTableModel();
		initialize();
	}

	public void reloadFileInfo() {
		GuiEvent ge = new GuiEvent(this, MainWindow.GET_FILES_INFO);
		ge.addParameter(1);
		myAgent.postGuiEvent(ge);
	}

	public void setFiles(ArrayList data) {
		filesModel.setFiles(data);
		jTable.setModel(filesModel);
		jTable.createDefaultColumnsFromModel();
	}

	String[] attributesTypes = { "Categorical", "Numerical", "Mixed" };
	String[] defaultTasks = { "Classification", "Regression", "Clustering" };

	private class FilesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -5772409714155549244L;

		ArrayList data = new ArrayList();

		private String columnName(int index) {
			switch (index) {
			case 0:
				return "Filename";
			case 1:
				return "Number of attributes";
			case 2:
				return "Attributes type";
			case 3:
				return "Number of instances";
			case 4:
				return "Missing values";
			case 5:
				return "Default task";
			default:
				return "";
			}
		}

		private Object getColumnValue(Metadata m, int index) {
			switch (index) {
			case 0:
				return m.getExternal_name();
			case 1:
				return m.getNumber_of_attributes();
			case 2:
				return m.getAttribute_type();
			case 3:
				return m.getNumber_of_instances();
			case 4:
				return m.getMissing_values();
			case 5:
				return m.getDefault_task();
			default:
				return "";
			}
		}

		private void setColumnValue(Metadata m, int index, Object value) {
			switch (index) {
			case 0:
				m.setExternal_name((String) value);
				break;
			case 1:
				m.setNumber_of_attributes((Integer) value);
				break;
			case 2:
				m.setAttribute_type((String) value);
				break;
			case 3:
				m.setNumber_of_instances((Integer) value);
				break;
			case 4:
				m.setMissing_values((Boolean) value);
				break;
			case 5:
				m.setDefault_task((String) value);
				break;
			}
		}

		public void setFiles(ArrayList data) {
			this.data = data;
		}

		public FilesTableModel() {
			reloadFileInfo();
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			// super.addTableModelListener(l);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (data == null) {
				return String.class;
			}
			Object value = getColumnValue((Metadata) data.get(0), columnIndex);
			if (value == null) {
				return String.class;
			}
			return getColumnValue((Metadata) data.get(0), columnIndex)
					.getClass();
		}

		@Override
		public int getColumnCount() {
			if (data.size() == 0) {
				return 0;
			}
			return 6;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnName(columnIndex);
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object value = getColumnValue((Metadata) data.get(rowIndex),
					columnIndex);

			return value;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex > 0) {
				return true;
			}
			return false;
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			// super.removeTableModelListener(l);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Metadata update = (Metadata) data.get(rowIndex);
			setColumnValue(update, columnIndex, aValue);
			GuiEvent ge = new GuiEvent(this, MainWindow.UPDATE_METADATA);
			ge.addParameter(update);
			myAgent.postGuiEvent(ge);
		}
	}

	/**
	 * This is the default constructor
	 */
	public FileManagerPanel() {
		super();
		initialize();
		filesModel = new FilesTableModel();
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
		this.setSize(520, 480);
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
			jSplitPane.setDividerLocation(30);
			jSplitPane.setPreferredSize(new Dimension(520, 480));
			jSplitPane.setDividerSize(10);
			jSplitPane.setBottomComponent(getJPanel1());
			jSplitPane.setTopComponent(getJPanel());
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
			jPanel = new JPanel();
			jPanel.setLayout(new FlowLayout());
			jPanel.setPreferredSize(new Dimension(520, 30));
			jPanel.add(getJTextField(), null);
			jPanel.add(getJButton(), null);
			jPanel.add(getJButton1(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridx = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getJScrollPane(), gridBagConstraints1);
		}
		return jPanel1;
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
		return jTable;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new Dimension(200, 20));
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
			jButton.setPreferredSize(new Dimension(200, 20));
			jButton.setText("Choose file...");
			jButton.addMouseListener(new java.awt.event.MouseListener() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					JFileChooser importFile = new JFileChooser();
					importFile.setVisible(true);
					if (importFile.showOpenDialog(FileManagerPanel.this) == JFileChooser.APPROVE_OPTION) {
						File f = importFile.getSelectedFile();
						jTextField.setText(f.getPath());
					}
				}

				public void mousePressed(java.awt.event.MouseEvent e) {
				}

				public void mouseReleased(java.awt.event.MouseEvent e) {
				}

				public void mouseEntered(java.awt.event.MouseEvent e) {
				}

				public void mouseExited(java.awt.event.MouseEvent e) {
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setPreferredSize(new Dimension(90, 20));
			jButton1.setText("Import");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {

					String fileName = jTextField.getText();
					File in = new File(fileName);

					try {
						BufferedReader fis = new BufferedReader(new FileReader(
								in));
						StringBuffer data = new StringBuffer(10000);

						System.err.println("Starting reading file");

						char[] buf = new char[1024];
						int numRead = 0;
						while ((numRead = fis.read(buf)) != -1) {
							String readData = String.valueOf(buf, 0, numRead);
							data.append(readData);
							buf = new char[1024];
						}

						fis.close();

						System.err.println("Finished reading file");

						GuiEvent ge = new GuiEvent(FileManagerPanel.this,
								MainWindow.IMPORT_FILE);

						String[] names = fileName.split(Pattern.quote(System
								.getProperty("file.separator")));
						ge.addParameter(names[names.length - 1]);
						ge.addParameter(data.toString());
						myAgent.postGuiEvent(ge);
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			});
		}
		return jButton1;
	}

}
