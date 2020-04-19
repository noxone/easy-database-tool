package org.noxfire.edt;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

final class IconFactory
{
	static Map<String, ImageIcon> icons;

	private IconFactory()
	{}

	static
	{
		icons = new HashMap<String, ImageIcon>();

		addIcon("logo", "logo.gif");
		addIcon("table", "table.gif");
		addIcon("table editable", "table_editable.gif");
		addIcon("table not editable", "table_not_editable.gif");
		addIcon("multiple tables", "multiple_tables.gif");
		addIcon("column", "column.gif");
		addIcon("addfav", "addfav.gif");
		addIcon("remfav", "remfav.gif");
		addIcon("addfavfol", "addfavfol.gif");
		addIcon("remfavfol", "remfavfol.gif");
		addIcon("back", "back.gif");
		addIcon("forward", "forward.gif");
		addIcon("run", "run.gif");
		addIcon("actions", "actions.gif");
		addIcon("folder", "folder.gif");
		addIcon("folder open", "folder_open.gif");
		addIcon("query", "query.gif");
		addIcon("folder big", "folder_big.gif");
		addIcon("query big", "query_big.gif");
		addIcon("external window", "external.gif");
		addIcon("xml", "xml.gif");
		addIcon("csv", "csv.gif");
		addIcon("error", "error.gif");
		addIcon("no error", "no_error.gif");
		addIcon("copy", "copy.gif");
		addIcon("paste", "paste.gif");
		addIcon("cut", "cut.gif");
		addIcon("save", "save.gif");
		addIcon("key", "key.gif");
	}

	private static void addIcon(String title, String filename)
	{
		final String folder = "gfx/";
		icons.put(title.toUpperCase(), new ImageIcon(folder + filename));
	}

	static ImageIcon getIcon(String title)
	{
		if (title == null)
			return null;

		title = title.toUpperCase();

		ImageIcon icon = icons.get(title);

		if (icon == null)
			throw new NullPointerException("Cannot find Icon namen '" + title + "'");
		return icon;
	}
}
