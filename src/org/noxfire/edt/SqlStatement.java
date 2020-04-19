package org.noxfire.edt;

public class SqlStatement
{
	private String select;
	private String where;
	private String groupBy;
	private String having;
	private String orderBy;

	// public static void main(String[] args)
	// {
	// String preGroupBy = null;
	// String inGroupBy = null;
	// String postGroupBy = null;
	// String code =
	// "SELECT fump, bla as 'blub' FROM blub WHERE dksdf = 'sdfsdf' AND dfgondfg = 32 order by bling GROUP bY blub ORDER by blign, blign, blub";
	// // Pattern p =
	// //
	// Pattern.compile("^(.*)([Ww][Hh][Ee][Rr][Ee])(.*)([Oo][Rr][Dd][Ee][Rr]\\s+[Bb][Yy])(.*)([Gg][Rr][Oo][Uu][Pp]\\s+[Bb][Yy])(.*)");
	// Pattern p =
	// Pattern.compile("^(.*)([Gg][Rr][Oo][Uu][Pp]\\s+[Bb][Yy])(.*)");
	// Matcher m = p.matcher(code);
	// if (m.matches())
	// {
	// // es ist ein Group By enthalten
	// // System.out.println("Groups: " + m.groupCount());
	// // for (int i = 0; i < m.groupCount(); ++i)
	// // System.out.println("Group " + i + ": " + m.group(i + 1));
	// preGroupBy = m.group(1);
	// postGroupBy = m.group(3);
	//
	// // postGroupBy aufspalten, so dass nachkommende sachen noch passen
	// System.out.println(postGroupBy);
	// p =
	// Pattern.compile("^(.*)([Hh][Aa][Vv][Ii][Nn][Gg])(.*)([Oo][Rr][Dd][Ee][Rr]\\s+[Bb][Yy])(.*)");
	// m = p.matcher(postGroupBy);
	// if (m.matches())
	// {
	// System.out.println("Groups: " + m.groupCount());
	// for (int i = 0; i < m.groupCount(); ++i)
	// System.out.println("Group " + i + ": " + m.group(i + 1));
	// }
	// else
	// System.out.println("passt nicht");
	// }
	// else
	// {
	// System.out.println("group-by nicht gefunden");
	// p = Pattern.compile("^(.*)([Oo][Rr][Dd][Ee][Rr]\\s+[Bb][Yy])(.*)");
	// m = p.matcher(code);
	// if (m.matches())
	// {}
	// }
	// }

	public SqlStatement(String code)
	{
		this(null, null, null, null, null);
		if (code.toLowerCase().contains("order by"))
		{
			orderBy = code.substring(code.toLowerCase().indexOf("order by"));
			code = code.substring(0, code.length() - orderBy.length());
			orderBy = Utils.trimNewLines(orderBy);
		}
		if (code.toLowerCase().contains("having"))
		{
			having = code.substring(code.toLowerCase().indexOf("having"));
			code = code.substring(0, code.length() - having.length());
			having = Utils.trimNewLines(having);
		}
		if (code.toLowerCase().contains("group by"))
		{
			groupBy = code.substring(code.toLowerCase().indexOf("group by"));
			code = code.substring(0, code.length() - groupBy.length());
			groupBy = Utils.trimNewLines(groupBy);
		}
		if (code.toLowerCase().contains("where"))
		{
			where = code.substring(code.toLowerCase().indexOf("where"));
			code = code.substring(0, code.length() - where.length());
			where = Utils.trimNewLines(where);
		}
		select = Utils.trimNewLines(code);

		// TODO.... codeerkennung auf Regex umstellen.... wobei das hier ja auch
		// schon ganz passabel sein sollte....
	}

	public SqlStatement(String select, String where, String groupBy, String having, String orderBy)
	{
		this.select = select;
		this.where = where;
		this.groupBy = groupBy;
		this.having = having;
		this.orderBy = orderBy;
	}

	/**
	 * @return the select
	 */
	public String getSelect()
	{
		return select + "\n";
	}

	/**
	 * @param select
	 *            the select to set
	 */
	public void setSelect(String select)
	{
		this.select = select;
	}

	/**
	 * @return the where
	 */
	public String getWhere()
	{
		return where;
	}

	/**
	 * @param where
	 *            the where to set
	 */
	public void setWhere(String where)
	{
		this.where = where;
	}

	/**
	 * @return the groupBy
	 */
	public String getGroupBy()
	{
		return groupBy;
	}

	/**
	 * @param groupBy
	 *            the groupBy to set
	 */
	public void setGroupBy(String groupBy)
	{
		this.groupBy = groupBy;
	}

	/**
	 * @return the having
	 */
	public String getHaving()
	{
		return having;
	}

	/**
	 * @param having
	 *            the having to set
	 */
	public void setHaving(String having)
	{
		this.having = having;
	}

	/**
	 * @return the orderBy
	 */
	public String getOrderBy()
	{
		return orderBy;
	}

	/**
	 * @param orderBy
	 *            the orderBy to set
	 */
	public void setOrderBy(String orderBy)
	{
		this.orderBy = orderBy;
	}

	public String getWherePrepared()
	{
		return where == null ? "" : where + "\n";
	}

	public String getGroupByPrepared()
	{
		return groupBy == null ? "" : groupBy + "\n";
	}

	public String getHavingPrepared()
	{
		return having == null ? "" : having + "\n";
	}

	public String getOrderByPrepared()
	{
		return orderBy == null ? "" : orderBy + "\n";
	}
}
