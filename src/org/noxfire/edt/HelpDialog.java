package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.noxfire.edt.Help.HelpItem;

public class HelpDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private JPanel pnlBottom;
	private JPanel pnlHelp;
	private JTextField txtOriginalMessage;
	private JTextField txtCode;
	private JTextArea txtMessage;
	private JTextArea txtExplanation;
	private JTextArea txtSystemAction;
	private JTextArea txtProgrammersResponse;
	private JTextArea txtSqlState;
	private JButton btnOK;

	public HelpDialog(Window owner, HelpItem item, String originalMessage)
	{
		super(owner);
		setModalityType(ModalityType.MODELESS);

		setIconImage(Toolkit.getDefaultToolkit().getImage("gfx/logo.gif"));
		setTitle(EDT.APPLICATION_TITLE + " - Help");

		txtOriginalMessage = new JTextField(originalMessage);
		txtOriginalMessage.setEditable(false);
		txtCode = new JTextField(Integer.toString(item.code));
		txtMessage = new DisplayTextArea(item.message);
		txtExplanation = new DisplayTextArea(item.explanation);
		txtSystemAction = new DisplayTextArea(item.systemAction);
		txtProgrammersResponse = new DisplayTextArea(item.programmersResponse);
		txtSqlState = new DisplayTextArea(item.sqlState);
		txtCode.setEditable(false);
		txtMessage.setEditable(false);
		txtExplanation.setEditable(false);
		txtSystemAction.setEditable(false);
		txtProgrammersResponse.setEditable(false);
		txtSqlState.setEditable(false);
		pnlHelp = new JPanel();
		pnlHelp.setLayout(new GridBagLayout());
		// GridBagLayout einrichten
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		// Controls hinzufügen
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(2, 2, 2, 10);
		c.anchor = GridBagConstraints.PAGE_START;
		pnlHelp.add(new JLabel("SQL code:"), c);
		c.weighty = 0.2;
		c.gridy = 1;
		pnlHelp.add(new JLabel("Message:"), c);
		c.gridy = 2;
		pnlHelp.add(new JLabel("Explanation:"), c);
		c.gridy = 3;
		pnlHelp.add(new JLabel("System action:"), c);
		c.gridy = 4;
		pnlHelp.add(new JLabel("Programmers response:"), c);
		c.gridy = 5;
		pnlHelp.add(new JLabel("SQL state:"), c);
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 0;
		c.weighty = 0.0;
		pnlHelp.add(txtCode, c);
		c.weighty = 0.2;
		c.gridy = 1;
		pnlHelp.add(new JScrollPane(txtMessage), c);
		c.gridy = 2;
		pnlHelp.add(new JScrollPane(txtExplanation), c);
		c.gridy = 3;
		pnlHelp.add(new JScrollPane(txtSystemAction), c);
		c.gridy = 4;
		pnlHelp.add(new JScrollPane(txtProgrammersResponse), c);
		c.gridy = 5;
		pnlHelp.add(new JScrollPane(txtSqlState), c);

		btnOK = new JButton("OK");
		pnlBottom = new JPanel();
		pnlBottom.add(btnOK);

		setLayout(new BorderLayout());
		add(txtOriginalMessage, BorderLayout.NORTH);
		add(pnlHelp, BorderLayout.CENTER);
		add(pnlBottom, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(btnOK);

		pack();

		btnOK.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
	}

	@Override
	public void setVisible(boolean b)
	{
		if (b)
		{
			if (getOwner() != null)
			{
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				if (getPreferredSize().width > screenSize.width)
				{
					setSize(screenSize.width, getPreferredSize().height);
					setLocation(screenSize.width / 2 - getWidth() / 2, getOwner().getLocation().y / 2
							+ getOwner().getHeight() / 2 - getHeight() / 2);
				}
				else
				{
					setLocation(getOwner().getLocation().x / 2 + getOwner().getWidth() / 2 - getWidth() / 2, getOwner()
							.getLocation().y
							/ 2 + getOwner().getHeight() / 2 - getHeight() / 2);
				}
			}
		}
		super.setVisible(b);
		if (!b)
			dispose();
	}

	private static class DisplayTextArea extends JTextArea
	{
		private static final long serialVersionUID = 1L;

		public DisplayTextArea(String text)
		{
			super(text);
		}

		@Override
		public Dimension getPreferredSize()
		{
			Dimension d = super.getPreferredSize();
			return new Dimension(d.width, d.height + 40);
		}
	}
}
