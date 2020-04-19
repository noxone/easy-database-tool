package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.noxfire.edt.QueryResultTabPanel.ExportFormats;
import org.noxfire.edt.Table.Column;
import org.noxfire.edt.gui.ButtonTabComponent;
import org.noxfire.edt.gui.DnDTabbedPane;
import org.noxfire.edt.gui.WheelSpinner;
import org.noxfire.edt.jedit.JEditTextArea;
import org.noxfire.edt.jedit.TSQLTokenMarker;

public class MainWindow extends JFrame implements ClipboardOwner {
	private static final long serialVersionUID = 1L;

	public static final int POPUP_MENU_WAITING_TIME = 500;

	static final String OPTIONS_TREE_TABLE_DOUBLECLICK = "treeTableDoubleclick";
	static final String OPTIONS_TREE_TABLE_DOUBLECLICK_CODE = "generateCode";
	static final String OPTIONS_TREE_TABLE_DOUBLECLICK_QB = "queryBuilder";
	static final String OPTIONS_SAVE_HISTORY = "saveHistory";
	static final String OPTIONS_AUTO_SHOW_COLUMNS_LIST = "autoShowColumnList";
	static final String OPTIONS_RESULT_TABLE_FONT = "resultTableFont";
	static final String OPTIONS_RESULT_TABLE_FONT_STANDARD = "standard";
	static final String OPTIONS_RESULT_TABLE_FONT_MONOSPACE = "monospace";
	static final String OPTIONS_RESULT_TABLE_ROW_HEIGHT = "resultTableRowHeight";
	static final int OPTIONS_RESULT_TABLE_ROW_HEIGHT_STANDARD = 18;

	final EDT edt;
	private DBConnection connection;
	private List<Query> historyBackList;
	private List<Query> historyForwardList;

	private JFileChooser chooser;
	private ButtonTabComponent queryPanelTabComponent;
	private QueryTabPanel queryPanel;
	private int queryCounter = 0;

	private JPanel pnlTop;
	private JPanel pnlTopLeft;
	private JPanel pnlTopRight;
	private JSplitPane splCenter;
	private JTabbedPane tbpLeft;
	private JPanel pnlTabLeftTables;
	private JPanel pnlTabLeftFavourites;
	private JPanel pnlTabLeftFavouritesToolbar;
	private JPanel pnlTabLeftHistory;
	private JTabbedPane tbpCenter;

	// Top
	private JButton btnHistoryBack;
	private JButton btnHistoryForward;
	private JButton btnActions;
	private JButton btnRun;
	private JPanel pnlRun;
	private JPanel pnlRunConfig;
	private JPanel pnlEditor;
	private JEditTextArea txtEditor;
	// private JTextPane txtEditor;
	private JPanel pnlEditorExtern;
	private JButton btnEditorExtern;
	private JSplitPane splMain;
	private JRadioButton optRunToNewTab;
	private JRadioButton optRunToExistingTab;
	private JRadioButton optRunToCurrentTab;

	// Left
	private DefaultTreeModel mdlTables;
	private JTree treTables;
	private JToolBar tlbFavourites;
	private JButton btnAddFavFolder;
	private JButton btnAddFav;
	private JButton btnRemFavFolder;
	private JButton btnRemFav;
	private JTree treFavourites;
	private DefaultTreeModel mdlFavourites;
	private JTree treHistory;
	private DefaultTreeModel mdlHistory;

	// Suchfelder
	private JPanel pnlTableSearch;
	private JTextField txtTableSearch;
	private JButton btnTableSearch;
	private JPanel pnlFavouriteSearch;
	private JTextField txtFavouriteSearch;
	private JButton btnFavouriteSearch;
	private JPanel pnlHistorySearch;
	private JTextField txtHistorySearch;
	private JButton btnHistorySearch;

	// Bottom
	private JSplitPane splBottom;
	private JScrollPane sclErrors;
	private JList lstErrors;

	// Tree Roots
	private DefaultMutableTreeNode nodeTableRoot;
	private DefaultMutableTreeNode nodeFavRoot;
	private DefaultMutableTreeNode nodeHistRoot;

	// ContextMenu für den editor
	private JPopupMenu popEditorRightclick;
	private JMenuItem mnuCut;
	private JMenuItem mnuCopy;
	private JMenuItem mnuPaste;

	// ContextMenu für den Tabellen-Tree
	private JPopupMenu popTreeTablesRightclick;
	private JMenuItem mnuGenerateCode;
	private JMenuItem mnuOpenQueryBuilder;
	private JMenuItem mnuReadColumnsForTable;

	// Action-Button
	private JPopupMenu popAction;
	private JMenuItem mnuClear;
	private JMenuItem mnuOpen;
	private JMenuItem mnuSave;
	private JMenuItem mnuPrint;
	private JMenu mnuExport;
	private JMenuItem mnuOpenExternalWindow;
	private JMenuItem mnuShowQueryPanel;
	private JMenuItem mnuExit;
	private JMenuItem mnuRenewConnection;
	private JMenu mnuOptions;
	private JMenuItem mnuOptionsGenerateQuery;
	private JMenuItem mnuOptionsQueryBuilder;
	private JMenuItem mnuOptionsSaveHistory;
	private JMenuItem mnuOptionsAutoShowColumnList;
	private JMenuItem mnuOptionsChooseTableFontStandard;
	private JMenuItem mnuOptionsChooseTableFontMonospace;
	private JSpinner spnRowHeight;

	// Suchen-Funktion bei den Tabellen
	private DefaultMutableTreeNode nodeLastTableSearch = null;
	private DefaultMutableTreeNode nodeLastFavouritesSearch = null;
	private DefaultMutableTreeNode nodeLastHistorySearch = null;

	// externe Fenster
	private ExternalEditorWindow externEditor;
	private QueryBuilderDialog queryBuilder;

	private ActionListener actRunToNewTab = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			runCode(txtEditor.getText(), -1, false);
		}
	};
	private ActionListener actRunToExistingTab = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			runCode(txtEditor.getText(), -1, true, true);
		}
	};
	private ActionListener actRunToCurrentTab = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (tbpCenter.getTabCount() == 0
					|| tbpCenter.getTabCount() == 1 && tbpCenter.getComponentAt(0) instanceof QueryTabPanel
					|| tbpCenter.getSelectedIndex() == -1
					|| !(tbpCenter.getComponentAt(tbpCenter.getSelectedIndex()) instanceof QueryResultTabPanel))
				runCode(txtEditor.getText(), -1, false);
			else {
				runCode(txtEditor.getText(), tbpCenter.getSelectedIndex(), true);
			}
		}
	};
	private ActionListener actTreTablesRightclick = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = treTables.getSelectionPath();
			if (path != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
				if (node.getUserObject() instanceof Table) {
					if (e.getSource() == mnuGenerateCode) {
						setCodeEditorText(((Table)node.getUserObject()).getQueryString());
					} else if (e.getSource() == mnuOpenQueryBuilder) {
						showQueryBuilderForTable((Table)node.getUserObject());
					} else if (e.getSource() == mnuReadColumnsForTable) {
						readColumnsForTable((Table)node.getUserObject());
					}
				}
			}
		}
	};

	MainWindow(final EDT edt, final DBConnection connection) {
		this.edt = edt;
		this.connection = connection;
		historyBackList = new Vector<Query>();
		historyForwardList = new Vector<Query>();

		setIconImage(Toolkit.getDefaultToolkit().getImage("gfx/logo.gif"));
		setTitle(EDT.APPLICATION_TITLE + " - " + connection.getUsername() + "@" + connection.getDatabase().database);
		setPreferredSize(new Dimension(800, 600));

		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);

		btnHistoryBack = new JButton(IconFactory.getIcon("back"));
		btnHistoryForward = new JButton(IconFactory.getIcon("forward"));
		btnActions = new JButton(IconFactory.getIcon("actions"));
		btnRun = new JButton(IconFactory.getIcon("run"));
		btnHistoryBack.setEnabled(false);
		btnHistoryForward.setEnabled(false);
		btnHistoryBack.setToolTipText("Back");
		btnHistoryForward.setToolTipText("Forward");
		btnActions.setToolTipText("What do you want to do today?");
		btnRun.setToolTipText("<html>Run <i>(F5)</i></html>");
		btnHistoryBack.setPreferredSize(new Dimension(75, 75));
		btnHistoryForward.setPreferredSize(new Dimension(75, 75));
		btnActions.setPreferredSize(new Dimension(75, 75));
		btnRun.setPreferredSize(new Dimension(75, 75));
		// btnRun.setPreferredSize(new Dimension(75 +
		// btnRun.getArrowButton().getPreferredSize().width, 75));
		pnlRun = new JPanel();
		pnlRun.setLayout(new BorderLayout());
		pnlRunConfig = new JPanel();
		pnlRunConfig.setLayout(new GridLayout(3, 1));
		pnlRun.add(btnRun, BorderLayout.WEST);
		pnlRun.add(pnlRunConfig, BorderLayout.EAST);
		ButtonGroup group = new ButtonGroup();
		optRunToNewTab = new JRadioButton("new tab");
		optRunToCurrentTab = new JRadioButton("current tab");
		optRunToExistingTab = new JRadioButton("exisiting tab");
		optRunToNewTab.setToolTipText("Run code to a new tab");
		optRunToExistingTab.setToolTipText("Run code to an existing tab which contains the same tables");
		optRunToCurrentTab.setToolTipText("Run code into current tab");
		group.add(optRunToNewTab);
		group.add(optRunToCurrentTab);
		group.add(optRunToExistingTab);
		pnlRunConfig.add(optRunToNewTab);
		pnlRunConfig.add(optRunToExistingTab);
		pnlRunConfig.add(optRunToCurrentTab);
		optRunToNewTab.setSelected(true);

		pnlTopLeft = new JPanel();
		pnlTopLeft.add(btnHistoryBack);
		pnlTopLeft.add(btnHistoryForward);
		pnlTopLeft.add(btnActions);
		pnlTopRight = new JPanel();
		pnlTopRight.add(pnlRun);
		txtEditor = new JEditTextArea(this);
		txtEditor.setTokenMarker(new TSQLTokenMarker());
		txtEditor.setAutoscrolls(true);
		txtEditor.setCaretBlinkEnabled(true);
		txtEditor.setPreferredSize(new Dimension(75, 75));
		pnlEditor = new JPanel();
		pnlEditor.setLayout(new BorderLayout());
		pnlEditorExtern = new JPanel();
		pnlEditorExtern.setLayout(new BorderLayout());
		btnEditorExtern = new JButton(IconFactory.getIcon("external window"));
		btnEditorExtern.setPreferredSize(new Dimension(16, 16));
		btnEditorExtern.setToolTipText("Open editor in an external window...");
		pnlEditorExtern.add(btnEditorExtern, BorderLayout.NORTH);
		pnlEditor.add(pnlEditorExtern, BorderLayout.EAST);
		JScrollPane sp = new JScrollPane(txtEditor);
		sp.setPreferredSize(new Dimension(75, 75));
		pnlEditor.add(sp, BorderLayout.CENTER);
		pnlTop = new JPanel();
		pnlTop.setLayout(new BorderLayout());
		pnlTop.add(pnlTopLeft, BorderLayout.WEST);
		pnlTop.add(pnlTopRight, BorderLayout.EAST);
		pnlTop.add(pnlEditor);

		// Left Panels
		tlbFavourites = new JToolBar("Favourites");
		btnAddFav = new JButton(IconFactory.getIcon("addfav"));
		btnAddFavFolder = new JButton(IconFactory.getIcon("addfavfol"));
		btnRemFav = new JButton(IconFactory.getIcon("remfav"));
		btnRemFavFolder = new JButton(IconFactory.getIcon("remfavfol"));
		btnAddFav.setToolTipText("Add query to favourites...");
		btnRemFav.setToolTipText("Remove query from favourites...");
		btnAddFavFolder.setToolTipText("Create a new query folder...");
		btnRemFavFolder.setToolTipText("Remove current query folder...");
		tlbFavourites.add(btnAddFav);
		tlbFavourites.add(btnRemFav);
		tlbFavourites.add(btnAddFavFolder);
		tlbFavourites.add(btnRemFavFolder);

		// Treenodes
		nodeTableRoot = new DefaultMutableTreeNode("Tables");
		nodeFavRoot = new DefaultMutableTreeNode("Favourites");
		nodeHistRoot = new DefaultMutableTreeNode("History");
		// TreeViews
		treTables = new JTree();
		treFavourites = new JTree();
		treHistory = new JTree();
		tbpLeft = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		// tbpCenter = new JTabbedPane(JTabbedPane.TOP,
		// JTabbedPane.SCROLL_TAB_LAYOUT);
		tbpCenter = new DnDTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		tbpCenter.addContainerListener(new ContainerAdapter() {
			@Override
			public void componentRemoved(ContainerEvent e) {
				super.componentRemoved(e);
				if (e.getChild() instanceof QueryResultTabPanel) {
					QueryResultTabPanel qrp = (QueryResultTabPanel)e.getChild();
					qrp.closeResultSet();
					System.runFinalization();
					System.gc();
				}
			}
		});
		pnlTabLeftTables = new JPanel();
		pnlTabLeftTables.setLayout(new BorderLayout());
		pnlTabLeftTables.add(new JScrollPane(treTables), BorderLayout.CENTER);
		pnlTableSearch = new JPanel();
		pnlTableSearch.setLayout(new BorderLayout());
		txtTableSearch = new JTextField();
		btnTableSearch = new JButton("Next");
		pnlTableSearch.add(txtTableSearch, BorderLayout.CENTER);
		pnlTableSearch.add(btnTableSearch, BorderLayout.EAST);
		pnlTabLeftTables.add(pnlTableSearch, BorderLayout.NORTH);
		pnlTabLeftFavourites = new JPanel();
		pnlTabLeftFavourites.setLayout(new BorderLayout());
		pnlTabLeftFavourites.add(new JScrollPane(treFavourites), BorderLayout.CENTER);
		pnlTabLeftFavouritesToolbar = new JPanel();
		pnlTabLeftFavouritesToolbar.setLayout(new BorderLayout());
		pnlTabLeftFavouritesToolbar.add(pnlTabLeftFavourites, BorderLayout.CENTER);
		pnlTabLeftFavouritesToolbar.add(tlbFavourites, BorderLayout.PAGE_START);
		pnlFavouriteSearch = new JPanel();
		pnlFavouriteSearch.setLayout(new BorderLayout());
		txtFavouriteSearch = new JTextField();
		btnFavouriteSearch = new JButton("Next");
		pnlFavouriteSearch.add(txtFavouriteSearch, BorderLayout.CENTER);
		pnlFavouriteSearch.add(btnFavouriteSearch, BorderLayout.EAST);
		pnlTabLeftFavourites.add(pnlFavouriteSearch, BorderLayout.NORTH);
		pnlTabLeftHistory = new JPanel();
		pnlTabLeftHistory.setLayout(new BorderLayout());
		pnlTabLeftHistory.add(new JScrollPane(treHistory), BorderLayout.CENTER);
		pnlHistorySearch = new JPanel();
		pnlHistorySearch.setLayout(new BorderLayout());
		txtHistorySearch = new JTextField();
		btnHistorySearch = new JButton("Next");
		pnlHistorySearch.add(txtHistorySearch, BorderLayout.CENTER);
		pnlHistorySearch.add(btnHistorySearch, BorderLayout.EAST);
		pnlTabLeftHistory.add(pnlHistorySearch, BorderLayout.NORTH);
		tbpLeft.addTab("Tables", pnlTabLeftTables);
		tbpLeft.addTab("Favourites", pnlTabLeftFavouritesToolbar);
		tbpLeft.addTab("History", pnlTabLeftHistory);
		tbpLeft.setPreferredSize(new Dimension(200, 1000));
		splCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tbpLeft, tbpCenter);
		splCenter.setContinuousLayout(true);
		splCenter.setOneTouchExpandable(true);
		splCenter.setDividerSize(7);

		// Bottom
		lstErrors = new JList();
		sclErrors = new JScrollPane(lstErrors);
		sclErrors.setPreferredSize(new Dimension(200, 100));
		splBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splCenter, sclErrors);
		splBottom.setContinuousLayout(true);
		splBottom.setOneTouchExpandable(true);
		splBottom.setResizeWeight(1.0);

		// kram einfügen
		// add(pnlTop, BorderLayout.NORTH);
		// add(splCenter, BorderLayout.CENTER);
		// add(splBottom, BorderLayout.CENTER);
		pnlTop.setMinimumSize(new Dimension(85, 85));
		splMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlTop, splCenter);
		splMain.setContinuousLayout(true);
		add(splMain, BorderLayout.CENTER);

		// TreeViews füllen
		List<Table> tables = connection.getTables();
		for (Table table : tables)
			nodeTableRoot.add(table.getTreeNode());
		mdlTables = new DefaultTreeModel(nodeTableRoot);
		treTables.setModel(mdlTables);
		treTables.addMouseListener(new TreeSelectionHandler(treTables));
		treTables.setCellRenderer(new EDTTreeCellRenderer());
		treTables.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
				if (e.getPath().getLastPathComponent() == treTables.getModel().getRoot())
					throw new ExpandVetoException(e);
			}

			@Override
			public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {}
		});

		List<QueryFolder> folders = edt.getFavourites();
		for (QueryFolder folder : folders)
			nodeFavRoot.add(folder.getTreeNode());
		mdlFavourites = new DefaultTreeModel(nodeFavRoot);
		treFavourites.setModel(mdlFavourites);
		treFavourites.addMouseListener(new TreeSelectionHandler(treFavourites));
		treFavourites.setCellRenderer(new EDTTreeCellRenderer());
		treFavourites.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
				if (event.getPath().getLastPathComponent() == nodeFavRoot)
					throw new ExpandVetoException(event);
			}

			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {}
		});

		folders = edt.getHistory();
		for (QueryFolder folder : folders)
			nodeHistRoot.add(folder.getTreeNode());
		mdlHistory = new DefaultTreeModel(nodeHistRoot);
		treHistory.setModel(mdlHistory);
		treHistory.addMouseListener(new TreeSelectionHandler(treHistory));
		treHistory.setCellRenderer(new EDTTreeCellRenderer());
		treHistory.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
				if (event.getPath().getLastPathComponent() == nodeHistRoot)
					throw new ExpandVetoException(event);
			}

			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {}
		});

		// Tooltips im Baum anzeigen
		ToolTipManager.sharedInstance().registerComponent(treFavourites);
		ToolTipManager.sharedInstance().registerComponent(treHistory);

		// Action-Button-Menü einrichten
		popAction = new JPopupMenu();
		mnuClear = new JMenuItem("Clear");
		mnuOpen = new JMenuItem("Open...", IconFactory.getIcon("folder open"));
		mnuSave = new JMenuItem("Save...", IconFactory.getIcon("save"));
		mnuPrint = new JMenuItem("Print...");
		mnuExport = new JMenu("Export");
		for (ExportFormats format : ExportFormats.values()) {
			JMenuItem item = new JMenuItem("Export as " + format.description, format.icon);
			mnuExport.add(item);
			item.addActionListener(new ExportHandler(format));
		}
		mnuShowQueryPanel = new JMenuItem("Show query panel", IconFactory.getIcon("query"));
		mnuOpenExternalWindow = new JMenuItem("Open external editor window...");
		mnuExit = new JMenuItem("Exit");
		mnuRenewConnection = new JMenuItem("Restart database connection");
		mnuOptions = new JMenu("Options");
		group = new ButtonGroup();
		mnuOptionsGenerateQuery = new JRadioButtonMenuItem("Generate simple query");
		mnuOptionsQueryBuilder = new JRadioButtonMenuItem("Open query builder");
		group.add(mnuOptionsGenerateQuery);
		group.add(mnuOptionsQueryBuilder);
		mnuOptionsSaveHistory = new JCheckBoxMenuItem("Save history");
		mnuOptionsAutoShowColumnList = new JCheckBoxMenuItem("Show column list by default");
		group = new ButtonGroup();
		mnuOptionsChooseTableFontStandard = new JRadioButtonMenuItem("Standard font");
		mnuOptionsChooseTableFontMonospace = new JRadioButtonMenuItem("Monospace font");
		group.add(mnuOptionsChooseTableFontStandard);
		group.add(mnuOptionsChooseTableFontMonospace);
		// Extrawurst für die Zeilenhöhe
		JPanel pnlOptionsRowHeight = new JPanel();
		pnlOptionsRowHeight.setBackground(mnuOptions.getBackground());
		pnlOptionsRowHeight.setLayout(new BorderLayout());
		int rowHeight = edt.getIntOption(OPTIONS_RESULT_TABLE_ROW_HEIGHT, OPTIONS_RESULT_TABLE_ROW_HEIGHT_STANDARD);
		spnRowHeight = new WheelSpinner(rowHeight, 5, 100);
		pnlOptionsRowHeight.add(new JLabel(" Row height/ font size: "), BorderLayout.WEST);
		pnlOptionsRowHeight.add(spnRowHeight, BorderLayout.CENTER);
		// Optionen
		mnuOptions.add(Utils.createMenuLabel("Result table settings", mnuOptions.getBackground()));
		mnuOptions.add(mnuOptionsChooseTableFontStandard);
		mnuOptions.add(mnuOptionsChooseTableFontMonospace);
		mnuOptions.add(pnlOptionsRowHeight);
		mnuOptions.addSeparator();
		mnuOptions.add(Utils.createMenuLabel("On table treeview doubleclick", mnuOptions.getBackground()));
		mnuOptions.add(mnuOptionsGenerateQuery);
		mnuOptions.add(mnuOptionsQueryBuilder);
		mnuOptions.addSeparator();
		mnuOptions.add(Utils.createMenuLabel("History", mnuOptions.getBackground()));
		mnuOptions.add(mnuOptionsSaveHistory);
		mnuOptions.addSeparator();
		mnuOptions.add(Utils.createMenuLabel("Query result tab", mnuOptions.getBackground()));
		mnuOptions.add(mnuOptionsAutoShowColumnList);
		popAction.add(mnuClear);
		popAction.add(mnuOpen);
		popAction.add(mnuSave);
		popAction.addSeparator();
		popAction.add(mnuOpenExternalWindow);
		popAction.add(mnuShowQueryPanel);
		popAction.addSeparator();
		popAction.add(mnuOptions);
		popAction.add(mnuRenewConnection);
		popAction.addSeparator();
		popAction.add(mnuPrint);
		popAction.add(mnuExport);
		popAction.addSeparator();
		popAction.add(mnuExit);

		mnuRenewConnection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renewDatabaseConnection();
			}
		});
		mnuPrint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (tbpCenter.getSelectedIndex() != -1
						&& tbpCenter.getSelectedComponent() instanceof QueryResultTabPanel)
					((QueryResultTabPanel)tbpCenter.getSelectedComponent()).print();
			}
		});
		mnuOptionsGenerateQuery.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edt.getOptions().put(OPTIONS_TREE_TABLE_DOUBLECLICK, OPTIONS_TREE_TABLE_DOUBLECLICK_CODE);
			}
		});
		mnuOptionsQueryBuilder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edt.getOptions().put(OPTIONS_TREE_TABLE_DOUBLECLICK, OPTIONS_TREE_TABLE_DOUBLECLICK_QB);
			}
		});
		mnuOptionsSaveHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edt.getOptions().put(OPTIONS_SAVE_HISTORY, Boolean.toString(mnuOptionsSaveHistory.isSelected()));
			}
		});
		mnuOptionsAutoShowColumnList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edt.getOptions().put(OPTIONS_AUTO_SHOW_COLUMNS_LIST,
						Boolean.toString(mnuOptionsAutoShowColumnList.isSelected()));
			}
		});
		mnuOptionsChooseTableFontStandard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edt.getOptions().put(OPTIONS_RESULT_TABLE_FONT, OPTIONS_RESULT_TABLE_FONT_STANDARD);
				applyCurrentFont();
			}
		});
		mnuOptionsChooseTableFontMonospace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edt.getOptions().put(OPTIONS_RESULT_TABLE_FONT, OPTIONS_RESULT_TABLE_FONT_MONOSPACE);
				applyCurrentFont();
			}
		});
		spnRowHeight.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int newRowHeight = (Integer)spnRowHeight.getValue();
				edt.getOptions().put(OPTIONS_RESULT_TABLE_ROW_HEIGHT, Integer.toString(newRowHeight));
				applyCurrentRowHeight();
			}
		});

		mnuExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnuClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(MainWindow.this, "Do you really want to clear the code editor?",
						EDT.APPLICATION_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
					setCodeEditorText("");
			}
		});
		mnuOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new FileReader(chooser.getSelectedFile()));
						String file = "";
						String line;
						while ((line = reader.readLine()) != null)
							file += (file.length() > 0 ? "\n" : "") + line;
						setCodeEditorText(file);
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(MainWindow.this, "Cannot load the file!", EDT.APPLICATION_TITLE,
								JOptionPane.ERROR_MESSAGE);
					}
					finally {
						try {
							reader.close();
						}
						catch (Exception ex) {}
					}
				}
			}
		});
		mnuSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
					PrintWriter writer = null;
					try {
						writer = new PrintWriter(chooser.getSelectedFile());
						writer.write(txtEditor.getText());
					}
					catch (Exception ex) {
						JOptionPane.showMessageDialog(MainWindow.this, "Cannot save to this file!",
								EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
					}
					finally {
						try {
							writer.close();
						}
						catch (Exception ex) {}
					}
				}
			}
		});
		mnuExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(MainWindow.this, "Print is not implemented yet!");
			}
		});
		mnuShowQueryPanel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showQueryTabPanel();
			}
		});

		// PopupMenu für treTables einrichten
		popTreeTablesRightclick = new JPopupMenu();
		mnuGenerateCode = new JMenuItem("Generate code for this table");
		mnuOpenQueryBuilder = new JMenuItem("Open query builder for this table...");
		mnuReadColumnsForTable = new JMenuItem("Read columns for this table");
		popTreeTablesRightclick.add(mnuGenerateCode);
		popTreeTablesRightclick.add(mnuOpenQueryBuilder);
		popTreeTablesRightclick.addSeparator();
		popTreeTablesRightclick.add(mnuReadColumnsForTable);
		mnuGenerateCode.addActionListener(actTreTablesRightclick);
		mnuOpenQueryBuilder.addActionListener(actTreTablesRightclick);
		mnuReadColumnsForTable.addActionListener(actTreTablesRightclick);

		// PopupMenu einrichten
		popEditorRightclick = new JPopupMenu();
		mnuCut = new JMenuItem("Cut", IconFactory.getIcon("cut"));
		mnuCopy = new JMenuItem("Copy", IconFactory.getIcon("copy"));
		mnuPaste = new JMenuItem("Paste", IconFactory.getIcon("paste"));
		mnuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		mnuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		mnuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		popEditorRightclick.add(mnuCut);
		popEditorRightclick.add(mnuCopy);
		popEditorRightclick.add(mnuPaste);

		mnuCut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cut(txtEditor);
			}
		});
		mnuCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copy(txtEditor);
			}
		});
		mnuPaste.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paste(txtEditor);
			}
		});
		txtEditor.setComponentPopupMenu(popEditorRightclick);

		// Buttons belegen
		btnRun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireRunButtonAction(e);
			}
		});
		btnRun.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_F5)
					fireRunButtonAction(new ActionEvent(txtEditor, e.getID(), e.paramString()));
			}
		});
		btnActions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mnuExport.setEnabled(tbpCenter.getSelectedIndex() != -1
						&& tbpCenter.getSelectedComponent() instanceof QueryResultTabPanel);
				mnuPrint.setEnabled(mnuExport.isEnabled());
				popAction.show(btnActions, btnActions.getMousePosition().x, btnActions.getMousePosition().y);
			}
		});
		btnHistoryBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (btnHistoryBack) {
					historyGoBack(1);
				}
			}
		});
		btnHistoryBack.addMouseListener(new DelayedPopupMenuMouseListener(btnHistoryBack, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (btnHistoryBack) {
					JPopupMenu popBack = new JPopupMenu();
					for (int i = historyBackList.size() - 1; i >= 0 && i >= historyBackList.size() - 20; --i) {
						JMenuItem item = new JMenuItem(historyBackList.get(i).getQueryString());
						popBack.add(item);
						item.addActionListener(new SaveIntActionListener(historyBackList.size() - i) {
							@Override
							public void actionPerformed(ActionEvent e) {
								historyGoBack(amount);
							}
						});
					}
					try {
						popBack.show(btnHistoryBack, btnHistoryBack.getMousePosition().x,
								btnHistoryBack.getMousePosition().y);
					}
					catch (NullPointerException e2) {}
				}
			}
		}));
		btnHistoryForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (btnHistoryForward) {
					historyGoForward(1);
				}
			}
		});
		btnHistoryForward.addMouseListener(new DelayedPopupMenuMouseListener(btnHistoryForward, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (btnHistoryForward) {
					JPopupMenu popForward = new JPopupMenu();
					for (int i = historyForwardList.size() - 1; i >= 0 && i >= historyForwardList.size() - 20; --i) {
						JMenuItem item = new JMenuItem(historyForwardList.get(i).getQueryString());
						popForward.add(item);
						item.addActionListener(new SaveIntActionListener(historyForwardList.size() - i) {
							@Override
							public void actionPerformed(ActionEvent e) {
								historyGoForward(amount);
							}
						});
					}
					popForward.show(btnHistoryForward, btnHistoryForward.getMousePosition().x,
							btnHistoryForward.getMousePosition().y);
				}
			}
		}));
		btnAddFavFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String folderName = JOptionPane.showInputDialog(MainWindow.this,
						"Please enter the name of the new folder:");

				if (folderName != null && folderName.trim().length() != 0) {
					QueryFolder folder = new QueryFolder(folderName);
					edt.getFavourites().add(folder);

					if (nodeFavRoot.getChildCount() > 0) {
						boolean added = false;
						// index suchen, damit das ganze auch Vergleichbar wird
						// :)
						for (int i = 0; i < nodeFavRoot.getChildCount(); ++i) {
							if (folderName.compareToIgnoreCase(((QueryFolder)((DefaultMutableTreeNode)nodeFavRoot
									.getChildAt(i)).getUserObject()).getName()) < 0) {
								addFolder(folder, i);
								added = true;
								break;
							}
						}
						if (!added) {
							addFolder(folder, nodeFavRoot.getChildCount());
						}
					} else {
						addFolder(folder, 0);
					}
				}
			}

			private void addFolder(QueryFolder folder, int index) {
				mdlFavourites.insertNodeInto(folder.getTreeNode(), nodeFavRoot, index);
				treFavourites.scrollPathToVisible(new TreePath(folder.getTreeNode().getPath()));
			}
		});
		btnRemFavFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath tp = treFavourites.getSelectionPath();
				if (tp != null) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
					if (node.getUserObject() instanceof QueryFolder) {
						QueryFolder folder = (QueryFolder)node.getUserObject();
						if (JOptionPane.showConfirmDialog(MainWindow.this, "Do you really want to delete the folder '"
								+ folder.getName() + "'?", EDT.APPLICATION_TITLE, JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, IconFactory.getIcon("folder big")) == JOptionPane.YES_OPTION) {
							edt.getFavourites().remove(folder);
							mdlFavourites.removeNodeFromParent(folder.getTreeNode());
						}
					}
				}
			}
		});
		btnRemFav.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath tp = treFavourites.getSelectionPath();
				if (tp != null) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
					if (node.getUserObject() instanceof Query) {
						Query query = (Query)node.getUserObject();
						if (JOptionPane.showConfirmDialog(MainWindow.this, "Do you really want to delete the query '"
								+ query.getName() + "'?", EDT.APPLICATION_TITLE, JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, IconFactory.getIcon("query big")) == JOptionPane.YES_OPTION) {
							QueryFolder folder = (QueryFolder)((DefaultMutableTreeNode)tp.getPathComponent(tp
									.getPathCount() - 2)).getUserObject();
							mdlFavourites.removeNodeFromParent(query.getTreeNode());
							folder.removeQuery(query);
						}
					}
				}
			}
		});
		btnAddFav.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddFavouriteDialog afd = new AddFavouriteDialog(MainWindow.this, edt, txtEditor.getText(),
						btnAddFavFolder.getActionListeners(), mdlFavourites);
				afd.setVisible(true);
			}
		});
		btnEditorExtern.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openExternalEditorWindow();
			}
		});
		mnuOpenExternalWindow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openExternalEditorWindow();
			}
		});

		// Suchen-Funktion
		txtTableSearch.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				super.insertString(offs, str, a);
				doTableSearch(true);
			}
		});
		btnTableSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doTableSearch(false);
			}
		});
		txtFavouriteSearch.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				super.insertString(offs, str, a);
				doFavouriteSearch(true);
			}
		});
		btnFavouriteSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFavouriteSearch(false);
			}
		});
		txtHistorySearch.setDocument(new PlainDocument() {
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				super.insertString(offs, str, a);
				doHistorySearch(true);
			}
		});
		btnHistorySearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doHistorySearch(false);
			}
		});

		// externen Editor aufbauen
		externEditor = new ExternalEditorWindow(this);
		queryBuilder = new QueryBuilderDialog(this);

		// Panel für sonstige Query-Results aufbauen
		queryPanelTabComponent = new ButtonTabComponent(tbpCenter, false);
		queryPanelTabComponent.setIcon(IconFactory.getIcon("query"));
		queryPanel = new QueryTabPanel(this, queryPanelTabComponent);
		// showQueryTabPanel();

		initOptions();

		// done
		pack();
		txtEditor.requestFocus();
		// splBottom.setDividerLocation(splBottom.getHeight());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
	}

	private void initOptions() {
		String s;

		// Save History
		mnuOptionsSaveHistory.setSelected(edt.saveHistory());

		// Spalten-liste anzeigen
		mnuOptionsAutoShowColumnList.setSelected(edt.showColumnsList());

		// Doppelklick-Action auf treTables
		s = edt.getOptions().get(OPTIONS_TREE_TABLE_DOUBLECLICK);
		mnuOptionsGenerateQuery.setSelected(s != null && s.equalsIgnoreCase(OPTIONS_TREE_TABLE_DOUBLECLICK_CODE));
		mnuOptionsQueryBuilder.setSelected(!mnuOptionsGenerateQuery.isSelected());

		// Schriftart
		s = edt.getOptions().get(OPTIONS_RESULT_TABLE_FONT);
		mnuOptionsChooseTableFontStandard.setSelected(s == null
				|| s.equalsIgnoreCase(OPTIONS_RESULT_TABLE_FONT_STANDARD));
		mnuOptionsChooseTableFontMonospace.setSelected(!mnuOptionsChooseTableFontStandard.isSelected());
	}

	public void renewDatabaseConnection() {
		if (connection.renewConnection()) {
			// DO nothing... just be silent!
//			JOptionPane.showMessageDialog(MainWindow.this, "New database connection successfully created!",
//					EDT.APPLICATION_TITLE, JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(MainWindow.this,
					"EDT was not able to renew the database connection. The application will quit now!",
					EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	private void applyCurrentFont() {
		Font font = getCurrentFont();
		for (int i = 0; i < tbpCenter.getTabCount(); ++i) {
			Component cmp = tbpCenter.getComponentAt(i);
			if (cmp instanceof QueryResultTabPanel)
				((QueryResultTabPanel)cmp).setTableFont(font);
		}

	}

	private void applyCurrentRowHeight() {
		// durch die tabs gehen und die RowHeight verändern...
		for (int i = 0; i < tbpCenter.getTabCount(); ++i) {
			Component cmp = tbpCenter.getComponentAt(i);
			if (cmp instanceof QueryResultTabPanel)
				((QueryResultTabPanel)cmp).setTableRowHeight(edt.getIntOption(OPTIONS_RESULT_TABLE_ROW_HEIGHT,
						OPTIONS_RESULT_TABLE_ROW_HEIGHT_STANDARD));
		}
		applyCurrentFont();
	}

	Font getCurrentFont() {
		String option = edt.getStringOption(OPTIONS_RESULT_TABLE_FONT);
		if (option != null && option.equalsIgnoreCase(OPTIONS_RESULT_TABLE_FONT_MONOSPACE))
			// return new Font(Font.MONOSPACED, Font.PLAIN,
			// getFont().getSize());
			return new Font(Font.MONOSPACED, Font.PLAIN, edt.getIntOption(OPTIONS_RESULT_TABLE_ROW_HEIGHT,
					OPTIONS_RESULT_TABLE_ROW_HEIGHT_STANDARD) * 2 / 3);
		else
			// return new Font(Font.SANS_SERIF, Font.PLAIN,
			// getFont().getSize());
			return new Font(Font.SANS_SERIF, Font.PLAIN, edt.getIntOption(OPTIONS_RESULT_TABLE_ROW_HEIGHT,
					OPTIONS_RESULT_TABLE_ROW_HEIGHT_STANDARD) * 2 / 3);
	}

	private void doTableSearch(boolean clearOldNode) {
		if (clearOldNode)
			nodeLastTableSearch = null;
		DefaultMutableTreeNode node = Utils.findExecutable(connection.getTables(), nodeLastTableSearch,
				txtTableSearch.getText());

		nodeLastTableSearch = node;
		if (node != null) {
			treTables.scrollPathToVisible(new TreePath(node.getPath()));
			treTables.setSelectionPath(new TreePath(node.getPath()));
		} else {
			treTables.setSelectionPath(null);
		}
	}

	private void doFavouriteSearch(boolean clearOldNode) {
		if (clearOldNode)
			nodeLastFavouritesSearch = null;
		DefaultMutableTreeNode node = Utils.findExecutable(edt.getFavouriteList(), nodeLastFavouritesSearch,
				txtFavouriteSearch.getText());

		nodeLastFavouritesSearch = node;
		if (node != null) {
			treFavourites.scrollPathToVisible(new TreePath(node.getPath()));
			treFavourites.setSelectionPath(new TreePath(node.getPath()));
		} else {
			treFavourites.setSelectionPath(null);
		}
	}

	private void doHistorySearch(boolean clearOldNode) {
		if (clearOldNode)
			nodeLastHistorySearch = null;
		DefaultMutableTreeNode node = Utils.findExecutable(edt.getHistoryList(), nodeLastHistorySearch,
				txtHistorySearch.getText());

		nodeLastHistorySearch = node;
		if (node != null) {
			treHistory.scrollPathToVisible(new TreePath(node.getPath()));
			treHistory.setSelectionPath(new TreePath(node.getPath()));
		} else {
			treHistory.setSelectionPath(null);
		}
	}

	private void enableHistoryButtons() {
		btnHistoryBack.setEnabled(historyBackList.size() > 0);
		btnHistoryForward.setEnabled(historyForwardList.size() > 0);
		btnRun.requestFocus();
	}

	private void addHistoryBack(String query) {
		if (historyBackList.size() == 0 || !historyBackList.get(historyBackList.size() - 1).isSameSql(query))
			historyBackList.add(new Query(query, query));
	}

	private void addHistoryForward(String query) {
		if (historyForwardList.size() == 0 || !historyForwardList.get(historyForwardList.size() - 1).isSameSql(query))
			historyForwardList.add(new Query(query, query));
	}

	private void historyGoBack(int amount) {
		for (int i = 0; i < amount; ++i) {
			if (historyBackList.size() > 0) {
				addHistoryForward(txtEditor.getText());
				txtEditor.setText(historyBackList.remove(historyBackList.size() - 1).getQueryString());
				txtEditor.requestFocus();
			}
		}
		enableHistoryButtons();
	}

	private void historyGoForward(int amount) {
		for (int i = 0; i < amount; ++i) {

			if (historyForwardList.size() > 0) {
				addHistoryBack(txtEditor.getText());
				txtEditor.setText(historyForwardList.remove(historyForwardList.size() - 1).getQueryString());
				txtEditor.requestFocus();
			}
		}
		enableHistoryButtons();
	}

	public void setCodeEditorText(String text) {
		String oldText = txtEditor.getText();
		if (oldText.trim().length() > 0) {
			// history
			historyForwardList.clear();
			addHistoryBack(oldText);
		}
		txtEditor.setText(text);
		txtEditor.requestFocus();
		enableHistoryButtons();
	}

	public void addWhereClause(String clause) {
		SqlStatement stmt = new SqlStatement(txtEditor.getText());
		String code = stmt.getSelect();
		if (stmt.getWhere() != null)
			code += stmt.getWhere() + "\n  AND " + clause + "\n";
		else
			code += "WHERE " + clause + "\n";
		code += stmt.getGroupByPrepared() + stmt.getHavingPrepared() + stmt.getOrderByPrepared();
		setCodeEditorText(code);
	}

	public void addOrderByClause(String clause) {
		SqlStatement stmt = new SqlStatement(txtEditor.getText());
		String code = stmt.getSelect() + stmt.getWherePrepared() + stmt.getGroupByPrepared() + stmt.getHavingPrepared();
		if (stmt.getOrderBy() != null)
			code += stmt.getOrderBy() + ",\n         " + clause + "\n";
		else
			code += "ORDER BY " + clause + "\n";
		setCodeEditorText(code);
	}

	public void addHavingClause(String clause) {
		SqlStatement stmt = new SqlStatement(txtEditor.getText());
		String code = stmt.getSelect() + stmt.getWherePrepared() + stmt.getGroupByPrepared();
		if (stmt.getHaving() != null)
			code += stmt.getHaving() + "\n   AND " + clause + "\n";
		else
			code += "HAVING " + clause + "\n";
		code += stmt.getOrderByPrepared();
		setCodeEditorText(code);
	}

	public void addGroupByClause(String clause) {
		SqlStatement stmt = new SqlStatement(txtEditor.getText());
		String code = stmt.getSelect() + stmt.getWherePrepared();
		if (stmt.getGroupBy() != null)
			code += stmt.getGroupBy() + ",\n         " + clause + "\n";
		else
			code += "GROUP BY " + clause + "\n";
		code += stmt.getHavingPrepared() + stmt.getOrderByPrepared();
		setCodeEditorText(code);
	}

	public void closeOtherTabs(int index) {
		for (int i = tbpCenter.getTabCount() - 1; i >= 0; --i)
			if (i != index)
				closeTab(i);
	}

	public void closeTab(int index) {
		if (index != -1)
			tbpCenter.remove(index);
	}

	public void closeTab(QueryResultTabPanel panel) {
		closeTab(tbpCenter.indexOfComponent(panel));
	}

	public void fireRunButtonAction(Object source) {
		fireRunButtonAction(new ActionEvent(source, 0, "click"));
	}

	public void fireRunButtonAction(ActionEvent e) {
		if (optRunToNewTab.isSelected())
			actRunToNewTab.actionPerformed(e);
		else if (optRunToExistingTab.isSelected())
			actRunToExistingTab.actionPerformed(e);
		else if (optRunToCurrentTab.isSelected())
			actRunToCurrentTab.actionPerformed(e);
		else
			JOptionPane
					.showMessageDialog(
							this,
							"Please select, where to display the result of the query by checking one of the radio buttons next to the run button!",
							EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
	}

	public void runCode(String code, int tabIndex, boolean renewGivenTab) {
		runCode(code, tabIndex, renewGivenTab, false);
	}

	public void runCode(String code, int tabIndex, boolean renewGivenTab, boolean replaceExsitingTable) {
		String codeToRun = "";
		{
			String[] z = code.split("[\\n]+");
			Vector<String> zeilen = new Vector<String>();
			boolean done = false;
			for (int i = 0; i < z.length; ++i) {
				String n = z[i].trim();
				if (done || n.length() > 0 && !n.startsWith("--")) {
					zeilen.add(n);
					done = true;
				}
			}
			for (int i = 0; i < zeilen.size(); ++i)
				codeToRun += zeilen.get(i) + "\n";
		}

		if (code.trim().length() == 0) {
			JOptionPane.showMessageDialog(MainWindow.this, "Please enter a query!", EDT.APPLICATION_TITLE,
					JOptionPane.INFORMATION_MESSAGE);
			txtEditor.requestFocus();
		} else {
			final String titleDivider = ": ";
			if (codeToRun.trim().toLowerCase().startsWith("select")) {
				// es ist ein SELECT!!
				QueryResult result = connection.executeSelect(MainWindow.this, codeToRun, code);
				if (result != null) {
					Query query = new Query(txtEditor.getText(), code);
					if (tabIndex < 0 && !renewGivenTab || replaceExsitingTable) {
						++queryCounter;

						int index = -1;
						ButtonTabComponent tabC = new ButtonTabComponent(tbpCenter, true);
						QueryResultTabPanel qrp = new QueryResultTabPanel(MainWindow.this, result, tabC, connection);
						tabC.addActionListenerToReloadButton(new TabReloader(qrp));
						if (renewGivenTab) {
							if (!replaceExsitingTable)
								index = tabIndex;
							else {
								String title = tbpCenter.getTitleAt(tbpCenter.getSelectedIndex());
								for (; Character.isDigit(title.charAt(0)); title = title.substring(1));
								title = title.substring(titleDivider.length());
								if (title.equalsIgnoreCase(qrp.getTableName()))
									index = tbpCenter.getSelectedIndex();
								else {
									for (int i = 0; i < tbpCenter.getTabCount(); ++i) {
										title = tbpCenter.getTitleAt(i);
										for (; Character.isDigit(title.charAt(0)); title = title.substring(1));
										title = title.substring(titleDivider.length());
										if (title.equalsIgnoreCase(qrp.getTableName())) {
											index = i;
											break;
										}
									}
								}
							}
							if (index >= 0) {
								tbpCenter.setTitleAt(index, queryCounter + titleDivider + qrp.getTableName());
								tbpCenter.setComponentAt(index, qrp);
							}
						}
						if (!renewGivenTab || index == -1) {
							tbpCenter.addTab((queryCounter + titleDivider + qrp.getTableName()).trim(), null, qrp,
									qrp.toolTipText);
							index = tbpCenter.getTabCount() - 1;
						}
						tbpCenter.setSelectedIndex(index);
						tbpCenter.setTabComponentAt(index, tabC);
						tbpCenter.setToolTipTextAt(index, qrp.getToolTipText());
					} else {
						QueryResultTabPanel qrp = (QueryResultTabPanel)tbpCenter.getComponentAt(tabIndex);
						qrp.codeRerun(result);
						tbpCenter.setTitleAt(tabIndex, ++queryCounter + titleDivider + qrp.getTableName().trim());
						tbpCenter.setToolTipTextAt(tabIndex, qrp.getToolTipText());
					}

					edt.addHistoryQuery(treHistory, mdlHistory, query);
					historyForwardList.clear();
					addHistoryBack(query.getQueryString());
					enableHistoryButtons();

					tbpCenter.repaint();
				}
			} else {
				// es handelt sich NICHT um ein SELECT
				try {
					if (connection.executeUpdate(codeToRun)) {
						showQueryTabPanel(code, null);

						edt.addHistoryQuery(treHistory, mdlHistory, new Query(code, code));

						historyForwardList.clear();
						addHistoryBack(code);
						enableHistoryButtons();
					}
				}
				catch (QueryException e) {
					showQueryTabPanel(code, e.getMessage());
				}
			}
		}
		txtEditor.requestFocus();
	}

	public void displayHelp() {
		JOptionPane.showMessageDialog(this, "Help function is not implemented yet.... sorry!", EDT.APPLICATION_TITLE,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public void copy(JEditTextArea editor) {
		String content = editor.getSelectedText();
		if (content == null || content.length() == 0)
			content = editor.getText();

		if (content != null && content.length() != 0)
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringTransfer(content), this);
	}

	public void paste(JEditTextArea editor) {
		Transferable transfer = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if (transfer != null) {
			try {
				editor.setSelectedText((String)transfer.getTransferData(DataFlavor.stringFlavor));
			}
			catch (UnsupportedFlavorException e1) {}
			catch (IOException e1) {}
		}
	}

	public void cut(JEditTextArea editor) {
		copy(editor);
		editor.setSelectedText("");
	}

	public void openExternalEditorWindow() {
		externEditor.setVisible(true);
		externEditor.requestFocus();
	}

	void showQueryTabPanel() {
		int position = -1;
		for (int i = 0; i < tbpCenter.getTabCount(); ++i) {
			if (tbpCenter.getComponentAt(i) == queryPanel) {
				position = i;
				break;
			}
		}
		if (position != -1) {
			tbpCenter.setSelectedIndex(position);
		} else {
			tbpCenter.addTab("Query results", queryPanel);
			tbpCenter.setTabComponentAt(tbpCenter.getTabCount() - 1, queryPanelTabComponent);
			tbpCenter.setSelectedIndex(tbpCenter.getTabCount() - 1);
		}
		queryPanelTabComponent.repaint();
	}

	void showQueryTabPanel(String sql, String message) {
		showQueryTabPanel();
		queryPanel.addRow(sql, message);
	}

	private void readColumnsForTable(Table table) {
		if (table.columns.isEmpty()) {
			for (String col : connection.getColumns(table.getTable()))
				table.addColumn(col);
			((DefaultTreeModel)treTables.getModel()).nodeStructureChanged(table.getTreeNode());
		}
	}

	private void showQueryBuilderForTable(Table table) {
		readColumnsForTable(table);
		String query = queryBuilder.showForTable(table);
		if (query != null)
			setCodeEditorText(query);

	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// aha... do nothing
	}

	private class TreeSelectionHandler extends MouseAdapter {
		private JTree tree;

		public TreeSelectionHandler(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
			if (selRow != -1 && selPath != null) {
				switch (e.getButton()) {
					case MouseEvent.BUTTON1:
						if (e.getClickCount() == 2) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();

							if (node.getUserObject() instanceof AbstractExecutable) {
								AbstractExecutable exe = (AbstractExecutable)node.getUserObject();

								boolean showQueryBuilder = e.getSource() == treTables
										&& mnuOptionsQueryBuilder.isSelected() && exe instanceof Table;

								if (!showQueryBuilder) {
									setCodeEditorText(exe.getQueryString());
								} else {
									showQueryBuilderForTable((Table)exe);
								}

								if (exe instanceof Table && e.getSource() == treTables) {
									// TODO vllt verhindern, dass die
									// tabellen-daten ausgeklappt werden
								}
							}
						}
						break;
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
			if (selRow != -1 && selPath != null) {
				switch (e.getButton()) {
					case MouseEvent.BUTTON3:
						if (e.getSource() == treTables) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();
							if (node.getUserObject() instanceof Table) {
								treTables.setSelectionPath(selPath);
								popTreeTablesRightclick.show(treTables, e.getX(), e.getY());
							}
						}
						break;
				}
			}
		}
	}

	private static class EDTTreeCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

		private Icon icoTable = IconFactory.getIcon("table");
		private Icon icoColumn = IconFactory.getIcon("column");
		private Icon icoFolder = IconFactory.getIcon("folder");
		private Icon icoFolderOpen = IconFactory.getIcon("folder open");
		private Icon icoQuery = IconFactory.getIcon("query");

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

			setToolTipText(null);

			if (node.getUserObject() instanceof Table) {
				setIcon(icoTable);
			} else if (node.getUserObject() instanceof Column) {
				setIcon(icoColumn);
			} else if (node.getUserObject() instanceof Query) {
				setIcon(icoQuery);
				setToolTipText("<html><pre>" + ((Query)node.getUserObject()).getQueryString() + "</pre></html>");
			} else if (node.getUserObject() instanceof QueryFolder) {
				if (!expanded)
					setIcon(icoFolder);
				else
					setIcon(icoFolderOpen);
				QueryFolder qf = (QueryFolder)node.getUserObject();
				if (qf.getQueryCount() == 0)
					setToolTipText("The folder '" + qf.getName() + "' contains no queries");
				else if (qf.getQueryCount() == 1)
					setToolTipText("The folder '" + qf.getName() + "' contains one query");
				else
					setToolTipText("The folder '" + qf.getName() + "' contains " + qf.getQueryCount() + " queries");
			} else {
				setIcon(null);
			}

			return this;
		}
	}

	private abstract class SaveIntActionListener implements ActionListener {
		protected final int amount;

		public SaveIntActionListener(int amount) {
			this.amount = amount;
		}
	}

	private static class DelayedPopupMenuMouseListener extends MouseAdapter {
		private final ActionListener actionListener;
		private final Component comp;
		private Thread waiter;

		public DelayedPopupMenuMouseListener(Component comp, ActionListener actionlistener) {
			this.comp = comp;
			this.actionListener = actionlistener;
		}

		@Override
		public synchronized void mousePressed(MouseEvent e) {
			if (comp.isEnabled()) {
				if (waiter != null)
					waiter.interrupt();
				waiter = new waiterThread();
				waiter.start();
			}
		}

		@Override
		public synchronized void mouseReleased(MouseEvent e) {
			if (waiter != null)
				waiter.interrupt();
		}

		private class waiterThread extends Thread {
			@Override
			public void run() {
				try {
					sleep(POPUP_MENU_WAITING_TIME);
					if (actionListener != null)
						actionListener.actionPerformed(new ActionEvent(this, 0, null));
				}
				catch (InterruptedException e) {}
			}
		}
	}

	private class ExportHandler implements ActionListener {
		private ExportFormats format;

		public ExportHandler(ExportFormats format) {
			this.format = format;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (tbpCenter.getSelectedIndex() != -1 && tbpCenter.getSelectedComponent() instanceof QueryResultTabPanel)
				format.export((QueryResultTabPanel)tbpCenter.getSelectedComponent());
		}
	}

	private class TabReloader implements ActionListener {
		private QueryResultTabPanel qrp;

		TabReloader(QueryResultTabPanel qrp) {
			this.qrp = qrp;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			runCode(qrp.getQueryResult().sql, qrp.getTabComponent().getTabIndex(), false);
		}
	}
}
