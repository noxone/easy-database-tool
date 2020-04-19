package org.noxfire.edt;

import java.awt.Window;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class Help
{
	private static final String RETURN_CODES = "ReturnCodes";
	private static final String CODE = "code";
	private static final String MESSAGE = "message";
	private static final String EXPLANATION = "explanation";
	private static final String SYSTEM_ACTION = "systemAction";
	private static final String PROGRAMMERS_RESPONSE = "programmersResponse";
	private static final String SQL_STATE = "sqlState";

	private static HashMap<String, Help> helps;

	static
	{
		helps = new HashMap<String, Help>();
	}

	synchronized public static Help loadHelp(String system)
	{
		system = system.toLowerCase();
		Help help = helps.get(system);
		if (help == null)
		{
			try
			{
				help = new Help(system);
				helps.put(system, help);
			}
			catch (RuntimeException re)
			{}
		}
		return help;
	}

	private String system;
	private HashMap<String, HelpItem> items;

	@SuppressWarnings("unchecked")
	private Help(String system) throws RuntimeException
	{
		this.system = system;
		this.items = new HashMap<String, HelpItem>();

		// Einstellungen laden
		SAXBuilder builder = new SAXBuilder();
		try
		{
			Document doc;
			doc = builder.build("help." + system + ".xml");
			Element root = doc.getRootElement();

			for (Element returnCode : (List<Element>)root.getChildren(RETURN_CODES))
			{
				String eCode = returnCode.getChild(CODE).getText();
				String eMessage = returnCode.getChild(MESSAGE).getText();
				String eExplanation = returnCode.getChild(EXPLANATION).getText();
				String eSystemAction = returnCode.getChild(SYSTEM_ACTION).getText();
				String eProgrammersResponse = returnCode.getChild(PROGRAMMERS_RESPONSE).getText();
				String eSqlState = returnCode.getChild(SQL_STATE).getText();

				if (eCode.startsWith("+"))
					eCode = eCode.substring(1);
				HelpItem item = new HelpItem(Integer.parseInt(eCode), eMessage, eExplanation, eSystemAction,
						eProgrammersResponse, eSqlState);
				items.put(Integer.toString(item.code), item);
			}
		}
		catch (JDOMException e)
		{
			throw new RuntimeException("Cannot load help file...");
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cannot load help file...");
		}
	}

	public String getSystem()
	{
		return system;
	}

	public static boolean tryToDisplayDB2Help(Window owner, String message)
	{
		String code;
		try
		{
			Matcher m = Pattern.compile("^.*SQLCODE: ([+-]?[0-9]+).*$").matcher(message);
			code = m.group(1);
		}
		catch (Exception e)
		{
			if(!message.contains("SQLCODE:"))
				return false;
			try
			{
				code = message.substring(message.indexOf("SQLCODE:") + "SQLCODE:".length(), message.indexOf(", "))
						.trim();
			}
			catch (Exception e2)
			{
				return false;
			}
		}
		if (code.startsWith("+"))
			code = code.substring(1);

		Help help = loadHelp("db2");
		if (help == null)
			return false;
		HelpItem item = help.items.get(code);
		new HelpDialog(owner, item, message).setVisible(true);

		return true;
	}

	public static class HelpItem
	{
		public final int code;
		public final String message;
		public final String explanation;
		public final String systemAction;
		public final String programmersResponse;
		public final String sqlState;

		public HelpItem(int code, String message, String explanation, String systemAction, String programmersResponse,
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
}
