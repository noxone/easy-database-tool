package org.noxfire.edt.table;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.noxfire.edt.Utils;
import org.noxfire.edt.QueryResultTabPanel.ColumnHeaderToolTips;

public class EdtTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;

	private final JTable table;

	private final Vector<String> names;
	private Vector<Vector<Object>> data;
	private final Vector<Class<?>> classes;
	private final int columnCount;
	private final boolean mayEditColumns;
	private final Vector<Boolean> nullable;

	private final boolean[] columnVisible;
	private final int[] widths;
	private final Object[] headers;
	private final String[] tooltips;

	public EdtTableModel(JTable table, Vector<String> columnNames, Vector<Vector<Object>> data,
			Vector<Class<?>> classes, Vector<Boolean> nullable, int columnCount, boolean editColumns)
	{
		this.table = table;
		this.names = columnNames;
		this.data = data;
		this.columnCount = columnCount;
		this.classes = classes;
		this.mayEditColumns = editColumns;
		this.nullable = nullable;

		columnVisible = new boolean[names.size()];
		for (int i = 0; i < columnVisible.length; ++i)
			columnVisible[i] = true;
		widths = new int[names.size()];
		headers = new Object[names.size()];
		tooltips = new String[names.size()];
	}

	public int convertToRealColumnColumnNumber(int col)
	{
		int n = col;
		int i = 0;
		do
		{
			if (!(columnVisible[i]))
				++n;
			++i;
		}
		while (i < n);
		while (!(columnVisible[n]))
			++n;
		return n;
	}

	public Vector<String> getColumnNames()
	{
		return names;
	}

	public Vector<Vector<Object>> getData()
	{
		return data;
	}

	public void setData(Vector<Vector<Object>> data)
	{
		this.data = data;
	}

	public Vector<Object> getEmptyLine()
	{
		Vector<Object> row = new Vector<Object>();
		for (int i = 0; i < columnCount; ++i)
		{
			if (nullable.get(i))
				row.add(null);
			else
			{
				if (Utils.isNumber(classes.get(i)))
					row.add(0L);
				else
					try
					{
						row.add(classes.get(i).newInstance());
					}
					catch (Exception e)
					{
						row.add(null);
					}
			}
		}
		return row;
	}

	public EdtTableModel clone(JTable newTable)
	{
		return new EdtTableModel(newTable, names, data, classes, nullable, columnCount, true);
	}

	@Override
	public int getColumnCount()
	{
		int n = 0;
		for (boolean b : columnVisible)
			if (!b)
				++n;
		return columnCount - n;
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
		return r.get(convertToRealColumnColumnNumber(col));
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return mayEditColumns;
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex)
	{
		data.get(rowIndex).set(convertToRealColumnColumnNumber(columnIndex), value);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return classes.get(convertToRealColumnColumnNumber(columnIndex));
	}

	@Override
	public String getColumnName(int column)
	{
		return names.get(convertToRealColumnColumnNumber(column));
	}

	public void setColumnVisibility(int column, boolean visible, ColumnHeaderToolTips tipData)
	{
		// alte Spaltendaten speichern
		for (int i = 0; i < table.getColumnCount(); ++i)
		{
			int realColumn = convertToRealColumnColumnNumber(i);
			TableColumn col = table.getColumnModel().getColumn(i);
			widths[realColumn] = col.getWidth();
			headers[realColumn] = col.getHeaderValue();
			tooltips[realColumn] = tipData.setToolTip(col, null);
		}

		// Tabellenstruktur verändern
		columnVisible[column] = visible;
		fireTableStructureChanged();

		// alte Spaltendaten wiederherstellen
		for (int i = 0; i < table.getColumnCount(); ++i)
		{
			int realColumn = convertToRealColumnColumnNumber(i);
			TableColumn col = table.getColumnModel().getColumn(i);
			col.setPreferredWidth(widths[convertToRealColumnColumnNumber(i)]);
			col.setHeaderValue(headers[realColumn]);
			tipData.setToolTip(col, tooltips[realColumn]);
		}
	}
	
	public boolean isRealColumnVisible(int column)
	{
		return columnVisible[column];
	}
}
