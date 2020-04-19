package org.noxfire.edt;

import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

class Table extends AbstractExecutable
{
	public final List<Column> columns;

	private final String table;

	Table(String name, String tableName)
	{
		super(name);
		this.name = name;
		this.table = tableName;
		columns = new Vector<Column>();
	}
	
	public String getTable()
	{
		return table;
	}

	void addColumn(Column column)
	{
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(column);

		columns.add(column);
		getTreeNode().add(node);
	}

	void addColumn(String name, boolean isKey, String type)
	{
		addColumn(new Column(name, isKey, type));
	}

	void addColumn(String name)
	{
		addColumn(name, false, null);
	}

	static class Column
	{
		public final String name;
		public final boolean isKey;
		public final String type;

		Column(String name, boolean isKey, String type)
		{
			this.name = name;
			this.isKey = isKey;
			this.type = type;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	@Override
	String getQueryString()
	{
		return "SELECT * FROM " + table;
	}
}
