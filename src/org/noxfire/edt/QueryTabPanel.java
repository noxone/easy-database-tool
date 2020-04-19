package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.noxfire.edt.gui.ButtonTabComponent;

class QueryTabPanel extends TabPanel
{
	private static final long serialVersionUID = 1L;

	private final MainWindow owner;

	private JTable tblData;
	private Vector<String> columns;
	private Vector<Vector<Object>> data;

	private JPopupMenu popEdit;
	private JMenuItem mnuEditSql;
	private JMenuItem mnuClearList;

	public QueryTabPanel(MainWindow owner, ButtonTabComponent tabComponent)
	{
		super(tabComponent);
		this.owner = owner;

		columns = new Vector<String>();
		columns.add("Date & time");
		columns.add("SQL");
		columns.add("OK");
		columns.add("Message");
		data = new Vector<Vector<Object>>();

		tblData = new JTable(data, columns)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent event)
			{
				int rowIndex = rowAtPoint(event.getPoint());
				int colIndex = columnAtPoint(event.getPoint());
				int realRowIndex = convertRowIndexToModel(rowIndex);
				int realColumnIndex = convertColumnIndexToModel(colIndex);

				switch (realColumnIndex)
				{
					case 1:
						return "<html><pre>" + data.get(realRowIndex).get(realColumnIndex).toString() + "</pre></html>";
					case 3:
						if (data.get(realRowIndex).get(realColumnIndex) != null)
							return data.get(realRowIndex).get(realColumnIndex).toString();
						else
							return super.getToolTipText(event);
					default:
						return super.getToolTipText(event);
				}
			}
		};
		tblData.setModel(new QueryTableModel());
		tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblData.setAutoCreateRowSorter(true);
		tblData.setRowHeight(IconFactory.getIcon("no error").getIconHeight());
		for (int i = 0; i < tblData.getColumnCount(); ++i)
		{
			TableColumn column = tblData.getColumnModel().getColumn(i);

			switch (i)
			{
				case 0:
					column.setPreferredWidth(150);
					break;
				case 1:
					column.setPreferredWidth(250);
					break;
				case 3:
					column.setPreferredWidth(400);
					break;
				case 2:
					int width = IconFactory.getIcon("no error").getIconWidth() + 3;
					column.setWidth(width);
					column.setPreferredWidth(width);
					break;
			}
		}

		setLayout(new BorderLayout());
		add(new JScrollPane(tblData), BorderLayout.CENTER);

		popEdit = new JPopupMenu();
		mnuEditSql = new JMenuItem("Edit SQL code", IconFactory.getIcon("query"));
		mnuClearList = new JMenuItem("Clear list");
		popEdit.add(mnuEditSql);
		popEdit.add(mnuClearList);
		mnuEditSql.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] lines = tblData.getSelectedRows();
				if (lines.length == 1)
				{
					QueryTabPanel.this.owner.setCodeEditorText((String)data.get(lines[0]).get(1));
				}
			}
		});
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
					if (tblData.getSelectedRowCount() > 0)
						popEdit.show(tblData, e.getX(), e.getY());
				}
				else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					int row = tblData.rowAtPoint(e.getPoint());
					if (row != -1)
					{
						QueryTabPanel.this.owner.setCodeEditorText((String)data.get(row).get(1));
					}
				}
			}
		});
		mnuClearList.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (JOptionPane.showConfirmDialog(QueryTabPanel.this.owner, "Do you really want to clear the list?",
						EDT.APPLICATION_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				{
					data.clear();
					((QueryTableModel)tblData.getModel()).fireTableDataChanged();
				}
			}
		});
	}

	void addRow(String sql, String message)
	{
		Vector<Object> row = new Vector<Object>();
		row.add(Utils.formatDateLong(new Date()));
		row.add(sql);
		row.add(IconFactory.getIcon(message == null ? "no error" : "error"));
		row.add(message);
		data.add(row);
		((QueryTableModel)tblData.getModel()).fireTableDataChanged();
		tblData.scrollRectToVisible(new Rectangle(0, (data.size() - 1) * tblData.getRowHeight(), 100, tblData
				.getRowHeight()));
		tblData.getSelectionModel().setSelectionInterval(data.size() - 1, data.size() - 1);
	}

	private class QueryTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;

		public Vector<String> getColumnNames()
		{
			return columns;
		}

		public Vector<Vector<Object>> getData()
		{
			return data;
		}

		@Override
		public int getColumnCount()
		{
			return columns.size();
		}

		@Override
		public int getRowCount()
		{
			return data.size();
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			Vector<Object> r = data.get(row);
			return r.get(col);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex)
		{
			data.get(rowIndex).set(columnIndex, value);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			switch (columnIndex)
			{
				// case 0:
				// return Date.class;
				// case 1:
				// case 3:
				// return String.class;
				case 2:
					return Icon.class;
				default:
					return String.class;
			}
		}

		@Override
		public String getColumnName(int column)
		{
			return columns.get(column);
		}
	}
}
