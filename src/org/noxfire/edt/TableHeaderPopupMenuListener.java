package org.noxfire.edt;

import java.awt.Point;

import javax.swing.table.TableColumn;

public interface TableHeaderPopupMenuListener
{
	void popupMenuCalled(TableColumn column, Point mousePosition);
}
