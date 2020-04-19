package org.noxfire.edt;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.Attribute;
import org.jdom.Element;

class QueryFolder implements Comparable<QueryFolder>
{
	public static final String XML_ROOT = "folder";
	public static final String XML_QUERY = "query";
	public static final String XML_NAME = "name";
	public static final String XML_LASTRUN = "lastrun";
	public static final String XML_FOLDER_NAME = "name";
	public static final String XML_SQL = "sql";

	private String name;
	private List<Query> queries;
	private DefaultMutableTreeNode node;

	QueryFolder(String name)
	{
		this.name = name;
		queries = new Vector<Query>();
		node = new DefaultMutableTreeNode(this);
	}

	void setName(String name)
	{
		this.name = name;
	}

	String getName()
	{
		return name;
	}

	void addQuery(Query query, boolean addToNode)
	{
		queries.add(query);
		if (addToNode)
			node.add(new DefaultMutableTreeNode(query));
	}

	void removeQuery(Query query)
	{
		queries.remove(query);
		for (int i = 0; i < node.getChildCount(); ++i)
		{
			DefaultMutableTreeNode dn = (DefaultMutableTreeNode)node.getChildAt(i);
			if (dn.getUserObject() == query)
			{
				node.remove(dn);
				return;
			}
		}
	}

	int getQueryCount()
	{
		return queries.size();
	}

	Element getXmlElement()
	{
		Element folder = new Element(XML_ROOT);
		folder.setAttribute(new Attribute(XML_FOLDER_NAME, name));

		for (Query query : queries)
		{
			Element q = new Element(XML_QUERY);
			if (query.getLastRun() != null)
				q.setAttribute(new Attribute(XML_LASTRUN, query.getLastRun().toString()));
			else
				q.setAttribute(new Attribute(XML_LASTRUN, ""));
			Element n = new Element(XML_NAME);
			n.addContent(query.getName());
			Element s = new Element(XML_SQL);
			s.addContent(query.getQueryString());
			q.addContent(n);
			q.addContent(s);

			folder.addContent(q);
		}

		return folder;
	}

	@SuppressWarnings("unchecked")
	static QueryFolder readXmlElement(Element root) throws DataLoadException
	{
		String name = root.getAttributeValue(XML_FOLDER_NAME);
		QueryFolder folder = new QueryFolder(name);

		for (Element q : (List<Element>)root.getChildren(XML_QUERY))
		{
			Query query = new Query(q.getChildText(XML_NAME));
			
			query.setSql(q.getChildText(XML_SQL));

			String lastrun = q.getAttributeValue(XML_LASTRUN);
			if (lastrun.trim().length() > 0)
			{
				try
				{
					query.setLastRun(DateFormat.getInstance().parse(lastrun));
				}
				catch (ParseException e)
				{
					throw new DataLoadException("Cannot parse lastrun date!");
				}
			}
			
			folder.queries.add(query);
			folder.getTreeNode().add(query.getTreeNode());
		}

		return folder;
	}

	DefaultMutableTreeNode getTreeNode()
	{
		return node;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	List<Query> getQueries()
	{
		return queries;
	}

	public int compareTo(QueryFolder o)
	{
		return name.compareToIgnoreCase(o.name);
	}
}