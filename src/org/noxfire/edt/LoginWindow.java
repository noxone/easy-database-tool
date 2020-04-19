package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

class LoginWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	private Object waiter = new Object();
	private Action retVal = null;

	private JPanel pnlMain;
	private JPanel pnlButtons;
	private JPanel pnlInput;

	private JButton btnOK;
	private JButton btnCancel;
	private JTextField txtUsername;
	private JTextField txtPassword;
	private JPanel pnlDatabase;
	// private JButton btnConfigDatabase;
	private JComboBox cmbDatabase;

	private String username = "";
	private String password = "";
	private Database database = null;

	public LoginWindow(Vector<Database> databases)
	{
		setIconImage(Toolkit.getDefaultToolkit().getImage("gfx/logo.gif"));
		setTitle(EDT.APPLICATION_TITLE + " - Database Login");
		setResizable(false);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pnlMain = new JPanel();
		pnlMain.setLayout(new BorderLayout());

		pnlButtons = new JPanel();
		btnOK = new JButton("OK");
		btnCancel = new JButton("Abbrechen");
		pnlButtons.add(btnOK);
		pnlButtons.add(btnCancel);
		// btnConfigDatabase = new JButton("...");
		// btnConfigDatabase.setPreferredSize(new Dimension(24, 24));
		pnlDatabase = new JPanel();
		pnlDatabase.setLayout(new BorderLayout());
		// pnlDatabase.add(btnConfigDatabase, BorderLayout.EAST);
		pnlMain.add(pnlButtons, BorderLayout.SOUTH);

		pnlInput = new JPanel();
		txtUsername = new JTextField(20);
		txtPassword = new JPasswordField(20);
		cmbDatabase = new JComboBox(databases);
		pnlDatabase.add(cmbDatabase, BorderLayout.CENTER);
		pnlInput.setLayout(new GridLayout(3, 2));
		pnlInput.add(new JLabel("Database connection:"));
		pnlInput.add(pnlDatabase);
		pnlInput.add(new JLabel("Username:"));
		pnlInput.add(txtUsername);
		pnlInput.add(new JLabel("Password:"));
		pnlInput.add(txtPassword);
		pnlMain.add(pnlInput, BorderLayout.CENTER);

		getRootPane().setDefaultButton(btnOK);

		setLayout(new FlowLayout());
		add(pnlMain);

		pack();

		txtUsername.requestFocus();

		btnCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				retVal = Action.Cancel;
				synchronized (waiter)
				{
					waiter.notify();
				}
			}
		});
		btnOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				retVal = Action.OK;
				synchronized (waiter)
				{
					waiter.notify();
				}
			}
		});
		// btnConfigDatabase.addActionListener(new ActionListener()
		// {
		// public void actionPerformed(ActionEvent e)
		// {
		// LoginDatabaseConfigDialog ldcd = new
		// LoginDatabaseConfigDialog(LoginWindow.this);
		// ldcd.setVisible(true);
		// }
		// });
		txtUsername.addKeyListener(new EscapeListener());
		txtPassword.addKeyListener(new EscapeListener());
		cmbDatabase.addKeyListener(new EscapeListener());
		btnOK.addKeyListener(new EscapeListener());
		btnCancel.addKeyListener(new EscapeListener());
		// btnConfigDatabase.addKeyListener(new EscapeListener());

		getRootPane().setDefaultButton(btnOK);
	}

	public Action showAndWait()
	{
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);

		setVisible(true);
		synchronized (waiter)
		{
			try
			{
				waiter.wait();
				username = txtUsername.getText();
				password = txtPassword.getText();
				database = (Database)cmbDatabase.getSelectedItem();
			}
			catch (InterruptedException e)
			{}
		}
		setVisible(false);
		dispose();
		return retVal;
	}

	String getUsername()
	{
		return username;
	}

	String getPassword()
	{
		return password;
	}

	Database getDatabase()
	{
		return database;
	}

	private class EscapeListener extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				retVal = Action.Cancel;
				synchronized (waiter)
				{
					waiter.notify();
				}
			}
		}
	}

	public static enum Action
	{
		OK, Cancel;
	}
}
