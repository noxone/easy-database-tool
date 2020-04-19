package org.noxfire.edt;

import javax.swing.tree.DefaultMutableTreeNode;

abstract class AbstractExecutable
{
	protected String name;
	protected DefaultMutableTreeNode node;

	public AbstractExecutable(String name)
	{
		this.name = name;
		node = new DefaultMutableTreeNode(this);
	}

	abstract String getQueryString();

	public DefaultMutableTreeNode getTreeNode()
	{
		return node;
	}

	public String getName()
	{
		return name;
	}

	void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	boolean isSameSql(String sql)
	{
		byte[] fSqlOld = sql.getBytes();
		byte[] fSqlNew = new byte[fSqlOld.length];

		for (int i = 0; i < fSqlOld.length; ++i)
			if (fSqlOld[i] >= 32)
				fSqlNew[i] = fSqlOld[i];
			else
				fSqlNew[i] = (byte)32;
		String fSql = new String(fSqlNew);
		while (fSql.contains("  "))
			fSql = fSql.replace("  ", " ");

		byte[] oSqlOld = getQueryString().getBytes();
		byte[] oSqlNew = new byte[oSqlOld.length];

		for (int i = 0; i < oSqlOld.length; ++i)
			if (oSqlOld[i] >= 32)
				oSqlNew[i] = oSqlOld[i];
			else
				oSqlNew[i] = (byte)32;
		String oSql = new String(oSqlNew);
		while (oSql.contains("  "))
			oSql = oSql.replace("  ", " ");

		return fSql.equalsIgnoreCase(oSql);
	}
}
