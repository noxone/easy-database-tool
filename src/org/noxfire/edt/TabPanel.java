package org.noxfire.edt;

import javax.swing.JPanel;

import org.noxfire.edt.gui.ButtonTabComponent;

class TabPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	protected ButtonTabComponent tabComponent;
	
	public TabPanel(ButtonTabComponent tabComponent)
	{
		this.tabComponent = tabComponent;
	}
}
