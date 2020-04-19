package org.noxfire.edt.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.noxfire.edt.QueryException;
import org.noxfire.edt.Utils;

abstract class AbstractTableCellEditor extends DefaultCellEditor
{
	private static final long serialVersionUID = 1L;

	private final TableCellEditorListener listener;

	private int editingColumn = -1;
	private int editingRow = -1;
	private Object editingCellValue = null;

	private JPopupMenu popNull;
	private JMenuItem mnuNull;
	private boolean stoppedWithNull = false;

	public AbstractTableCellEditor(final JTextField comp, final TableCellEditorListener listener, boolean nullable)
	{
		super(comp);
		this.listener = listener;

		if (nullable)
		{
			popNull = new JPopupMenu();
			mnuNull = new JMenuItem("Set NULL");
			popNull.add(mnuNull);
			mnuNull.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					stoppedWithNull = true;
					fireEditingStopped();
				}
			});
			comp.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if (e.getButton() == MouseEvent.BUTTON3)
						popNull.show(comp, e.getX(), e.getY());
				}
			});
		}

		setClickCountToStart(2);

		addCellEditorListener(new CellEditorListener()
		{
			@Override
			public void editingCanceled(ChangeEvent arg0)
			{
				editingColumn = -1;
				editingRow = 0;
				editingCellValue = null;
			}

			@Override
			public void editingStopped(ChangeEvent event)
			{
				if (listener != null)
				{
					Object nVal = listener.getTable().getValueAt(listener.getTable().getSelectedRow(),
							listener.getTable().getSelectedColumn());
					if (((editingCellValue == null || nVal == null) && editingCellValue != nVal)
							|| (editingCellValue != null && !editingCellValue.equals(nVal)))
					{
						if (!Utils.isNumber(editingCellValue)
								|| ((editingCellValue == null && nVal != null)
										|| (editingCellValue != null && nVal == null) || ((Number) editingCellValue)
										.longValue() != ((Number) nVal).longValue()))
						{
							try
							{
								if (listener.runUpdate(editingColumn, editingRow, editingCellValue))
									listener.setInfoText("Update OK!", false);
								else
									throw new QueryException(listener.getUpdateWarning());
							}
							catch (QueryException e)
							{
								listener.getTable().setValueAt(editingCellValue,
										listener.getTable().convertRowIndexToView(editingRow),
										listener.getTable().convertColumnIndexToView(editingColumn));
								listener.setInfoText("Update failed: " + e.getMessage(), true);
							}
						}
					}
					editingColumn = -1;
					editingRow = -1;
				}
			}
		});
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		if (listener != null)
		{
			editingColumn = table.convertColumnIndexToModel(column);
			editingRow = table.convertRowIndexToModel(row);
			editingCellValue = value;
		}

		return getTableCellEditorComponent(value);
	}

	protected abstract Component getTableCellEditorComponent(Object value);

	@Override
	public final Object getCellEditorValue()
	{
		if (stoppedWithNull)
		{
			stoppedWithNull = false;
			return null;
		}
		else
			return getRealCellEditorValue();
	}

	public abstract Object getRealCellEditorValue();
}
