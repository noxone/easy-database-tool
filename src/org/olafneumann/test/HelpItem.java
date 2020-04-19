package org.olafneumann.test;

public class HelpItem
{
	public final String code;
	public final String message;
	public final String explanation;
	public final String systemAction;
	public final String programmersResponse;
	public final String sqlState;

	public HelpItem(String code, String message, String explanation, String systemAction, String programmersResponse,
			String sqlState)
	{
		super();
		this.code = code;
		this.message = message;
		this.explanation = explanation;
		this.systemAction = systemAction;
		this.programmersResponse = programmersResponse;
		this.sqlState = sqlState;
	}
}
