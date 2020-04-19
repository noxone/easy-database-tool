package org.noxfire.edt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

class QueryResult {
	final String sql;
	final Statement statement;
	final ResultSet resultset;
	final ResultSet generatedkeys;
	final ResultSetMetaData rsmd;

	final private DBConnection connection;

	final int columnCount;
	final boolean editable;
	final Vector<String> columns;
	final Vector<String> originalTableNames;
	final Vector<String> types;
	final Vector<Class<?>> classes;
	final Vector<Integer> widths;
	final Vector<Boolean> nullable;
	final String usedTables;
	final int tableCount;
	private List<String> keyColumns;

	private HashMap<String, PreparedStatement> updateStatements;
	private PreparedStatement deleteStatement = null;
	private PreparedStatement insertStatement = null;

	public QueryResult(final String sql, final Statement stmt, ResultSet rs, final DBConnection connection)
			throws SQLException {
		this.connection = connection;
		this.sql = sql;
		statement = stmt;
		resultset = rs != null ? rs : stmt.getResultSet();
		generatedkeys = rs != null ? null : stmt.getGeneratedKeys();
		rsmd = resultset.getMetaData();

		updateStatements = new HashMap<String, PreparedStatement>();

		columns = new Vector<String>();
		types = new Vector<String>();
		classes = new Vector<Class<?>>();
		widths = new Vector<Integer>();
		nullable = new Vector<Boolean>();
		originalTableNames = new Vector<String>();

		// Spalten-Überschriften, Classes und Nullable holen...
		// und die Breite
		ResultSetMetaData rsmd = resultset.getMetaData();
		columnCount = rsmd.getColumnCount();
		for (int i = 1; i <= columnCount; ++i) {
			// Name
			columns.add(rsmd.getColumnName(i));
			types.add(rsmd.getColumnTypeName(i) + " (" + rsmd.getPrecision(i) + ")");
			originalTableNames.add(rsmd.getTableName(i));
			// Klasse
			try {
				classes.add(Class.forName(rsmd.getColumnClassName(i)));
			}
			catch (ClassNotFoundException e) {
				classes.add(String.class);
			}
			// NULL ?
			nullable.add(rsmd.isNullable(i) == ResultSetMetaData.columnNullable);

			// Breite
			int w = rsmd.getColumnDisplaySize(i);
			if (w > QueryResultTabPanel.TABLE_MAX_INITIAL_COLUMN_CHAR_WIDTH)
				w = QueryResultTabPanel.TABLE_MAX_INITIAL_COLUMN_CHAR_WIDTH;
			if (w < QueryResultTabPanel.TABLE_MIN_INITIAL_COLUMN_CHAR_WIDTH)
				w = QueryResultTabPanel.TABLE_MIN_INITIAL_COLUMN_CHAR_WIDTH;
			widths.add(w * QueryResultTabPanel.TABLE_DATA_WIDTH_MULTIPLICATOR);

		}

		// Überschrift des Tabs
		String ttables = "";
		boolean emptyName = false;
		HashSet<String> tabSet = new HashSet<String>();
		for (int i = 1; i <= columnCount; ++i) {
			String t = rsmd.getTableName(i);
			if (t.trim().equalsIgnoreCase(""))
				emptyName = true;
			else
				tabSet.add(rsmd.getTableName(i));
		}
		tableCount = tabSet.size() + (emptyName ? 1 : 0);
		for (String s : tabSet)
			ttables += s + ", ";
		if (ttables.length() > 0)
			usedTables = ttables.substring(0, ttables.length() - 2);
		else
			usedTables = "*** strange... no tables used ***";

		// Key-Spalten herausfinden
		if (connection != null && tabSet.size() == 1)
			keyColumns = connection.getKeyColumns(usedTables);
		else
			keyColumns = null;

		// testen, ob die key-spalten auch alle vorhanden sind
		if (keyColumns != null) {
			boolean foundAll = true;
			for (String col : keyColumns) {
				boolean foundCol = false;
				for (String c : columns)
					if (col.equalsIgnoreCase(c)) {
						foundCol = true;
						break;
					}
				foundAll = foundAll && foundCol;
				if (!foundAll)
					break; // die ganze Sache etwas abkürzen
			}
			if (!foundAll)
				keyColumns = null;
		}
		editable = keyColumns != null;
	}

	int getColumnNumber(String name) {
		for (int i = 0; i < columns.size(); ++i)
			if (columns.get(i).equalsIgnoreCase(name))
				return i;
		return -1;
	}

	List<String> getKeyColumns() {
		return keyColumns;
	}

	synchronized PreparedStatement getUpdateStatement(String column) throws SQLException {
		PreparedStatement stmt = updateStatements.get(column);
		if (stmt != null)
			return stmt;

		String sql = "UPDATE " + usedTables + " SET " + column + " = ? WHERE ";
		for (int i = 0; i < getKeyColumns().size(); ++i) {
			if (i > 0)
				sql += " AND ";
			sql += getKeyColumns().get(i) + " = ? ";
		}

		stmt = connection.prepareStatement(sql);
		updateStatements.put(column, stmt);
		return stmt;
	}

	synchronized PreparedStatement getDeleteStatement() throws SQLException {
		PreparedStatement stmt = deleteStatement;
		if (stmt != null)
			return stmt;

		String sql = "DELETE FROM " + usedTables + " WHERE ";
		for (int i = 0; i < getKeyColumns().size(); ++i)
			sql += (i > 0 ? " AND " : "") + getKeyColumns().get(i) + " = ? ";

		stmt = connection.prepareStatement(sql);
		deleteStatement = stmt;
		return stmt;
	}

	synchronized PreparedStatement getInsertStatement() throws SQLException {
		PreparedStatement stmt = insertStatement;
		if (stmt != null)
			return stmt;

		String sql = "INSERT INTO " + usedTables + " (";
		for (int i = 0; i < columns.size(); ++i) {
			if (i > 0)
				sql += " , ";
			sql += columns.get(i);
		}
		sql += " ) VALUES ( ";
		for (int i = 0; i < columns.size(); ++i) {
			if (i > 0)
				sql += " , ";
			sql += " ? ";
		}
		sql += " )";

		stmt = connection.prepareStatement(sql);
		insertStatement = stmt;
		return stmt;
	}

	boolean isKeyCoumn(String column) {
		if (keyColumns == null)
			return false;
		for (String col : keyColumns)
			if (column.equalsIgnoreCase(col))
				return true;
		return false;
	}

}
