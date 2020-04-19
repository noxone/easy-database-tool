package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.noxfire.edt.gui.ButtonTabComponent;
import org.noxfire.edt.gui.CheckListManager;
import org.noxfire.edt.table.EdtTableModel;
import org.noxfire.edt.table.GeneralTableCellEditor;
import org.noxfire.edt.table.GeneralTableCellRenderer;
import org.noxfire.edt.table.TableCellEditorListener;

public class QueryResultTabPanel extends TabPanel implements TableCellEditorListener, ClipboardOwner
{
	private static final long serialVersionUID = 1L;

	public static final int TIME_RESULTSET_STAY_OPEN = 10 * 60 * 1000;// 10
	// Minuten

	public static final int INITIAL_NUMBER_OF_ROWS_TO_READ = 30;
	public static final int TABLE_DATA_WIDTH_MULTIPLICATOR = 9;
	public static final int TABLE_MAX_INITIAL_COLUMN_CHAR_WIDTH = 128;
	public static final int TABLE_MIN_INITIAL_COLUMN_CHAR_WIDTH = 5;
	public static final int COLUMN_LIST_PREFERRED_SIZE = 150;

	private final MainWindow owner;
	private final DBConnection connection;
	String toolTipText;

	private String updateWarning = null;

	private QueryResult result;
	private Vector<Vector<Object>> data;
	private DefaultListModel dlmColumns;
	private CheckListManager clmColumns;
	private Vector<Boolean> boolColumns;

	private int rowsRead = 0;

	private JPanel pnlTop;
	private JPanel pnlContent;
	private JPanel pnlBottom;
	private JPanel pnlBottomRight;
	private JTable tblData = null;
	private ColumnHeaderToolTips tipData;
	private TableRowSorter<? extends TableModel> sorter;
	private JTextField txtSearch;
	private JLabel lblRunTime;
	private JToggleButton chkShowColumnList;
	private JLabel lblInfo;
	private JButton btnReadMore;
	private JButton btnCloseStatement;
	private JScrollPane sclColumns;
	private JPanel pnlColumns;
	private JList lstColumns;

	// Popupmenü auf dem Tab
	private JPopupMenu popTab;
	private JMenuItem mnuSqlCode;
	private JMenuItem mnuRerunCode;
	private JMenuItem mnuCloseTab;
	private JMenuItem mnuCloseOtherTabs;

	// Popupmenu, um mehr Zeilen zu lesen
	private JPopupMenu popReadMore;
	private JMenuItem mnuRead50;
	private JMenuItem mnuRead100;
	private JMenuItem mnuRead200;
	private JMenuItem mnuRead500;
	private JMenuItem mnuRead1000;
	private JMenuItem mnuRead2000;
	private JMenuItem mnuRead5000;
	private JMenuItem mnuReadAll;

	// Popupmenu in der Tabelle
	private JPopupMenu popEdit;
	private JMenuItem mnuCopyCell;
	private JMenuItem mnuCopyRow;
	private JMenuItem mnuDeleteRow;
	private JMenuItem mnuInsertRow;

	// Popupmenu im Tabellenkopf
	private JPopupMenu popTableHeader;
	private JMenuItem mnuAddWhereClause;
	private JMenu mnuAddSpecificWhereClause;
	private JMenu mnuAddSpecificWhereNotClause;
	private JMenuItem mnuAddGroupByClause;
	private JMenuItem mnuAddHavingClause;
	private JMenuItem mnuAddOrderByAscClause;
	private JMenuItem mnuAddOrderByDescClause;

	private String sAddWhere;
	private String sAddOrderByAsc;
	private String sAddOrderByDesc;
	private String sAddGroupBy;
	private String sAddHaving;

	ActionListener copyRowAction = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (!result.editable)
				return;
			Vector<Vector<Object>> lines = new Vector<Vector<Object>>();
			for (int i : tblData.getSelectedRows())
			{
				i = tblData.convertRowIndexToModel(i);
				Vector<Object> row = new Vector<Object>();
				lines.add(row);
				for (int k = 0; k < data.get(i).size(); ++k)
					row.add(data.get(i).get(k));
			}
			JTable table = new JTable();
			InsertRowDialog crd = new InsertRowDialog(QueryResultTabPanel.this.owner, table, ((EdtTableModel)tblData
					.getModel()).clone(table), lines, result.widths, QueryResultTabPanel.this, result.nullable);
			crd.setVisible(true);
		}
	};
	ActionListener deleteRowAction = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (!result.editable)
				return;
			if (JOptionPane.showConfirmDialog(QueryResultTabPanel.this.owner,
					"Do you really want to delete the selected line"
							+ (tblData.getSelectedRows().length > 1 ? "s" : "") + "?", EDT.APPLICATION_TITLE,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
			{
				Vector<Integer> rows = new Vector<Integer>();
				for (int row : tblData.getSelectedRows())
				{
					row = tblData.convertRowIndexToModel(row);
					try
					{
						runDelete(row);
						rows.add(row);
					}
					catch (QueryException e1)
					{
						JOptionPane.showMessageDialog(QueryResultTabPanel.this.owner, "Cannot delete row."
								+ e1.getMessage(), EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
						break;
					}
				}
				Collections.sort(rows);
				for (int i = 0; i < rows.size(); ++i)
				{
					data.remove(rows.get(i) - i);
				}
				((EdtTableModel)tblData.getModel()).fireTableDataChanged();
				if (rows.size() == 0)
					setInfoText("No rows deleted!", false);
				else if (rows.size() == 1)
					setInfoText("1 row deleted!", false);
				else
					setInfoText(rows.size() + " rows deleted!", false);
			}
		}
	};
	ActionListener insertRowAction = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (!result.editable)
				return;
			JTable table = new JTable();
			InsertRowDialog crd = new InsertRowDialog(QueryResultTabPanel.this.owner, table, ((EdtTableModel)tblData
					.getModel()).clone(table), null, result.widths, QueryResultTabPanel.this, result.nullable);
			crd.setVisible(true);
		}
	};

	QueryResultTabPanel(MainWindow owner, QueryResult result, final ButtonTabComponent tabComponent,
			DBConnection connection)
	{
		super(tabComponent);

		// ein paar wichtige Werte setzen
		this.owner = owner;
		this.connection = connection;
		this.result = result;
		data = new Vector<Vector<Object>>();

		// gui hübsch einrichten
		pnlTop = new JPanel();
		pnlContent = new JPanel();
		pnlBottom = new JPanel();
		pnlBottomRight = new JPanel();
		txtSearch = new JTextField(30);
		txtSearch.setToolTipText("Enter a regular expression to filter the read lines...");
		lblInfo = new JLabel("");
		btnReadMore = new JButton("Read more...");
		btnCloseStatement = new JButton("Close Statement");

		// GUI aufbauen
		tblData = new JTable(data, result.columns);
		// tblData.setFillsViewportHeight(true);
		tblData.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		tblData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblData.getTableHeader().setReorderingAllowed(false);
		EdtTableModel model = new EdtTableModel(tblData, result.columns, data, result.classes, result.nullable,
				result.columnCount, result.editable);
		// sorter = new TableRowSorter<EdtTableModel>();
		tblData.setModel(model);
		tblData.setAutoCreateRowSorter(true);
		setTableRowHeight(owner.edt.getIntOption(MainWindow.OPTIONS_RESULT_TABLE_ROW_HEIGHT,
				MainWindow.OPTIONS_RESULT_TABLE_ROW_HEIGHT_STANDARD));
		tblData.setFont(owner.getCurrentFont());
		tipData = new ColumnHeaderToolTips();
		tblData.setShowGrid(false);

		// Tooltips für
		tblData.getTableHeader().addMouseMotionListener(tipData);

		pnlTop.setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.add(new JLabel("Filter:"));
		panel.add(txtSearch);
		pnlTop.add(panel, BorderLayout.WEST);

		pnlBottom.setLayout(new BorderLayout());
		pnlBottom.add(pnlBottomRight, BorderLayout.EAST);
		pnlBottom.add(lblInfo, BorderLayout.WEST);
		pnlBottomRight.add(btnReadMore);
		pnlBottomRight.add(btnCloseStatement);
		pnlContent.setLayout(new BorderLayout());
		pnlContent.add(new JScrollPane(tblData), BorderLayout.CENTER);

		// RunTime und Checkbox-Liste
		chkShowColumnList = new JToggleButton("Columns", true);
		lblRunTime = new JLabel();
		panel = new JPanel();
		panel.add(lblRunTime);
		panel.add(chkShowColumnList);
		pnlTop.add(panel, BorderLayout.EAST);

		setLayout(new BorderLayout());
		add(pnlTop, BorderLayout.NORTH);
		add(pnlContent, BorderLayout.CENTER);
		add(pnlBottom, BorderLayout.SOUTH);

		// Kram initialisieren
		initQueryResultTabPanel(result, INITIAL_NUMBER_OF_ROWS_TO_READ);
		showColumnCheckboxList(owner.edt.showColumnsList());

		tblData.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					int row = tblData.rowAtPoint(e.getPoint());
					if (!tblData.isRowSelected(row))
					{
						tblData.getSelectionModel().setSelectionInterval(row, row);
					}
					popEdit.show(tblData, e.getX(), e.getY());
				}
			}
		});
		tblData.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if ((e.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)) == (KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK))
				{
					switch (e.getKeyCode())
					{
						case KeyEvent.VK_C:
							copyRowAction.actionPerformed(new ActionEvent(tblData, 0, null));
							break;
						case KeyEvent.VK_I:
							insertRowAction.actionPerformed(new ActionEvent(tblData, 0, null));
							break;
						case KeyEvent.VK_DELETE:
							deleteRowAction.actionPerformed(new ActionEvent(tblData, 0, null));
							break;
					}
				}
				if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK)
				{
					switch (e.getKeyCode())
					{
						case KeyEvent.VK_C:
							copyCellValue(tblData.convertRowIndexToModel(tblData.getSelectedRow()), tblData
									.convertColumnIndexToModel(tblData.getSelectedColumn()));
							break;
					}
				}
			}
		});

		btnReadMore.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				popReadMore.show(btnReadMore, btnReadMore.getMousePosition().x, btnReadMore.getMousePosition().y);
			}
		});
		btnCloseStatement.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				closeResultSet();
			}
		});
		txtSearch.setDocument(new PlainDocument()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
			{
				super.insertString(offs, str, a);
				createSearchFilter();
			}

			@Override
			public void remove(int offs, int len) throws BadLocationException
			{
				super.remove(offs, len);
				if (offs == 0 && len == txtSearch.getText().length())
				{
					sorter.setRowFilter(null);

					int lines = tblData.getRowCount();
					if (lines == 0)
						setInfoText("Filtering disabled. Table contains no lines!", false);
					else if (lines == 1)
						setInfoText("Filtering disabled. Table contains one line!", false);
					else
						setInfoText("Filtering disabled. Table contains " + lines + " lines!", false);
				}
				else
					createSearchFilter();
			}
		});
		chkShowColumnList.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				showColumnCheckboxList(chkShowColumnList.isSelected());
			}
		});

		popTab = new JPopupMenu();
		mnuSqlCode = new JMenuItem("Edit SQL code");
		mnuRerunCode = new JMenuItem("Rerun this SQL code");
		mnuCloseTab = new JMenuItem("Close tab");
		mnuCloseOtherTabs = new JMenuItem("Close other tabs");
		popTab.add(mnuSqlCode);
		popTab.add(mnuRerunCode);
		popTab.addSeparator();
		popTab.add(mnuCloseTab);
		popTab.add(mnuCloseOtherTabs);
		tabComponent.setComponentPopupMenu(popTab);
		mnuCloseTab.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				tabComponent.closeTab();
			}
		});
		mnuCloseOtherTabs.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QueryResultTabPanel.this.owner.closeOtherTabs(tabComponent.getTabIndex());
			}
		});
		mnuRerunCode.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QueryResultTabPanel.this.owner.runCode(QueryResultTabPanel.this.result.sql, tabComponent.getTabIndex(),
						false);
			}
		});
		mnuSqlCode.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QueryResultTabPanel.this.owner.setCodeEditorText(QueryResultTabPanel.this.result.sql);
			}
		});

		popReadMore = new JPopupMenu();
		mnuRead50 = new JMenuItem("Read 50 rows");
		mnuRead100 = new JMenuItem("Read 100 rows");
		mnuRead200 = new JMenuItem("Read 200 rows");
		mnuRead500 = new JMenuItem("Read 500 rows");
		mnuRead1000 = new JMenuItem("Read 1000 rows");
		mnuRead2000 = new JMenuItem("Read 2000 rows");
		mnuRead5000 = new JMenuItem("Read 5000 rows");
		mnuReadAll = new JMenuItem("Read all rows");

		mnuRead50.addActionListener(new ReadMoreHandler(50));
		mnuRead100.addActionListener(new ReadMoreHandler(100));
		mnuRead200.addActionListener(new ReadMoreHandler(200));
		mnuRead500.addActionListener(new ReadMoreHandler(500));
		mnuRead1000.addActionListener(new ReadMoreHandler(1000));
		mnuRead2000.addActionListener(new ReadMoreHandler(2000));
		mnuRead5000.addActionListener(new ReadMoreHandler(5000));
		mnuReadAll.addActionListener(new ReadMoreHandler(-1));

		popReadMore.add(mnuRead50);
		popReadMore.add(mnuRead100);
		popReadMore.add(mnuRead200);
		popReadMore.add(mnuRead500);
		popReadMore.add(mnuRead500);
		popReadMore.add(mnuRead1000);
		popReadMore.add(mnuRead2000);
		popReadMore.add(mnuRead5000);
		popReadMore.addSeparator();
		popReadMore.add(mnuReadAll);

		popEdit = new JPopupMenu()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void show(Component invoker, int x, int y)
			{
				setMultiLineSelectionMenuItem();
				super.show(invoker, x, y);
			}
		};
		mnuCopyCell = new JMenuItem("Copy cell value");
		mnuCopyRow = new JMenuItem("Copy row...");
		mnuDeleteRow = new JMenuItem("Delete row...");
		mnuInsertRow = new JMenuItem("Insert row...");
		// mnuCopyCell.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		// ActionEvent.CTRL_MASK));
		mnuCopyRow
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		mnuDeleteRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.CTRL_MASK
				| ActionEvent.SHIFT_MASK));
		mnuInsertRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK
				| ActionEvent.SHIFT_MASK));
		popEdit.add(mnuCopyCell);
		popEdit.addSeparator();
		popEdit.add(mnuInsertRow);
		popEdit.addSeparator();
		popEdit.add(mnuCopyRow);
		popEdit.add(mnuDeleteRow);
		mnuDeleteRow.addActionListener(deleteRowAction);
		mnuCopyRow.addActionListener(copyRowAction);
		mnuInsertRow.addActionListener(insertRowAction);
		mnuCopyCell.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				copyCellValue(tblData.convertRowIndexToModel(tblData.getSelectedRow()), tblData
						.convertColumnIndexToModel(tblData.getSelectedColumn()));
			}
		});

		final Font fntMenu = new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize());
		popTableHeader = new JPopupMenu();
		mnuAddWhereClause = new JMenuItem("Add Where...");
		mnuAddSpecificWhereClause = new JMenu("Add specific WHERE clause...");
		mnuAddSpecificWhereNotClause = new JMenu("Add specific WHERE NOT clause...");
		mnuAddGroupByClause = new JMenuItem("Add Group By...");
		mnuAddHavingClause = new JMenuItem("Add Having...");
		mnuAddOrderByAscClause = new JMenuItem("Add Order By Asc...");
		mnuAddOrderByDescClause = new JMenuItem("Add Order By Desc...");
		mnuAddWhereClause.setFont(fntMenu);
		mnuAddGroupByClause.setFont(fntMenu);
		mnuAddHavingClause.setFont(fntMenu);
		mnuAddOrderByAscClause.setFont(fntMenu);
		mnuAddOrderByDescClause.setFont(fntMenu);
		mnuAddOrderByAscClause.setLayout(new BorderLayout());
		popTableHeader.add(mnuAddWhereClause);
		popTableHeader.add(mnuAddSpecificWhereClause);
		popTableHeader.add(mnuAddSpecificWhereNotClause);
		popTableHeader.addSeparator();
		popTableHeader.add(mnuAddGroupByClause);
		popTableHeader.add(mnuAddHavingClause);
		popTableHeader.add(mnuAddOrderByAscClause);
		popTableHeader.add(mnuAddOrderByDescClause);
		tblData.getTableHeader().addMouseListener(new ColumnHeaderPopupMenu(new TableHeaderPopupMenuListener()
		{
			private String getExpression(boolean klammern, String text)
			{
				return (klammern ? "'" : "") + text + (klammern ? "'" : "");
			}

			@Override
			public void popupMenuCalled(TableColumn column, Point mousePosition)
			{
				int col = 0;
				for (int i = 0; i < tblData.getColumnModel().getColumnCount(); ++i)
					if (tblData.getColumnModel().getColumn(i) == column)
					{
						col = ((EdtTableModel)tblData.getModel()).convertToRealColumnColumnNumber(tblData
								.convertColumnIndexToModel(i));
						break;
					}
				boolean klammern = QueryResultTabPanel.this.result.classes.get(col) == String.class;

				// statisches Menü einrichten
				mnuAddWhereClause.setText("WHERE " + QueryResultTabPanel.this.result.columns.get(col) + " = "
						+ getExpression(klammern, ""));
				sAddWhere = QueryResultTabPanel.this.result.columns.get(col) + " = " + getExpression(klammern, "");
				mnuAddOrderByAscClause.setText("ORDER BY " + QueryResultTabPanel.this.result.columns.get(col) + " ASC");
				sAddOrderByAsc = QueryResultTabPanel.this.result.columns.get(col) + " ASC";
				mnuAddOrderByDescClause.setText("ORDER BY " + QueryResultTabPanel.this.result.columns.get(col)
						+ " DESC");
				sAddOrderByDesc = QueryResultTabPanel.this.result.columns.get(col) + " DESC";
				mnuAddGroupByClause.setText("GROUP BY " + QueryResultTabPanel.this.result.columns.get(col));
				sAddGroupBy = QueryResultTabPanel.this.result.columns.get(col);
				mnuAddHavingClause.setText("HAVING " + QueryResultTabPanel.this.result.columns.get(col) + " = "
						+ getExpression(klammern, ""));
				sAddHaving = QueryResultTabPanel.this.result.columns.get(col) + " = " + getExpression(klammern, "");

				// dynamisches Menü einrichten
				class MenuItems
				{
					JMenuItem item;
					JMenuItem notItem;
				}
				mnuAddSpecificWhereClause.removeAll();
				mnuAddSpecificWhereNotClause.removeAll();
				HashMap<String, MenuItems> dynItems = new HashMap<String, MenuItems>();
				Vector<String> titems = new Vector<String>();
				for (int i = 0; i < data.size(); ++i)
				{
					Object o = data.get(i).get(col);
					if (o != null)
					{
						String s = o.toString().trim();
						if (dynItems.get(s) == null)
						{
							MenuItems mi = new MenuItems();
							final String clause = QueryResultTabPanel.this.result.columns.get(col) + " = "
									+ getExpression(klammern, s) + "";
							final String notClause = "NOT " + QueryResultTabPanel.this.result.columns.get(col) + " = "
									+ getExpression(klammern, s) + "";

							titems.add(s);
							mi.item = new JMenuItem("WHERE " + QueryResultTabPanel.this.result.columns.get(col) + " = "
									+ getExpression(klammern, s) + "");
							mi.item.setFont(fntMenu);
							mi.item.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent e)
								{
									QueryResultTabPanel.this.owner.addWhereClause(clause);
								}
							});
							mi.notItem = new JMenuItem("WHERE NOT " + QueryResultTabPanel.this.result.columns.get(col)
									+ " = " + getExpression(klammern, s) + "");
							mi.notItem.setFont(fntMenu);
							mi.notItem.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent e)
								{
									QueryResultTabPanel.this.owner.addWhereClause(notClause);
								}
							});
							dynItems.put(s, mi);
						}
					}
				}
				Collections.sort(titems);
				for (String s : titems)
				{
					mnuAddSpecificWhereClause.add(dynItems.get(s).item);
					mnuAddSpecificWhereNotClause.add(dynItems.get(s).notItem);
				}

				// Menü anzeigen
				popTableHeader.show(tblData.getTableHeader(), mousePosition.x, mousePosition.y);
			}
		}, tblData.getTableHeader(), tblData));
		mnuAddWhereClause.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QueryResultTabPanel.this.owner.addWhereClause(sAddWhere);
			}
		});
		mnuAddOrderByAscClause.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QueryResultTabPanel.this.owner.addOrderByClause(sAddOrderByAsc);
			}
		});
		mnuAddOrderByDescClause.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QueryResultTabPanel.this.owner.addOrderByClause(sAddOrderByDesc);
			}
		});
		mnuAddGroupByClause.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QueryResultTabPanel.this.owner.addGroupByClause(sAddGroupBy);
			}
		});
		mnuAddHavingClause.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QueryResultTabPanel.this.owner.addHavingClause(sAddHaving);
			}
		});
	}

	private void initQueryResultTabPanel(QueryResult result, int linesToRead)
	{
		data.clear();

		if (dlmColumns == null)
			dlmColumns = new DefaultListModel();
		if (pnlColumns == null)
		{
			pnlColumns = new JPanel();
			pnlColumns.setLayout(new BorderLayout());

		}
		lstColumns = new JList(dlmColumns);
		lstColumns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (sclColumns != null)
			pnlColumns.remove(sclColumns);
		sclColumns = new JScrollPane(lstColumns);
		pnlColumns.add(sclColumns, BorderLayout.CENTER);
		clmColumns = new CheckListManager(lstColumns);
		dlmColumns.clear();
		if (boolColumns == null)
			boolColumns = new Vector<Boolean>();
		boolColumns.clear();

		this.result = result;

		rowsRead = 0;
		readLines(linesToRead);

		EdtTableModel model = new EdtTableModel(tblData, result.columns, data, result.classes, result.nullable,
				result.columnCount, result.editable);
		tblData.setModel(model);
		sorter = (TableRowSorter<? extends TableModel>)tblData.getRowSorter();
		tipData.clear();

		lblRunTime.setText(new java.util.Date().toString());

		// Spalten der Tabelle einigermaßen einrichten, damit sich die Werte
		// auch wohlfühlen
		int columnsWidth = 0;
		for (int i = 0; i < result.columnCount; i++)
		{
			TableColumn column = tblData.getColumnModel().getColumn(i);
			column.setPreferredWidth(result.widths.get(i));
			columnsWidth += result.widths.get(i);

			column.setCellRenderer(new GeneralTableCellRenderer());
			column.setCellEditor(new GeneralTableCellEditor(result.classes.get(i), result.nullable.get(i), this));

			String header = "<html><b>" + result.columns.get(i) + "</b><br><pre>" + result.types.get(i)
					+ "</pre></html>";
			String tip = "<html><b>" + result.columns.get(i) + "</b>";
			if (result.originalTableNames.get(i).trim().equalsIgnoreCase(""))
				tip += "<br><i>Column is generated by the query.</i>";
			else
			{
				if (result.tableCount != 1)
					tip += "<br>Column source is table '<b>" + result.originalTableNames.get(i) + "</b>'";
				else if (result.tableCount == 1 && result.isKeyCoumn(result.columns.get(i)))
					tip += "<br>This column is a key column!";
			}
			tip += "<pre>" + result.types.get(i) + "</pre>";
			if (result.nullable.get(i))
				tip += "This column is nullable.";
			tip += "</html>";
			column.setHeaderValue(header);
			tipData.setToolTip(column, tip);

			dlmColumns.addElement(result.columns.get(i));
			clmColumns.getSelectionModel().addSelectionInterval(i, i + 1);
			boolColumns.add(true);
		}

		String tooltip = "<html>Result for query:<br><pre>" + result.sql + "</pre><br>";
		// Icon setzen
		if (result.tableCount == 1)
		{
			if (result.editable)
			{
				this.tabComponent.setIcon(IconFactory.getIcon("table editable"));
				tooltip += "Data is editable.";
			}
			else
			{
				this.tabComponent.setIcon(IconFactory.getIcon("table not editable"));
				tooltip += "Data is <b>not</b> editable!";
			}
		}
		else
		{
			this.tabComponent.setIcon(IconFactory.getIcon("multiple tables"));
			tooltip += "Data is <b>not</b> editable because it contains more than one table!";
		}

		tooltip += "</html>";
		toolTipText = tooltip;
		// toolTipText = result.sql;
		// tabComponent.label.setToolTipText(toolTipText);

		clmColumns.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				for (int i = e.getFirstIndex(); i <= e.getLastIndex() && i < boolColumns.size(); ++i)
				{
					if (boolColumns.get(i) != clmColumns.getSelectionModel().isSelectedIndex(i))
					{
						boolColumns.set(i, clmColumns.getSelectionModel().isSelectedIndex(i));
						((EdtTableModel)tblData.getModel()).setColumnVisibility(i, boolColumns.get(i), tipData);
					}
				}
			}
		});
		lstColumns.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				for (int i = 0; i < boolColumns.size(); ++i)
				{
					if (lstColumns.isSelectedIndex(i))
						scrollToColumn(i);
				}
			}
		});
	}

	@Override
	public String getToolTipText()
	{
		return toolTipText;
	}

	public void codeRerun(QueryResult result)
	{
		try
		{
			this.result.statement.close();
		}
		catch (SQLException e)
		{}
		btnReadMore.setEnabled(true);
		btnCloseStatement.setEnabled(true);
		this.result = result;
		initQueryResultTabPanel(result, rowsRead > INITIAL_NUMBER_OF_ROWS_TO_READ ? rowsRead
				: INITIAL_NUMBER_OF_ROWS_TO_READ);
		((EdtTableModel)tblData.getModel()).fireTableDataChanged();
	}

	private void setMultiLineSelectionMenuItem()
	{
		if (tblData.getSelectedRows().length > 1)
		{
			mnuCopyRow.setText("Copy rows...");
			mnuDeleteRow.setText("Delete rows...");
		}
		else
		{
			mnuCopyRow.setText("Copy row...");
			mnuDeleteRow.setText("Delete row...");
		}

		mnuCopyRow.setEnabled(result.editable);
		mnuDeleteRow.setEnabled(result.editable);
		mnuInsertRow.setEnabled(result.editable);
	}

	private void readLines(int numRows)
	{
		try
		{
			Vector<Object> dv;
			boolean going = true;
			for (int i = 0; (i < numRows || numRows < 0) && (going = result.resultset.next()); ++i)
			{
				dv = new Vector<Object>();
				for (int k = 1; k <= result.columnCount; ++k)
				{
					dv.add(result.resultset.getObject(k));
				}
				data.add(dv);
				++rowsRead;
			}

			if (!going)
			{
				closeResultSet();
			}

			if (rowsRead == 0)
			{
				setInfoText("Query returned no lines!", true);
			}
		}
		catch (SQLException e1)
		{
			JOptionPane.showMessageDialog(QueryResultTabPanel.this, "An error occurred while reading:\n"
					+ e1.getMessage(), EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
		}

		if (rowsRead > 1)
			setInfoText(rowsRead + " lines read!", false);
		else if (rowsRead == 1)
			setInfoText("One line read!", false);

		if (tblData != null)
			((EdtTableModel)tblData.getModel()).fireTableDataChanged();
	}

	private void createSearchFilter()
	{
		try
		{
			sorter.setRowFilter(RowFilter.regexFilter(txtSearch.getText()));
			int lines = tblData.getRowCount();
			if (lines == 0)
				setInfoText("No lines left after filtering!", false);
			else if (lines == 1)
				setInfoText("One line left after filtering!", false);
			else
				setInfoText(lines + " lines left after filtering!", false);
		}
		catch (PatternSyntaxException e)
		{
			setInfoText("Error parsing filter regex: " + e.getMessage(), true);
		}
	}

	private void scrollToColumn(int index)
	{
		int scrollLeft = 0;
		int inv = 0;
		for (int i = 0; i < index; ++i)
			if (((EdtTableModel)tblData.getModel()).isRealColumnVisible(i))
				scrollLeft += tblData.getColumnModel().getColumn(i - inv).getWidth();
			else
				++inv;
		tblData.scrollRectToVisible(new Rectangle(scrollLeft, 0, tblData.getColumnModel().getColumn(index - inv)
				.getWidth(), 0));
	}

	String getTableName()
	{
		return result.usedTables;
	}

	void closeResultSet()
	{
		try
		{
			result.statement.close();
		}
		catch (SQLException e)
		{}
		btnReadMore.setEnabled(false);
		btnCloseStatement.setEnabled(false);
	}

	@Override
	public JTable getTable()
	{
		return tblData;
	}

	@Override
	public synchronized boolean runUpdate(int column, int row, Object oldVal) throws QueryException
	{
		updateWarning = null;
		PreparedStatement stmt;
		try
		{
			stmt = result.getUpdateStatement(result.columns.get(column));
		}
		catch (SQLException e1)
		{
			throw new QueryException(e1.getMessage());
		}

		// Sicherheitsabfrage... nicht, dass da was daneben geht
		if (!result.editable || stmt == null)
			return false;

		try
		{
			setInfoText("Executing UPDATE statement...", false);
			Object value = data.get(row).get(column);

			if (value != null)
				stmt.setObject(1, value);
			else
				stmt.setNull(1, java.sql.Types.NULL);

			for (int i = 0; i < result.getKeyColumns().size(); ++i)
			{
				int col = result.getColumnNumber(result.getKeyColumns().get(i));
				if (col == -1)
					throw new QueryException("Column '" + result.getKeyColumns().get(i) + "' does not exist!");
				Object key = data.get(row).get(col);

				if (col == column)
					key = oldVal; // Da brauchen wir den alten Wert!!

				if (key != null)
					stmt.setObject(i + 2, key);
				else
					stmt.setNull(i + 2, java.sql.Types.NULL);
			}

			boolean succ = connection.executeUpdate(stmt);
			if (!succ)
			{
				if (stmt.getWarnings() != null)
					updateWarning = stmt.getWarnings().getMessage();
				else
					succ = true;
			}
			return succ;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getUpdateWarning()
	{
		return updateWarning;
	}

	public synchronized boolean runDelete(int row) throws QueryException
	{
		PreparedStatement stmt;
		try
		{
			stmt = result.getDeleteStatement();
		}
		catch (SQLException e)
		{
			throw new QueryException(e.getMessage());
		}

		// Sicherheitsabfrage... nicht, dass da was daneben geht
		if (!result.editable || stmt == null)
			return false;

		try
		{
			setInfoText("Executing DELETE statement...", false);
			for (int i = 0; i < result.getKeyColumns().size(); ++i)
			{
				int col = result.getColumnNumber(result.getKeyColumns().get(i));
				if (col == -1)
					throw new QueryException("Column '" + result.getKeyColumns().get(i) + "' not found...");

				stmt.setObject(i + 1, data.get(row).get(col));
			}

			boolean succ = connection.executeUpdate(stmt);
			return succ;
		}
		catch (SQLException e)
		{
			throw new QueryException(e.getMessage());
		}
	}

	@Override
	public String insertRows(Vector<Vector<Object>> rows)
	{
		PreparedStatement stmt;
		try
		{
			stmt = result.getInsertStatement();
		}
		catch (SQLException e)
		{
			setInfoText("Insert failed: " + e.getMessage(), true);
			return e.getMessage();
		}

		int i = 0;
		try
		{
			setInfoText("Executing INSERT statement...", false);
			for (i = 0; i < rows.size(); ++i)
			{
				runInsert(stmt, rows.get(i));
				data.add(rows.get(i));
			}

			if (rows.size() == 1)
				setInfoText("1 row successfully added!", false);
			else
				setInfoText(rows.size() + " rows successfully added!", false);
		}
		catch (QueryException e)
		{
			switch (i)
			{
				case 0:
					setInfoText("Insert failed! No rows have been inserted. Message is: " + e.getMessage(), true);
					break;
				case 1:
					setInfoText("Insert failed! One row has been inserted. Message is: " + e.getMessage(), true);
					break;
				default:
					setInfoText("Insert failed! " + i + " rows have been inserted. Message is: " + e.getMessage(), true);
					break;
			}
			return e.getMessage();
		}

		((EdtTableModel)tblData.getModel()).fireTableDataChanged();
		return null;
	}

	private synchronized boolean runInsert(PreparedStatement stmt, Vector<Object> data) throws QueryException
	{
		// Sicherheitsabfrage... nicht, dass da was daneben geht
		if (!result.editable || stmt == null)
			return false;

		for (int i = 0; i < data.size(); ++i)
		{
			try
			{
				stmt.setObject(i + 1, data.get(i));
			}
			catch (SQLException e)
			{
				throw new QueryException(e.getMessage());
			}
		}
		return connection.executeUpdate(stmt);
	}

	private class ReadMoreHandler implements ActionListener
	{
		private int count;

		public ReadMoreHandler(int count)
		{
			this.count = count;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (count != -1
					|| JOptionPane.showConfirmDialog(owner, "Do you really want to read ALL rows?",
							EDT.APPLICATION_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				readLines(count);
		}
	}

	public void print()
	{
		try
		{
			tblData.print();
		}
		catch (PrinterException e)
		{
			JOptionPane.showMessageDialog(owner, "An error occurred while trying to print:\n" + e.getMessage(),
					EDT.APPLICATION_TITLE + " - error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static enum ExportFormats
	{
		CSV("Comma Separated Values (CSV)", IconFactory.getIcon("csv"), new FileNameExtensionFilter(
				"Comma Separated Values File", "cvs")), XML(
				"XML",
				IconFactory.getIcon("xml"),
				new FileNameExtensionFilter("XML File", "xml"));

		private final String CSV_SEPARATOR = ";";

		final String description;
		final Icon icon;
		final FileFilter filter;

		private ExportFormats(String description, Icon icon, FileFilter filter)
		{
			this.description = description;
			this.icon = icon;
			this.filter = filter;
		}

		void export(QueryResultTabPanel panel)
		{
			JFileChooser chooser = new JFileChooser();
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);
			File file = null;
			if (chooser.showSaveDialog(panel) == JFileChooser.CANCEL_OPTION)
				return;
			else
			{
				file = chooser.getSelectedFile();
			}

			try
			{
				switch (this)
				{
					case CSV:
						if (chooser.getFileFilter() == filter && !file.getName().toLowerCase().endsWith(".csv"))
							file = new File(file.getAbsolutePath() + ".csv");

						PrintWriter writer = null;
						try
						{
							writer = new PrintWriter(file);
							for (int i = 0; i < panel.result.columnCount; ++i)
							{
								if (i > 0)
									writer.print(CSV_SEPARATOR);
								writer.print(panel.result.columns.get(i));
							}
							for (int r = 0; r < panel.rowsRead; ++r)
							{
								writer.println();
								for (int c = 0; c < panel.result.columnCount; ++c)
								{
									if (c > 0)
										writer.print(CSV_SEPARATOR);
									if (panel.data.get(r).get(c) != null)
										writer.print(panel.data.get(r).get(c).toString());
								}
							}
						}
						catch (FileNotFoundException e1)
						{}
						finally
						{
							if (writer != null)
								writer.close();
						}
						break;
					case XML:
					{
						if (chooser.getFileFilter() == filter && !file.getName().toLowerCase().endsWith(".csv"))
							file = new File(file.getAbsolutePath() + ".xml");

						Element root = new Element("export");
						root.setAttribute(new Attribute("tables", panel.result.usedTables));
						Element columns = new Element("columns");
						Element values = new Element("values");
						root.addContent(columns);
						root.addContent(values);
						for (int i = 0; i < panel.result.columnCount; ++i)
						{
							Element c = new Element("column");
							c.setAttribute(new Attribute("name", panel.result.columns.get(i)));
							c.setAttribute(new Attribute("number", Integer.toString(i)));
							c.setAttribute(new Attribute("className", panel.result.classes.get(i).getName()));
							columns.addContent(c);
						}
						for (int l = 0; l < panel.data.size(); ++l)
						{
							Element r = new Element("row");
							r.setAttribute(new Attribute("number", Integer.toString(l)));
							for (int c = 0; c < panel.result.columnCount; ++c)
							{
								Element cell = new Element("value");
								cell.setAttribute(new Attribute("number", Integer.toString(c)));
								if (panel.data.get(l).get(c) != null)
									cell.addContent(panel.data.get(l).get(c).toString());
								r.addContent(cell);
							}
							values.addContent(r);
						}

						Document doc = new Document(root);
						XMLOutputter Xout = new XMLOutputter();

						OutputStream out = null;
						try
						{
							out = new BufferedOutputStream(new FileOutputStream(file));
							out.write(Xout.outputString(doc).getBytes());
						}
						catch (FileNotFoundException e)
						{}
						catch (IOException e)
						{
							JOptionPane.showMessageDialog(null,
									"Unable to save settings to edt.xml! The settings won't be saved!",
									EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
						}
						finally
						{
							try
							{
								out.close();
							}
							catch (Exception e)
							{}
						}
					}
						break;
					default:
						JOptionPane.showMessageDialog(null, "Uncovered export case!", EDT.APPLICATION_TITLE, ERROR);
				}
			}
			catch (OutOfMemoryError e)
			{
				JOptionPane.showMessageDialog(null, "Out of memory! Export aborted!", EDT.APPLICATION_TITLE,
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void setInfoText(String text, boolean error)
	{
		lblInfo.setText(text);
		lblInfo.setToolTipText("<html><pre>" + text + "</pre></html>");
		if (error)
			lblInfo.setIcon(IconFactory.getIcon("error"));
		else
			lblInfo.setIcon(IconFactory.getIcon("no error"));
	}

	private void copyCellValue(int row, int column)
	{
		if (row != -1 && column != -1)
		{
			Object value = data.get(row).get(column);
			if (value != null)
			{
				String content = value.toString();
				// System.out.println(row + ":" + column + " ... " + content);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringTransfer(content), this);
			}
		}
	}

	private void showColumnCheckboxList(boolean show)
	{
		chkShowColumnList.setSelected(show);
		if (show)
		{
			if (sclColumns.getPreferredSize().width > COLUMN_LIST_PREFERRED_SIZE)
				sclColumns.setPreferredSize(new Dimension(COLUMN_LIST_PREFERRED_SIZE,
						sclColumns.getPreferredSize().height));
			pnlContent.add(pnlColumns, BorderLayout.EAST);
		}
		else
			pnlContent.remove(pnlColumns);
		owner.repaint();
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1)
	{
	// nothing
	}

	QueryResult getQueryResult()
	{
		return result;
	}

	ButtonTabComponent getTabComponent()
	{
		return tabComponent;
	}

	void setTableRowHeight(int height)
	{
		tblData.setRowHeight(height);
	}

	void setTableFont(Font font)
	{
		tblData.setFont(font);
	}

	public static class ColumnHeaderToolTips extends MouseMotionAdapter
	{
		// Current column whose tooltip is being displayed.
		// This variable is used to minimize the calls to setToolTipText().
		TableColumn curCol;

		// Maps TableColumn objects to tooltips
		HashMap<TableColumn, String> tips = new HashMap<TableColumn, String>();

		// If tooltip is null, removes any tooltip text.
		public String setToolTip(TableColumn col, String tooltip)
		{
			if (tooltip == null)
				return tips.remove(col);
			else
				return tips.put(col, tooltip);
		}

		@Override
		public void mouseMoved(MouseEvent evt)
		{
			TableColumn col = null;
			JTableHeader header = (JTableHeader)evt.getSource();
			JTable table = header.getTable();
			TableColumnModel colModel = table.getColumnModel();
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());

			// Return if not clicked on any column header
			if (vColIndex >= 0)
				col = colModel.getColumn(vColIndex);

			if (col != curCol)
			{
				header.setToolTipText((String)tips.get(col));
				curCol = col;
			}
		}

		public void clear()
		{
			tips.clear();
		}
	}

	public class ColumnHeaderPopupMenu implements MouseListener
	{
		private TableHeaderPopupMenuListener listener;
		private Component source;
		private Component dispatcher;

		public ColumnHeaderPopupMenu(TableHeaderPopupMenuListener l, Component source, Component dispatcher)
		{
			if (l == null)
				throw new NullPointerException("TableHeaderPopupMenuListener must not be null!");
			listener = l;
			this.dispatcher = dispatcher;
			this.source = source;
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON3 && data.size() > 0)
			{
				JTableHeader header = (JTableHeader)e.getSource();
				JTable table = header.getTable();
				TableColumnModel colModel = table.getColumnModel();
				int vColIndex = colModel.getColumnIndexAtX(e.getX());

				listener.popupMenuCalled(colModel.getColumn(vColIndex), new Point(e.getPoint().x, e.getPoint().y));
			}
			else
				dispatcher.dispatchEvent(SwingUtilities.convertMouseEvent(source, e, dispatcher));
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			dispatcher.dispatchEvent(SwingUtilities.convertMouseEvent(source, e, dispatcher));
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			dispatcher.dispatchEvent(SwingUtilities.convertMouseEvent(source, e, dispatcher));
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			dispatcher.dispatchEvent(SwingUtilities.convertMouseEvent(source, e, dispatcher));
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			dispatcher.dispatchEvent(SwingUtilities.convertMouseEvent(source, e, dispatcher));
		}
	}

	@Override
	public void userAttempsToSetFailingValue(String value)
	{
		setInfoText("The value '" + value + "' is not valid for the currently edited field!", true);
	}
}
