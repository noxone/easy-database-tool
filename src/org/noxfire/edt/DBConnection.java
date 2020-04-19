package org.noxfire.edt;

import java.awt.Window;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

class DBConnection {
	private static final String DB2_CONNECTION_LOST_MESSAGE = "Execution failed due to a distribution protocol error that caused deallocation of the conversation. The command requested could not be completed because of a permanent error condition detected at the target system.";
	private static final int MAX_CONNECTION_RENEWAL_COUNT = 10;
	private Database database;
	private String username;
	private String password;
	private Connection connection = null;
	private String lastError;

	private List<Table> tables = null;

	/**
	 * Caching der Key-Spalten, damit nicht bei jeder Abfrage immer die gleichen Informationen
	 * abgefragt werden müssen
	 */
	private HashMap<String, List<String>> keyColumns;
	private HashMap<String, List<String>> columns;

	public DBConnection() {
		keyColumns = new HashMap<String, List<String>>();
		columns = new HashMap<String, List<String>>();
	}

	private String getJdbcString() {
		String jdbc = "jdbc:" + database.jdbcPrefix + ":";
		if (!database.server.isEmpty()) {
			jdbc += "//" + database.server + ":" + database.port;
		}
		jdbc += "/" + database.database;
		return jdbc;
	}

	boolean connect(Database database, String username, String password) {
		this.database = database;
		this.username = username;
		this.password = password;

		try {
			Class.forName(database.driverClassName);
		}
		catch (ClassNotFoundException e) {
			setLastError("Database driver not found!");
			return false;
		}

		try {
			connection = DriverManager.getConnection(getJdbcString(), username, password);
			connection.setReadOnly(false);
			connection.setAutoCommit(true);
		}
		catch (SQLException e) {
			setLastError(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Schließt die interne Verbindung und öffnet sie erneut.
	 * 
	 * @return true, wenn die Aktion geglückt ist
	 */
	boolean renewConnection() {
		close();
		return connect(database, username, password);
	}

	void close() {
		try {
			connection.close();
		}
		catch (SQLException e) {}
	}

	private QueryResult executeFind(String query, String variablePart) {
		if (connection == null)
			return null;

		PreparedStatement stmt;

		try {
			stmt = connection.prepareStatement(query);
			stmt.setString(1, variablePart);
			stmt.execute();
			return new QueryResult(query, stmt, null, null);
		}
		catch (SQLException e) {
			return null;
		}
	}

	public QueryResult executeSelect(Window owner, String query, String officialQuery) {
		return executeSelect(owner, query, officialQuery, 0);
	}

	private QueryResult executeSelect(Window owner, String query, String officialQuery, int retryCount) {
		if (connection == null) {
			JOptionPane
					.showMessageDialog(
							owner,
							"No connection to Database available! The query won't be processed.\nSave your query and restart the application!",
							EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
			return null;
		}

		Statement stmt;

		try {
			stmt = connection.createStatement();
			// stmt.execute(query, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stmt.executeQuery(query);

			// return new QueryResult(query, stmt, this);
			return new QueryResult(officialQuery, stmt, rs, this);
		}
		catch (SQLException e) {
			if (owner != null) {
				String message = e.getMessage();
				if (!Help.tryToDisplayDB2Help(owner, message)) {
					if (retryCount < MAX_CONNECTION_RENEWAL_COUNT //
							&& message != null //
							&& message.startsWith(DB2_CONNECTION_LOST_MESSAGE)) {
						if (renewConnection()) {
							return executeSelect(owner, query, officialQuery, retryCount + 1);
						} else {
							return null;
						}
					} else {
						JOptionPane.showMessageDialog(owner, "An error occurred while processing your query:\n"
								+ message, EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			return null;
		}
	}

	public boolean executeUpdate(String query) throws QueryException {
		if (connection == null) {
			JOptionPane
					.showMessageDialog(
							null,
							"No connection to Database available! The query won't be processed.\nSave your query and restart the application!",
							EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
			return false;
		}

		Statement stmt;

		try {
			stmt = connection.createStatement();
			stmt.executeUpdate(query);

			return true;
		}
		catch (SQLException e) {
			throw new QueryException(e.getMessage());
		}
	}

	public boolean executeUpdate(PreparedStatement stmt) throws QueryException {
		if (connection == null) {
			JOptionPane
					.showMessageDialog(
							null,
							"No connection to Database available! The query won't be processed.\nSave your query and restart the application!",
							EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			boolean blub = stmt.execute();
			return blub;
		}
		catch (SQLException e) {
			throw new QueryException(e.getMessage());
		}
	}

	synchronized List<Table> getTables() {
		if (tables == null) {
			tables = new Vector<Table>();

			QueryResult result = executeSelect(null, database.findTablesQuery, null);

			if (result != null) {
				try {
					int tblName = 0;
					int objName = 0;
					for (int i = 1; i <= result.resultset.getMetaData().getColumnCount(); ++i) {
						if (result.resultset.getMetaData().getColumnName(i).equalsIgnoreCase(database.tableColumnName))
							tblName = i;
						if (database.objectColumnName != null
								&& result.resultset.getMetaData().getColumnName(i)
										.equalsIgnoreCase(database.objectColumnName))
							objName = i;
					}

					if (tblName != 0) {
						while (result.resultset.next()) {
							String type = null;
							String name = result.resultset.getString(tblName).trim();
							if (objName != 0)
								type = result.resultset.getString(objName).trim();

							Table table = new Table(name + (type != null ? " (" + type + ")" : ""), name);
							tables.add(table);

							// Spalten holen
							if (database.readColumnAtStart && database.findColumnsQuery != null) {
								List<String> columns = getColumns(name);
								if (columns != null)
									for (String s : columns)
										table.addColumn(s);
							}
						}
					}
				}
				catch (SQLException e) {
					JOptionPane.showMessageDialog(null,
							"Cannot load table information from database. There will be no table tree available!",
							EDT.APPLICATION_TITLE, JOptionPane.ERROR_MESSAGE);
				}
				finally {
					try {
						result.statement.close();
					}
					catch (SQLException e) {}
				}
			}
		}

		return tables;
	}

	synchronized List<String> getColumns(String table) {
		if (database.findColumnsQuery == null)
			return null;

		// ist die Liste der Key-Spalten evtl schon gecached?
		List<String> columns = this.columns.get(table.toUpperCase());
		if (columns != null)
			return columns;

		QueryResult qr = executeFind(database.findColumnsQuery, table);

		if (qr != null) {
			try {
				int colName = 0;
				for (int i = 1; i <= qr.resultset.getMetaData().getColumnCount(); ++i) {
					if (qr.resultset.getMetaData().getColumnName(i).equalsIgnoreCase(database.columnColumnName))
						colName = i;
				}

				columns = new Vector<String>();
				while (qr.resultset.next()) {
					columns.add(qr.resultset.getString(colName).trim());
				}
				if (columns.size() == 0)
					columns = null;
			}
			catch (SQLException e) {
				columns = null;
			}
			finally {
				try {
					qr.statement.close();
				}
				catch (SQLException e) {}
			}
		}

		// Spalten cachen
		this.columns.put(table.toUpperCase(), columns);

		return columns;
	}

	synchronized List<String> getKeyColumns(String table) {
		// ist die Liste der Key-Spalten evtl schon gecached?
		List<String> keyColumns = this.keyColumns.get(table.toUpperCase());
		if (keyColumns != null)
			return keyColumns;

		QueryResult qr = executeFind(database.findKeyColumnsQuery, table);

		if (qr != null) {
			try {
				int colName = 0;
				for (int i = 1; i <= qr.resultset.getMetaData().getColumnCount(); ++i) {
					if (qr.resultset.getMetaData().getColumnName(i).equalsIgnoreCase(database.columnColumnName))
						colName = i;
				}

				keyColumns = new Vector<String>();
				while (qr.resultset.next()) {
					keyColumns.add(qr.resultset.getString(colName).trim());
				}
				if (keyColumns.size() == 0)
					keyColumns = null;
			}
			catch (SQLException e) {
				keyColumns = null;
			}
			finally {
				try {
					qr.statement.close();
				}
				catch (SQLException e) {}
			}
		}

		// Spalten cachen
		this.keyColumns.put(table.toUpperCase(), keyColumns);

		return keyColumns;
	}

	PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	String getLastError() {
		return lastError;
	}

	private void setLastError(String error) {
		lastError = error;
	}

	public Database getDatabase() {
		return database;
	}

	String getUsername() {
		return username;
	}
}
