package org.noxfire.edt;

import java.util.Date;

class Query extends AbstractExecutable
{
	private String sql;

	private Date lastRun;

	Query(String name)
	{
		this(name, null);
	}
	
	Query(String name, String sql)
	{
		super(name);
		setSql(sql);
	}

	@Override
	String getQueryString()
	{
		return sql;
	}

	void setSql(String sql)
	{
		this.sql = sql;
	}

	Date getLastRun()
	{
		return lastRun;
	}

	void setLastRun(Date lastRun)
	{
		this.lastRun = lastRun;
	}
}
