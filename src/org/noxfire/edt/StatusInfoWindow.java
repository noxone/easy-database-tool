package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class StatusInfoWindow extends JDialog
{
	private static final long serialVersionUID = 1L;

	private JLabel lblInfo;

	public StatusInfoWindow()
	{
		setIconImage(Toolkit.getDefaultToolkit().getImage("gfx/logo.gif"));
		setTitle(EDT.APPLICATION_TITLE);

		setLayout(new BorderLayout());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);

		lblInfo = new JLabel("");
		lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfo.setVerticalAlignment(SwingConstants.CENTER);
		add(lblInfo, BorderLayout.CENTER);
	}

	public void showInfoMessage(String text)
	{
		lblInfo.setText(text);
		pack();
		setSize(getWidth() + 100, getHeight() + 60);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
		setVisible(true);
	}
}
