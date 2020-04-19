package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;

import org.noxfire.edt.gui.WheelSpinner;
import org.noxfire.edt.table.EdtTableModel;
import org.noxfire.edt.table.GeneralTableCellRenderer;
import org.noxfire.edt.table.IntegerTableCellEditor;
import org.noxfire.edt.table.StringTableCellEditor;
import org.noxfire.edt.table.TableCellEditorListener;

public class InsertRowDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	private static final int MAX_INITIAL_WIDTH = 1024;

	private boolean clickedYes = false;

	private final MainWindow owner;
	private TableCellEditorListener tcel;

	private JPanel pnlTop;
	private JSpinner spnLines;

	private JTable tblData;

	private JPanel pnlButtons;
	private JButton btnOK;
	private JButton btnCancel;

	private final Vector<Vector<Object>> initData;
	private final Vector<Vector<Object>> data;

	public InsertRowDialog(MainWindow owner, final JTable table, final EdtTableModel model,
			Vector<Vector<Object>> data, Vector<Integer> widths, TableCellEditorListener tcel, Vector<Boolean> nullable)
	{
		super(owner);
		this.owner = owner;
		this.tcel = tcel;
		setModalityType(ModalityType.APPLICATION_MODAL);

		setIconImage(Toolkit.getDefaultToolkit().getImage("gfx/logo.gif"));
		setTitle(EDT.APPLICATION_TITLE + " - New rows...");
		int width = 2;
		for (int w : widths)
			width += w;
		if (width > MAX_INITIAL_WIDTH)
			width = MAX_INITIAL_WIDTH;
		// setPreferredSize(new Dimension(width, 480));

		// Daten einrichen
		if (data != null)
		{
			this.data = data;
			initData = new Vector<Vector<Object>>();
			for (int i = 0; i < data.size(); ++i)
			{
				Vector<Object> row = new Vector<Object>();
				initData.add(row);
				for (int col = 0; col < data.get(i).size(); ++col)
				{
					row.add(data.get(i).get(col));
				}
			}
		}
		else
		{
			this.data = new Vector<Vector<Object>>();
			this.data.add(model.getEmptyLine());
			this.initData = null;
		}
		model.setData(this.data);

		pnlButtons = new JPanel();
		btnOK = new JButton("OK");
		btnCancel = new JButton("Cancel");
		pnlButtons.add(btnCancel);
		pnlButtons.add(btnOK);
		setLayout(new BorderLayout());
		add(pnlButtons, BorderLayout.SOUTH);

		pnlTop = new JPanel();
		pnlTop.add(new JLabel("Number of new lines:"));
		spnLines = new WheelSpinner((initData != null && initData.size() > 0) ? initData.size() : 1, 1, 999);

		pnlTop.add(spnLines);
		add(pnlTop, BorderLayout.NORTH);

		// tblData = new JTable(data, model.getColumnNames());
		tblData = table;
		tblData.setFont(owner.getCurrentFont());
		tblData.setRowHeight(owner.edt.getIntOption(MainWindow.OPTIONS_RESULT_TABLE_ROW_HEIGHT,
				MainWindow.OPTIONS_RESULT_TABLE_ROW_HEIGHT_STANDARD));
		tblData.setModel(model);
		tblData.setShowGrid(false);
		tblData.getTableHeader().setReorderingAllowed(false);
		tblData.setRowHeight(owner.edt.getIntOption(MainWindow.OPTIONS_RESULT_TABLE_ROW_HEIGHT,
				MainWindow.OPTIONS_RESULT_TABLE_ROW_HEIGHT_STANDARD));
		tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i = 0; i < model.getColumnCount(); i++)
		{
			TableColumn column = tblData.getColumnModel().getColumn(i);
			column.setPreferredWidth(widths.get(i));

			column.setCellRenderer(new GeneralTableCellRenderer());
			if (Utils.isNumber(model.getColumnClass(i)))
				column.setCellEditor(new IntegerTableCellEditor(null, nullable.get(i)));
			else
				column.setCellEditor(new StringTableCellEditor(null, nullable.get(i)));
		}
		JScrollPane scp = new JScrollPane(tblData);
		scp.setPreferredSize(new Dimension(width, 450));
		add(scp, BorderLayout.CENTER);

		btnOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				clickedYes = true;
				String message = InsertRowDialog.this.tcel.insertRows(getData());
				if (message == null)
					setVisible(false);
				else
					JOptionPane.showMessageDialog(InsertRowDialog.this,
							"The insert statement failed. Error message is:\n" + message, EDT.APPLICATION_TITLE,
							JOptionPane.ERROR_MESSAGE);
			}
		});
		btnCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
		spnLines.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				int n = (Integer)spnLines.getValue();
				if (tblData.getRowCount() != n)
				{
					if (n > tblData.getRowCount())
					{
						for (int i = InsertRowDialog.this.data.size(); i < n; ++i)
						{
							if (InsertRowDialog.this.initData == null)
							{
								InsertRowDialog.this.data.add(model.getEmptyLine());
							}
							else
							{
								Vector<Object> row = new Vector<Object>();
								InsertRowDialog.this.data.add(row);
								for (int k = 0; k < model.getColumnCount(); ++k)
									row.add(InsertRowDialog.this.initData.get(i % initData.size()).get(k));
							}
						}
					}
					else
					{
						for (int i = InsertRowDialog.this.data.size() - 1; i >= n; --i)
						{
							InsertRowDialog.this.data.remove(i);
						}
					}
					((EdtTableModel)tblData.getModel()).fireTableDataChanged();
				}
			}
		});
		pack();
	}

	@Override
	public void setVisible(boolean b)
	{
		if (b)
		{
			setLocation(owner.getLocation().x + owner.getWidth() / 2 - getWidth() / 2, owner.getLocation().y
					+ owner.getHeight() / 2 - getHeight() / 2);
		}
		super.setVisible(b);
		if (!b)
		{
			dispose();
		}
	}

	public Vector<Vector<Object>> getData()
	{
		return data;
	}

	public boolean isClickedYes()
	{
		return clickedYes;
	}
}
