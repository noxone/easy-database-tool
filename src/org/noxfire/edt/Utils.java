package org.noxfire.edt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

public final class Utils
{
	private Utils()
	{}

	public static String formatDateShort(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR) + "-" + fillString(cal.get(Calendar.MONTH) + 1, 2, '0', false) + "-"
				+ fillString(cal.get(Calendar.DAY_OF_MONTH), 2, '0', false);
	}

	public static String formatDateLong(Date date)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR) + "-" + fillString(cal.get(Calendar.MONTH) + 1, 2, '0', false) + "-"
				+ fillString(cal.get(Calendar.DAY_OF_MONTH), 2, '0', false) + " "
				+ fillString(cal.get(Calendar.HOUR_OF_DAY), 2, '0', false) + ":"
				+ fillString(cal.get(Calendar.MINUTE), 2, '0', false) + ":"
				+ fillString(cal.get(Calendar.SECOND), 2, '0', false);

	}

	public static String fillString(String text, int length, char fillchar, boolean fillAtEnd)
	{
		if (text.length() >= length)
			return text;
		else
		{
			char[] zeichen = new char[length - text.length()];
			for (int i = 0; i < zeichen.length; ++i)
				zeichen[i] = fillchar;
			if (fillAtEnd)
				return text + new String(zeichen);
			else
				return new String(zeichen) + text;
		}
	}

	static DefaultMutableTreeNode findExecutable(List<? extends AbstractExecutable> executables,
			DefaultMutableTreeNode start, String text)
	{
		if (executables == null)
			throw new NullPointerException("tables list is null. Cannot search on it!");

		text = text.toUpperCase();

		int i = 0;

		// Start suchen
		if (start != null)
			for (; i < executables.size(); ++i)
				if (executables.get(i).getTreeNode() == start)
				{
					++i;
					break;
				}

		for (; i < executables.size(); ++i)
			if (executables.get(i).getName().toUpperCase().contains(text))
				return executables.get(i).getTreeNode();

		return null;
	}

	public static String fillString(int number, int length, char fillchar, boolean fillAtEnd)
	{
		return fillString(Integer.toString(number), length, fillchar, fillAtEnd);
	}

	public static boolean isNumber(Class<?> clazz)
	{
		return clazz.equals(Integer.class) || clazz.equals(Long.class) || clazz.equals(Short.class)
				|| clazz.equals(Byte.class);
	}

	public static boolean isNumber(Object value)
	{
		return value == null || isNumber(value.getClass());
	}

	public static JPanel createMenuLabel(String text)
	{
		return createMenuLabel(text, null);
	}

	public static JPanel createMenuLabel(String text, Color background)
	{
		JLabel label;
		label = new JLabel(text);
		// label.setAlignmentX(0.5f);
		label.setFont(label.getFont().deriveFont(Font.ITALIC | Font.BOLD));
		label.setPreferredSize(new Dimension(SwingUtilities.computeStringWidth(label.getFontMetrics(label.getFont()),
				label.getText()) + 5, 22));

		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.add(label);
		if (background != null)
			panel.setBackground(background);
		return panel;
	}

	public static String trimNewLines(String s)
	{
		while (s.startsWith("\n"))
			s = s.substring(1);
		while (s.endsWith("\n"))
			s = s.substring(0, s.length() - 1);
		return s;
	}

	public static boolean copyFile(String source, String dest)
	{
		InputStream in;
		OutputStream out;
		try
		{
			in = new BufferedInputStream(new FileInputStream(source));
			out = new BufferedOutputStream(new FileOutputStream(dest));
		}
		catch (FileNotFoundException e)
		{
			return false;
		}

		int c;
		try
		{
			while ((c = in.read()) != -1)
				out.write(c);
		}
		catch (IOException e)
		{
			return false;
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{}
			try
			{
				out.close();
			}
			catch (IOException e)
			{}
		}
		return true;
	}
}
