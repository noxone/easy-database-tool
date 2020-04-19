package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class QueryBuilderDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private final MainWindow owner;

	private JPanel pnlTop;
	private JPanel pnlButtons;
	private JPanel pnlBottom;

	private JLabel lblInfo;
	private JComboBox cmbOrderBy;
	private JTable tblData;
	private JScrollPane sclData;

	private JButton btnSelectAll;
	private JButton btnOK;
	private JButton btnCancel;

	private final Vector<String> columnNames;
	private final Vector<Vector<Object>> data;

	private boolean select = false;

	private boolean okPressed;

	public QueryBuilderDialog(MainWindow owner)
	{
		super(owner);
		this.owner = owner;
		setModalityType(ModalityType.APPLICATION_MODAL);

		setIconImage(Toolkit.getDefaultToolkit().getImage("gfx/logo.gif"));
		setTitle(EDT.APPLICATION_TITLE + " - Query Builder");

		columnNames = new Vector<String>();
		columnNames.add("Show");
		columnNames.add("Column");
		columnNames.add("A/D");
		columnNames.add("Order");
		columnNames.add("WHERE");
		data = new Vector<Vector<Object>>();

		cmbOrderBy = new JComboBox(OrderBy.values());

		pnlButtons = new JPanel();
		pnlBottom = new JPanel();

		lblInfo = new JLabel();
		btnSelectAll = new JButton("Unselect all");
		btnOK = new JButton("Generate code");
		btnCancel = new JButton("Cancel");
		pnlButtons.setLayout(new FlowLayout());
		pnlButtons.add(btnCancel);
		pnlButtons.add(btnOK);
		pnlBottom.setLayout(new BorderLayout());
		pnlBottom.add(pnlButtons, BorderLayout.EAST);
		pnlTop = new JPanel();
		pnlTop.add(lblInfo, BorderLayout.WEST);

		JPanel panel = new JPanel();
		panel.add(btnSelectAll);
		pnlBottom.add(panel, BorderLayout.WEST);

		setLayout(new BorderLayout());
		add(pnlTop, BorderLayout.NORTH);
		add(pnlBottom, BorderLayout.SOUTH);

		tblData = new JTable(new DefaultTableModel(data, columnNames)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				switch (columnIndex)
				{
					case 0:
						return Boolean.class;
					case 2:
						return OrderBy.class;
					case 3:
						return Integer.class;
					default:
						return String.class;
				}
			}

			@Override
			public boolean isCellEditable(int row, int column)
			{
				if (column == 1)
					return false;
				else if (column == 3)
					return data.get(row).get(2) != OrderBy.NIX;
				else
					return true;
			}
		});
		tblData.setRowHeight(owner.edt.getIntOption(MainWindow.OPTIONS_RESULT_TABLE_ROW_HEIGHT, MainWindow.OPTIONS_RESULT_TABLE_ROW_HEIGHT_STANDARD));
		tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblData.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		// Tabelle einrichten
		for (int i = 0; i < tblData.getColumnCount(); i++)
		{
			TableColumn column = tblData.getColumnModel().getColumn(i);

			switch (i)
			{
				case 0:
					column.setPreferredWidth(20);
					break;
				case 1:
					column.setPreferredWidth(100);
					break;
				case 2:
					int mw = 20;
					int cw = 0;
					for (OrderBy ob : OrderBy.values())
					{
						cw = SwingUtilities.computeStringWidth(tblData.getFontMetrics(tblData.getFont()), ob.text);
						mw = (cw > mw) ? cw : mw;
					}
					column.setPreferredWidth(mw + 5);
					column.setCellEditor(new DefaultCellEditor(cmbOrderBy)
					{
						private static final long serialVersionUID = 1L;

						private Integer lastRow = null;

						@Override
						public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
								int row, int column)
						{
							lastRow = row;
							return super.getTableCellEditorComponent(table, value, isSelected, row, column);
						}

						@Override
						public Object getCellEditorValue()
						{
							Object newVal = super.getCellEditorValue();
							if (lastRow != null)
							{
								if (newVal != OrderBy.NIX)
								{
									if (data.get(lastRow).get(2) == OrderBy.NIX)
										data.get(lastRow).set(3, 0);
								}
								else
									data.get(lastRow).set(3, null);
								((DefaultTableModel) tblData.getModel()).fireTableCellUpdated(lastRow, 3);
							}
							lastRow = null;
							return newVal;
						}
					});
					break;
				case 3:
					column.setPreferredWidth(50);
					break;
				case 4:
					column.setPreferredWidth(400);
					break;
			}
		}
		sclData = new JScrollPane(tblData);
		add(sclData, BorderLayout.CENTER);

		btnOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				okPressed = true;
				setVisible(false);
			}
		});
		btnCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				okPressed = false;
				setVisible(false);
			}
		});
		btnSelectAll.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (Vector<Object> line : data)
					line.set(0, select);
				((DefaultTableModel) tblData.getModel()).fireTableDataChanged();
				select = !select;
				btnSelectAll.setText(select ? "Select all" : "Unselect all");
			}
		});
		pack();
	}

	public String showForTable(Table table)
	{
		lblInfo.setText("<html>Please enter the code for table '<b>" + table.name + "</b>': (" + table.columns.size()
				+ " column" + (table.columns.size() == 1 ? "" : "s") + ")</html>");
		int maxWidth = 100;
		data.clear();
		for (Table.Column column : table.columns)
		{
			Vector<Object> line = new Vector<Object>();
			line.add(Boolean.TRUE);
			line.add(column.name);
			line.add(OrderBy.NIX);
			line.add(null);
			line.add("");
			data.add(line);

			int curWidth = SwingUtilities.computeStringWidth(tblData.getFontMetrics(tblData.getFont()), column.name);
			maxWidth = (curWidth > maxWidth) ? curWidth : maxWidth;
		}
		tblData.getColumnModel().getColumn(1).setPreferredWidth(maxWidth + 20);
		((DefaultTableModel) tblData.getModel()).fireTableDataChanged();
		sclData.setPreferredSize(new Dimension((int) (tblData.getPreferredSize().getWidth() + 30), (data.size())
				* tblData.getRowHeight() + tblData.getTableHeader().getHeight() + 4));
		pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (getHeight() > screenSize.height - 100)
			setSize(getWidth(), screenSize.height - 100);

		if (owner != null)
			setLocation(owner.getX() + owner.getWidth() / 2 - getWidth() / 2, owner.getY() + owner.getHeight() / 2
					- getHeight() / 2);
		setVisible(true);

		boolean done;
		String code;
		if (okPressed)
		{
			done = true;
			for (Vector<Object> line : data)
				done = done && (Boolean) line.get(0);
			if (done)
				code = "SELECT *";
			else
			{
				done = false;
				code = "SELECT ";
				for (Vector<Object> line : data)
				{
					if ((Boolean) line.get(0))
					{
						code += (!done ? "" : ", ") + line.get(1);
						done = true;
					}
				}
			}
			code += "\nFROM " + table.getTable();

			// WHERE
			done = false;
			for (Vector<Object> line : data)
				// testen, ob berhaupt eine Condition angegeben wurde
				if (((String) line.get(4)).trim().length() > 0)
					done = true;
			if (done)
			{
				// es wurde mindestens eine Condition angegeben
				code += "\nWHERE";
				for (Vector<Object> line : data)
					if (((String) line.get(4)).trim().length() > 0)
					{
						String where = (String) line.get(4);
						if (!(where.toUpperCase().contains("OR") || where.toUpperCase().contains("AND")))
							code += "\n     " + line.get(1) + " " + where;
						else
						{
							// TODO
						}
					}
			}

			// ORDER BY
			done = false;
			int orders = 0, counter = 0;
			for (Vector<Object> line : data)
				if (line.get(2) != OrderBy.NIX)
					++orders;
			if (orders > 0)
				code += "\nORDER BY";
			while (orders > 0)
			{
				for (Vector<Object> line : data)
					if (line.get(3) != null && ((Integer) line.get(3)).equals(counter))
					{
						code += (done ? "," : "") + "\n     " + line.get(1) + " " + line.get(2);
						--orders;
						done = true;
					}
				++counter;
			}
			return code;
		}
		else
			return null;
	}

	public static enum OrderBy
	{
		NIX(""), ASCENDING("Asc"), DESCENDING("Desc");

		public final String text;

		private OrderBy(String text)
		{
			this.text = text;
		}

		@Override
		public String toString()
		{
			return text;
		}
	}
}
