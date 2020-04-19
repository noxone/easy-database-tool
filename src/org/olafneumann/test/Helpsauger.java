package org.olafneumann.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;

public class Helpsauger
{
	private static final String BASE_URL = "http://publib.boulder.ibm.com/cgi-bin/bookmgr/BOOKS/dsnmcj11/2.";
	private static final String TEMP_FILE_BASE = "help/2.";
	private static final String TEMP_FILE_EXT = ".html";

	/**
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		final Helpsauger hs = new Helpsauger();
		PrintStream ps = new PrintStream(new File("help.db2.xml"));
		ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		ps.println("<EdtHelp version=\"1\">");
		// if (false)
		for (int i = 1; i <= 67; ++i)
		{
			System.out.print("Processing: 2.2." + i);
			String content = hs.readFile("2." + i);
			HelpItem hi = hs.examineContent(content, false);
			ps.println("\t<ReturnCodes>\n\t\t<code>" + hi.code + "</code>\n\t\t<message><![CDATA["
					+ hi.message + "]]></message>\n\t\t<explanation><![CDATA[" + hi.explanation
					+ "]]></explanation>\n\t\t<systemAction><![CDATA[" + hi.systemAction
					+ "]]></systemAction>\n\t\t<programmersResponse><![CDATA["
					+ hi.programmersResponse + "]]></programmersResponse>\n\t\t<sqlState>"
					+ hi.sqlState + "</sqlState>\n\t</ReturnCodes>");
			System.out.println();
		}
		for (int i = 1; i <= 558; ++i)
		{
			if (i == 508)
				System.out.println();
			System.out.print("Processing: 2.3." + i);
			String content = hs.readFile("3." + i);
			HelpItem hi = hs.examineContent(content, false);
			ps.println("\t<ReturnCodes>\n\t\t<code>" + hi.code + "</code>\n\t\t<message><![CDATA["
					+ hi.message + "]]></message>\n\t\t<explanation><![CDATA[" + hi.explanation
					+ "]]></explanation>\n\t\t<systemAction><![CDATA[" + hi.systemAction
					+ "]]></systemAction>\n\t\t<programmersResponse><![CDATA["
					+ hi.programmersResponse + "]]></programmersResponse>\n\t\t<sqlState>"
					+ hi.sqlState + "</sqlState>\n\t</ReturnCodes>");
			System.out.println();
		}
		ps.println("</EdtHelp>");
		ps.close();
		// String blub = hs.readFile("3.75");
		// hs.examineContent(blub, true);
		System.out.println("Done!");
	}

	private HelpItem examineContent(String content, boolean verbose) throws IOException
	{
		// System.out.println(content.substring(content.indexOf(
		// "</pre><pre><!-- * -->",
		// content.indexOf("<h6>"))));
		System.out.print(" - code - message");
		String blub = content.substring(content.indexOf("<pre><!-- * -->"), content.length());
		String string = blub.substring(0, blub.indexOf("</pre><!-- *CZ FLOW** -->"));
		String code = string.substring(string.indexOf("<B>") + 3, string.indexOf("</B>"));
		if (verbose)
			System.out.println(code);
		String message = string.substring(string.indexOf("</B>") + 4, string.indexOf("<p>"))
				.replaceAll("\\<.*?>", "").trim();
		message = message.replace("\n", " ");
		for (int i = 0; i < 32; ++i)
			message = message.replace("" + (char) i, "");
		while (message.indexOf("  ") != -1)
			message = message.replace("  ", " ");
		string = content.substring(content.indexOf("</pre><!-- CZoff or PRset -->"));

		String explanation2 = "";
		if (string.indexOf("Response:</B>") != -1)
		{
			System.out.print(" - response");
			string = string.substring(string.indexOf("Response:</B>"));
			explanation2 = string.substring(string.indexOf("</B>"), string.indexOf("<B>SQLSTATE"))
					.replaceAll("\\<.*?>", "").trim();
			while (explanation2.indexOf("  ") != -1)
				explanation2 = explanation2.replace("  ", " ");
			explanation2 = explanation2.replace("\n ", "\n").replaceAll("([^\\n])\\n([^\\n])", "$1 $2")
					.replaceAll(":\\n\\n\\n([^\\n])", ":\n - $1");
			for (int i = 0; i < 20;++i)
				explanation2 = explanation2.replaceAll("( - [^\\n]+)\\n\\n([^\\n])", "$1\n - $2");
			explanation2 = explanation2.replaceAll("\\n\\n", "\n");
		}

		String response2 = "";
		if (string.indexOf("Response:</B>") != -1)
		{
			System.out.print(" - response");
			string = string.substring(string.indexOf("Response:</B>"));
			response2 = string.substring(string.indexOf("</B>"), string.indexOf("<B>SQLSTATE"))
					.replaceAll("\\<.*?>", "").trim();
			while (response2.indexOf("  ") != -1)
				response2 = response2.replace("  ", " ");
			response2 = response2.replace("\n ", "\n").replaceAll("([^\\n])\\n([^\\n])", "$1 $2")
					.replaceAll(":\\n\\n\\n([^\\n])", ":\n - $1");
			for (int i = 0; i < 20;++i)
				response2 = response2.replaceAll("( - [^\\n]+)\\n\\n([^\\n])", "$1\n - $2");
			response2 = response2.replaceAll("\\n\\n", "\n");
		}

		System.out.print(" - state");
		string = string.substring(string.indexOf("SQLSTATE:</B>"), string.length());
		String state2 = string.substring(string.indexOf("</B>"), string.indexOf("<hr>"))
				.replaceAll("\\<.*?>", "").trim();

		if (true)
			return new HelpItem(code, message, "", "", response2, state2);

		int pExpl = string.indexOf("<B>Explanation:</B>");
		int pActi = string.indexOf("<B>Action:</B>");
		int pResp = string.indexOf("<B>Response:</B>");
		int pStat = string.indexOf("<B>SQLSTATE:</B>");

		String explanation = string.substring(string.indexOf("</B>"), string.indexOf("<B>System"))
				.replaceAll("\\<.*?>", "").trim();
		if (verbose)
			System.out.println(explanation);

		System.out.print(" - action");
		string = string.substring(string.indexOf("Action:</B>"));
		String action = string.substring(string.indexOf("</B>"), string.indexOf("<B>Response"));
		action = action.substring(0, action.lastIndexOf("<B>")).replaceAll("\\<.*?>", "").trim();
		if (verbose)
			System.out.println(action);

		System.out.print(" - response");
		string = string.substring(string.indexOf("Response:</B>"));
		String response = string.substring(string.indexOf("</B>"), string.indexOf("<B>SQLSTATE"))
				.replaceAll("\\<.*?>", "").trim();
		if (verbose)
			System.out.println(response);

		System.out.print(" - state");
		string = string.substring(string.indexOf("SQLSTATE:</B>"), string.length());
		String state = string.substring(string.indexOf("</B>"), string.indexOf("<hr>")).replaceAll(
				"\\<.*?>", "").trim();
		if (verbose)
			System.out.println(state);

		return new HelpItem(code, message, explanation, action, response, state);
	}

	private String readFile(String ending) throws IOException
	{
		String filename = TEMP_FILE_BASE + ending + TEMP_FILE_EXT;

		byte[] file = new byte[1024];
		int filePos = 0;

		InputStream in = new BufferedInputStream(new FileInputStream(filename));

		int b;
		while ((b = in.read()) != -1)
		{
			file[filePos++] = (byte) b;
			if (filePos >= file.length)
			{
				byte[] blub = new byte[file.length * 2];
				System.arraycopy(file, 0, blub, 0, file.length);
				file = blub;
			}
		}
		in.close();

		String back = new String(file, 0, filePos).replace("|", "").replace("#", "");
		back = back.substring(back.indexOf("<h6> 2." + ending), back.indexOf("©"));
		return back;
	}

	@SuppressWarnings("unused")
	private String readFileHttp(String ending) throws IOException
	{
		byte[] file = new byte[1024];
		int filePos = 0;

		// System.out.println("Reading URL: " + BASE_URL + ending);
		URL src = new URL(BASE_URL + ending);
		InputStream in = new BufferedInputStream(src.openStream());

		int b;
		while ((b = in.read()) != -1)
		{
			file[filePos++] = (byte) b;
			if (filePos >= file.length)
			{
				byte[] blub = new byte[file.length * 2];
				System.arraycopy(file, 0, blub, 0, file.length);
				file = blub;
			}
		}
		in.close();

		String back = new String(file, 0, filePos);
		return back;
	}
}
