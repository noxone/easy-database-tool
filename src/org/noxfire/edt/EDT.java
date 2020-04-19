package org.noxfire.edt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.noxfire.edt.LoginWindow.Action;

public class EDT
{
	public static final String XML_ROOT = "edtSettings";
	public static final String XML_ROOT_VERSION = "version";
	public static final String XML_ROOT_VERSION_1 = "1.0";
	public static final String XML_ROOT_VERSION_2 = "2.0";
	public static final String XML_ROOT_PART = "part";
	public static final String XML_ROOT_PART_MAIN = "main";
	public static final String XML_ROOT_PART_FAVOURITES = "favourites";
	public static final String XML_ROOT_PART_HISTORY = "history";
	public static final String XML_FAVOURITES = "favourites";
	public static final String XML_HISTORY = "history";
	public static final String XML_OPTIONS = "options";
	public static final String XML_OPTIONS_ITEM = "option";
	public static final String XML_OPTIONS_NAME = "name";

	public static final String APPLICATION_TITLE = "Easy Database Tool";
	public static final String APPLICATION_VERSION = "1.0";
	public static final String currentDateString = Utils.formatDateShort(new Date());
	private static final String DATA_FILE_SETTINGS = "edt.xml";
	private static final String DATA_FILE_SETTINGS_FAILSAFE = "edt.failsafe.xml";
	private static final String DATA_FILE_FAVOURITES = "favourites.edt.xml";
	private static final String DATA_FILE_FAVOURITES_FAILSAFE = "favourites.edt.failsafe.xml";
	private static final String DATA_FILE_HISTORY = "history.edt.xml";
	private static final String DATA_FILE_HISTORY_FAILSAFE = "history.edt.failsafe.xml";

	private Vector<QueryFolder> favourites;
	private Vector<QueryFolder> history;
	private Vector<Database> databases;
	private DBConnection connection;

	/**
	 * beinhaltet das Statusfenster, über das der User mit Informationen
	 * versorgt werden kann...
	 */
	StatusInfoWindow statInfo;

	/**
	 * enthält die Optionen
	 */
	private HashMap<String, String> options;

	/**
	 * Initialisiert sämtliche Variablen dieser Klasse
	 */
	private EDT()
	{
		favourites = new Vector<QueryFolder>();
		history = new Vector<QueryFolder>();
		databases = new Vector<Database>();
		options = new HashMap<String, String>();

		statInfo = new StatusInfoWindow();
	}

	/**
	 * Programmeinstiegspunkt
	 * 
	 * @param args
	 *            Kommandozeilenparameter
	 */
	public static void main(String[] args)
	{
		new EDT().startApplication();
	}

	/**
	 * Führt alle ladenden Befehle des Programms aus, führt die Login-Logik aus
	 * und öffnet das Hauptfenster. Außerdem wird das Look-and-Feel auf das
	 * System-LaF gesetzt und Shutdown-Hooks werden installiert, um das Programm
	 * korrekt zu beenden.
	 */
	private void startApplication()
	{
		// Dünnsinn
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{}

		String loadSettingsMessage = loadSettings(DATA_FILE_SETTINGS, false);
		if (loadSettingsMessage != null)
		{
			JOptionPane.showMessageDialog(null,
					"Unable to load the settings. This application will quit now!\nThe error message is:\n"
							+ loadSettingsMessage, EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Das Programm soll auch ordnungsgemäß beendet werden....
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				stopApplication();
			}
		});

		// Login?
		LoginWindow login = new LoginWindow(databases);
		if (login.showAndWait() == Action.OK)
		{
			// zeigen, dass wir was tun
			statInfo.showInfoMessage("Connecting to database '" + login.getDatabase().name + "'...");
			// Login-Daten holen
			connection = new DBConnection();
			databases.remove(login.getDatabase());
			databases.insertElementAt(login.getDatabase(), 0);
			// einloggen
			if (connection.connect(login.getDatabase(), login.getUsername(), login.getPassword()))
			{
				// Hauptfenster
				MainWindow window = new MainWindow(this, connection);
				window.setVisible(true);
			}
			else
			{
				// schade... ging wohl nicht... melden und quit
				JOptionPane.showMessageDialog(null, "Cannot load the application.\nError is: "
						+ connection.getLastError() + "\nThis application will quit now!", APPLICATION_TITLE,
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			statInfo.setVisible(false);
		}
	}

	/**
	 * Führt alle Aktionen durch, die beim Beenden des Programm nötig sind. Dazu
	 * gehört z.B. auch das Speichern der Einstellungen
	 */
	private void stopApplication()
	{
		safeSettingsVersion1();
	}

	/**
	 * Lädt die Einstellungen
	 * 
	 * @return null, wenn erfolgreich; sont Fehlermeldung
	 */
	private String loadSettings(String filename, boolean failsafe)
	{
		SAXBuilder builder = new SAXBuilder();
		try
		{
			Document doc = builder.build(filename);
			Element root = doc.getRootElement();
			String version = XML_ROOT_VERSION_1;
			Attribute attVersion = root.getAttribute(XML_ROOT_VERSION);
			if (attVersion == null)
				JOptionPane
						.showMessageDialog(null,
								"Version attribute missing in file 'edt.xml'. Assuming file 'edt.xml' to be version 1.0 of EDT data file!");
			else
				version = attVersion.getValue();

			if (XML_ROOT_VERSION_1.equals(version))
				loadSettingsVersion1(root);
			else if (XML_ROOT_VERSION_2.equals(version))
				loadSettingsVersion2(root, failsafe);

			// Laden hat funktioniert... jetzt noch Sicherungskopie erstellen
			if (!failsafe)
				Utils.copyFile(DATA_FILE_SETTINGS, DATA_FILE_SETTINGS_FAILSAFE);

			return null;
		}
		catch (Exception e)
		{
			if (!failsafe
					&& JOptionPane.showConfirmDialog(null, "Loading the settings failed. Error message is:\n"
							+ e.getMessage() + "\nDo you want to load your backup settings?", EDT.APPLICATION_TITLE,
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				return loadSettings(DATA_FILE_SETTINGS_FAILSAFE, true);
			return e.getMessage() != null ? e.getMessage() : "No description available!";
		}
	}

	/**
	 * Lädt Einstellungen der Version 1. Diese Zeichnen sich dadurch aus, dass
	 * sämtliche Informationen, die das Programm speichert, in einer einzigen
	 * XML-Datei abgelegt werden. Kann die XML-Datei am aus irgendeinem Grund
	 * nicht geladen werden, weigert sich das gesamte Programm zu starten. Daher
	 * wurde Version 2 eingeführt. Hier werden die Dateien aufgeteilt, so dass
	 * das Programm immer fähig ist, zu laden, auch wenn in der History oder den
	 * Favouriten ein Fehler vorliegt.
	 * 
	 * @param root
	 *            Root-XML-Element der edt.xml
	 */
	@SuppressWarnings("unchecked")
	private void loadSettingsVersion1(Element root)
	{
		// Datenbanken
		for (Element conn : (List<Element>)root.getChildren(Database.XML_ROOT))
		{
			databases.add(Database.readXmlElement(conn));
		}
		// Einstellungen
		Element options = root.getChild(XML_OPTIONS);
		for (Element op : (List<Element>)options.getChildren(XML_OPTIONS_ITEM))
		{
			Attribute name = op.getAttribute(XML_OPTIONS_NAME);
			this.options.put(name.getValue(), op.getText());
		}
		// Favouriten
		Element favourites = root.getChild(XML_FAVOURITES);
		for (Element f : (List<Element>)favourites.getChildren(QueryFolder.XML_ROOT))
		{
			this.favourites.add(QueryFolder.readXmlElement(f));
		}
		// History
		Element history = root.getChild(XML_HISTORY);
		for (Element h : (List<Element>)history.getChildren(QueryFolder.XML_ROOT))
		{
			this.history.add(QueryFolder.readXmlElement(h));
		}
	}

	/**
	 * Liest die Einstellung für EDT aus drei verschiedenen Dateien ein.
	 * 
	 * @param root
	 *            Wurzel-Knoten der EDT.xml-Datei
	 * @throws JDOMException
	 *             wenn Fehler beim Einlesen der XML-Dateien auftreten
	 * @throws IOException
	 *             wenn beim Lesen der Dateien Fehler auftreten
	 */
	@SuppressWarnings("unchecked")
	private void loadSettingsVersion2(Element root, boolean failsafe) throws JDOMException, IOException
	{
		SAXBuilder builder = new SAXBuilder();

		// Datenbanken laden
		for (Element conn : (List<Element>)root.getChildren(Database.XML_ROOT))
		{
			databases.add(Database.readXmlElement(conn));
		}
		// Einstellungen laden
		Element options = root.getChild(XML_OPTIONS);
		for (Element op : (List<Element>)options.getChildren(XML_OPTIONS_ITEM))
		{
			Attribute name = op.getAttribute(XML_OPTIONS_NAME);
			this.options.put(name.getValue(), op.getText());
		}

		// Favouriten Laden
		root = builder.build((!failsafe) ? DATA_FILE_FAVOURITES : DATA_FILE_FAVOURITES_FAILSAFE).getRootElement();
		Element favourites = root.getChild(XML_FAVOURITES);
		for (Element f : (List<Element>)favourites.getChildren(QueryFolder.XML_ROOT))
		{
			this.favourites.add(QueryFolder.readXmlElement(f));
		}

		// History laden
		root = builder.build((!failsafe) ? DATA_FILE_HISTORY : DATA_FILE_HISTORY_FAILSAFE).getRootElement();
		Element history = root.getChild(XML_HISTORY);
		for (Element h : (List<Element>)history.getChildren(QueryFolder.XML_ROOT))
		{
			this.history.add(QueryFolder.readXmlElement(h));
		}

		// Laden hat gefunzt... also können wir ne failsafe-sicherung machen
		if (!failsafe)
		{
			Utils.copyFile(DATA_FILE_FAVOURITES, DATA_FILE_FAVOURITES_FAILSAFE);
			Utils.copyFile(DATA_FILE_HISTORY, DATA_FILE_HISTORY_FAILSAFE);
		}
	}

	private void safeXmlToFile(Element root, String filename)
	{
		Document doc = new Document(root);
		XMLOutputter Xout = new XMLOutputter(Format.getPrettyFormat());

		OutputStream out = null;
		try
		{
			out = new BufferedOutputStream(new FileOutputStream(new File(filename)));
			out.write(Xout.outputString(doc).getBytes());
		}
		catch (FileNotFoundException e)
		{}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Unable to save settings to edt.xml! The settings won't be saved!",
					EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (Exception e)
			{}
		}
	}

	/**
	 * Speichert die Einstellungen nach version 1.0
	 */
	private void safeSettingsVersion1()
	{
		Element root = new Element(XML_ROOT);
		root.setAttribute(new Attribute(XML_ROOT_VERSION, XML_ROOT_VERSION_1));

		// Datenbank-Verbindungen
		{
			for (Database db : databases)
			{
				root.addContent(db.getXmlElement());
			}
		}

		// Optionen
		{
			Element options = new Element(XML_OPTIONS);
			root.addContent(options);
			for (String s : this.options.keySet())
			{
				Element op = new Element(XML_OPTIONS_ITEM);
				options.addContent(op);
				op.setAttribute(new Attribute(XML_OPTIONS_NAME, s));
				op.addContent(this.options.get(s));
			}
		}

		// Favouriten
		{
			Collections.sort(favourites);

			Element fav = new Element(XML_FAVOURITES);
			for (QueryFolder folder : favourites)
			{
				fav.addContent(folder.getXmlElement());
			}

			root.addContent(fav);
		}

		// History
		{
			Element his = new Element(XML_HISTORY);
			for (QueryFolder folder : history)
			{
				his.addContent(folder.getXmlElement());
			}

			root.addContent(his);
		}

		// und jetzt den Kram in die Datei schreiben
		safeXmlToFile(root, DATA_FILE_SETTINGS);
	}

	/**
	 * Speichert die Einstellungen nach version 2.0
	 */
	@SuppressWarnings("unused")
	private void safeSettingsVersion2()
	{
		Element root = new Element(XML_ROOT);
		root.setAttribute(new Attribute(XML_ROOT_VERSION, XML_ROOT_VERSION_2));
		root.setAttribute(new Attribute(XML_ROOT_PART, XML_ROOT_PART_MAIN));

		// Datenbank-Verbindungen
		{
			for (Database db : databases)
			{
				root.addContent(db.getXmlElement());
			}
		}

		// Optionen
		{
			Element options = new Element(XML_OPTIONS);
			root.addContent(options);
			for (String s : this.options.keySet())
			{
				Element op = new Element(XML_OPTIONS_ITEM);
				options.addContent(op);
				op.setAttribute(new Attribute(XML_OPTIONS_NAME, s));
				op.addContent(this.options.get(s));
			}
		}
		// speichern
		safeXmlToFile(root, DATA_FILE_SETTINGS);

		// Favouriten
		root = new Element(XML_ROOT);
		root.setAttribute(new Attribute(XML_ROOT_VERSION, XML_ROOT_VERSION_2));
		root.setAttribute(new Attribute(XML_ROOT_PART, XML_ROOT_PART_FAVOURITES));
		{
			Collections.sort(favourites);

			Element fav = new Element(XML_FAVOURITES);
			for (QueryFolder folder : favourites)
			{
				fav.addContent(folder.getXmlElement());
			}

			root.addContent(fav);
		}
		safeXmlToFile(root, DATA_FILE_FAVOURITES);

		// History
		root = new Element(XML_ROOT);
		root.setAttribute(new Attribute(XML_ROOT_VERSION, XML_ROOT_VERSION_2));
		root.setAttribute(new Attribute(XML_ROOT_PART, XML_ROOT_PART_HISTORY));
		{
			Element his = new Element(XML_HISTORY);
			for (QueryFolder folder : history)
			{
				his.addContent(folder.getXmlElement());
			}

			root.addContent(his);
		}
		safeXmlToFile(root, DATA_FILE_HISTORY);
	}

	List<QueryFolder> getFavourites()
	{
		return favourites;
	}

	List<QueryFolder> getHistory()
	{
		return history;
	}

	List<Query> getFavouriteList()
	{
		return mergeFolders(getFavourites());
	}

	List<Query> getHistoryList()
	{
		return mergeFolders(getHistory());
	}

	private List<Query> mergeFolders(List<QueryFolder> folders)
	{
		Vector<Query> q = new Vector<Query>();
		for (QueryFolder folder : folders)
		{
			q.addAll(folder.getQueries());
		}
		return q;
	}

	void addHistoryQuery(JTree tree, DefaultTreeModel model, Query query)
	{
		if (!saveHistory())
			return;
		try
		{
			for (QueryFolder folder : getHistory())
			{
				if (folder.getName().equals(currentDateString))
				{
					// noch testen, ob die Query nicht schonmal da war
					boolean found = false;
					for (Query qry : folder.getQueries())
						if (qry.isSameSql(query.getQueryString()))
						{
							query = qry;
							return;
						}

					if (!found)
					{
						model.insertNodeInto(query.getTreeNode(), folder.getTreeNode(), folder.getQueryCount());
						folder.addQuery(query, false);
						return;
					}
				}
			}

			QueryFolder folder = new QueryFolder(currentDateString);
			getHistory().add(folder);
			folder.addQuery(query, true);
			model.insertNodeInto(folder.getTreeNode(), (DefaultMutableTreeNode)model.getRoot(),
					((DefaultMutableTreeNode)model.getRoot()).getChildCount());
		}
		finally
		{
			// und immer schön die query markieren
			tree.setSelectionPath(new TreePath(query.getTreeNode().getPath()));
		}
	}

	public HashMap<String, String> getOptions()
	{
		return options;
	}

	public String getStringOption(String option)
	{
		return getOptions().get(option);
	}

	public int getIntOption(String option, int defaultValue)
	{
		try
		{
			return Integer.parseInt(getOptions().get(option));
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}

	public boolean saveHistory()
	{
		String s = getOptions().get(MainWindow.OPTIONS_SAVE_HISTORY);
		return s == null || s.equalsIgnoreCase(Boolean.toString(true));
	}

	public boolean showColumnsList()
	{
		String s = getOptions().get(MainWindow.OPTIONS_AUTO_SHOW_COLUMNS_LIST);
		return s == null ? false : s.equalsIgnoreCase(Boolean.toString(true));
	}
}
