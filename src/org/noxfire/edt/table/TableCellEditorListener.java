package org.noxfire.edt.table;

import java.util.Vector;

import javax.swing.JTable;

import org.noxfire.edt.QueryException;

public interface TableCellEditorListener
{
	boolean runUpdate(int column, int row, Object oldVal) throws QueryException;
	
	String getUpdateWarning();
	
	String insertRows(Vector<Vector<Object>> data);

	JTable getTable();

	void setInfoText(String text, boolean error);
	
	void userAttempsToSetFailingValue(String value);
}
