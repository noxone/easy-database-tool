package org.noxfire.edt;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;

class LoginDatabaseConfigDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	LoginDatabaseConfigDialog(JFrame owner)
	{
		super(owner);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(300, 200));

		pack();
	}

	@Override
	public void setVisible(boolean b)
	{
		if (b)
		{
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
		}
		super.setVisible(b);
		if (!b)
		{
			dispose();
		}
	}
}
