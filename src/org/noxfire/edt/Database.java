package org.noxfire.edt;

import org.jdom.Attribute;
import org.jdom.Element;

class Database
{
	public static final String XML_ROOT = "databaseConfig";
	public static final String XML_NAME = "name";
	public static final String XML_JDBC_PREFIX = "jdbcPrefix";
	public static final String XML_SERVER = "server";
	public static final String XML_PORT = "port";
	public static final String XML_DATABASE = "database";
	public static final String XML_DRIVER = "driverClassName";

	public static final String XML_FIND_TABLES = "tablesQuery";
	public static final String XML_FIND_COLUMNS = "columnsQuery";
	public static final String XML_FIND_KEY_COLUMNS = "keyColumnsQuery";
	public static final String XML_ATT_TABLE_NAME = "tablename";
	public static final String XML_ATT_OBJECT_NAME = "objectname";
	public static final String XML_ATT_COLUMN_NAME = "columnname";
	public static final String XML_ATT_READ_COLUMNS_AT_START = "readColumnsAtStart";

	public final String name;

	public final String jdbcPrefix;
	public final String server;
	public final int port;
	public final String database;

	public final String driverClassName;

	public final String findTablesQuery;
	public final String findColumnsQuery;
	public final String findKeyColumnsQuery;

	public final String tableColumnName;
	public final String objectColumnName;
	public final String columnColumnName;

	public final boolean readColumnAtStart;

	Database(String name, String jdbcPrefix, String server, int port, String database, String driverClassName,
			String findTablesQuery, String findColumnsQuery, String findKeyColumnsQuery, String tableColumnName,
			String objectColumnName, String columnColumnName, boolean readColumnsAtStart)
	{
		this.name = name;
		this.jdbcPrefix = jdbcPrefix;
		this.server = server;
		this.port = port;
		this.database = database;
		this.driverClassName = driverClassName;
		this.findTablesQuery = findTablesQuery;
		this.findColumnsQuery = findColumnsQuery;
		this.findKeyColumnsQuery = findKeyColumnsQuery;
		this.tableColumnName = tableColumnName;
		this.objectColumnName = objectColumnName;
		this.columnColumnName = columnColumnName;
		this.readColumnAtStart = readColumnsAtStart;
	}

	Element getXmlElement()
	{
		Element r = new Element(XML_ROOT);
		Element j = new Element(XML_JDBC_PREFIX);
		j.addContent(jdbcPrefix);
		Element s = new Element(XML_SERVER);
		s.addContent(server);
		Element p = new Element(XML_PORT);
		p.addContent(Integer.toString(port));
		Element d = new Element(XML_DATABASE);
		d.addContent(database);
		Element c = new Element(XML_DRIVER);
		c.addContent(driverClassName);
		Element n = new Element(XML_NAME);
		n.addContent(name);

		Element sqlT = new Element(XML_FIND_TABLES);
		sqlT.addContent(findTablesQuery);
		sqlT.setAttribute(new Attribute(XML_ATT_TABLE_NAME, tableColumnName));
		if (objectColumnName != null)
			sqlT.setAttribute(new Attribute(XML_ATT_OBJECT_NAME, objectColumnName));
		Element sqlC = null;
		if (findColumnsQuery != null)
		{
			sqlC = new Element(XML_FIND_COLUMNS);
			sqlC.addContent(findColumnsQuery);
			sqlC.setAttribute(new Attribute(XML_ATT_READ_COLUMNS_AT_START, Boolean.toString(readColumnAtStart)));
		}
		Element sqlK = new Element(XML_FIND_KEY_COLUMNS);
		sqlK.addContent(findKeyColumnsQuery);
		sqlK.setAttribute(new Attribute(XML_ATT_COLUMN_NAME, columnColumnName));

		r.addContent(n);
		r.addContent(j);
		r.addContent(s);
		r.addContent(p);
		r.addContent(d);
		r.addContent(c);
		r.addContent(sqlT);
		if (sqlC != null)
			r.addContent(sqlC);
		r.addContent(sqlK);

		return r;
	}

	static Database readXmlElement(Element e)
	{
		Element name = e.getChild(XML_NAME);
		Element prefix = e.getChild(XML_JDBC_PREFIX);
		Element server = e.getChild(XML_SERVER);
		Element port = e.getChild(XML_PORT);
		Element database = e.getChild(XML_DATABASE);
		Element driver = e.getChild(XML_DRIVER);

		Element table = e.getChild(XML_FIND_TABLES);
		Element column = e.getChild(XML_FIND_COLUMNS);
		Element key = e.getChild(XML_FIND_KEY_COLUMNS);

		Attribute tab = table.getAttribute(XML_ATT_TABLE_NAME);
		Attribute obj = table.getAttribute(XML_ATT_OBJECT_NAME);
		Attribute col = key.getAttribute(XML_ATT_COLUMN_NAME);
		Attribute rcas = column.getAttribute(XML_ATT_READ_COLUMNS_AT_START);

		return new Database(name.getText(), prefix.getText(), server.getText(), Integer.parseInt(port.getText()),
				database.getText(), driver.getText(), table.getText(), column != null ? column.getText() : null, key
						.getText(), tab.getValue(), obj != null ? obj.getValue() : null, col.getValue(),
				rcas != null ? rcas.getValue().equalsIgnoreCase("true") : false);
	}

	@Override
	public String toString()
	{
		return name;
	}
}
