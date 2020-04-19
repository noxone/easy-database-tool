package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.DefaultTreeModel;

public class AddFavouriteDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private static final int MAX_NAME_LENGTH = 30;

	private final JFrame owner;

	private JPanel pnlTop;
	private JPanel pnlTop2;
	private JPanel pnlTop3;
	private JPanel pnlBottom;

	private JTextField txtName;
	private JTextArea txtSql;
	private JList lstFolders;
	private DefaultListModel mdlFolders;
	private JButton btnAddFolder;
	private JButton btnOK;
	private JButton btnCancel;

	public AddFavouriteDialog(final JFrame owner, final EDT edt, String sql, final ActionListener[] actionlisteners,
			final DefaultTreeModel mdlTree)
	{
		super(owner);
		this.owner = owner;
		setModalityType(ModalityType.APPLICATION_MODAL);

		setTitle(EDT.APPLICATION_TITLE + " - Add favourite");
		setIconImage(Toolkit.getDefaultToolkit().getImage("gfx/logo.gif"));
		setLayout(new BorderLayout());

		mdlFolders = new DefaultListModel();
		for (QueryFolder folder : edt.getFavourites())
			mdlFolders.add(mdlFolders.getSize(), folder);
		lstFolders = new JList(mdlFolders);
		add(new JScrollPane(lstFolders), BorderLayout.CENTER);

		pnlBottom = new JPanel();
		btnOK = new JButton("OK");
		btnCancel = new JButton("Cancel");
		pnlBottom.add(btnCancel);
		pnlBottom.add(btnOK);
		add(pnlBottom, BorderLayout.SOUTH);

		pnlTop = new JPanel();
		pnlTop2 = new JPanel();
		pnlTop3 = new JPanel();
		pnlTop2.setLayout(new BorderLayout());
		pnlTop3.setLayout(new BorderLayout());
		pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
		txtName = new JTextField();
		if (sql.length() <= MAX_NAME_LENGTH)
			txtName.setText(sql);
		else
			txtName.setText(sql.substring(0, MAX_NAME_LENGTH));
		txtSql = new JTextArea();
		txtSql.setPreferredSize(new Dimension(450, 150));
		txtSql.setText(sql);
		btnAddFolder = new JButton("Add folder");
		pnlTop.add(new JLabel("Please enter the name of the query:"));
		pnlTop.add(txtName);
		pnlTop.add(new JLabel("SQL-Code:"));
		pnlTop3.add(btnAddFolder, BorderLayout.EAST);
		pnlTop2.add(pnlTop, BorderLayout.NORTH);
		pnlTop2.add(new JScrollPane(txtSql), BorderLayout.CENTER);
		pnlTop2.add(pnlTop3, BorderLayout.SOUTH);
		add(pnlTop2, BorderLayout.NORTH);

		btnAddFolder.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				for (ActionListener listener : actionlisteners)
					listener.actionPerformed(e);
				mdlFolders.clear();
				for (QueryFolder folder : edt.getFavourites())
					mdlFolders.add(mdlFolders.getSize(), folder);
			}
		});

		btnCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
		btnOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (lstFolders.getSelectedValue() != null)
				{
					QueryFolder folder = ((QueryFolder)lstFolders.getSelectedValue());
					Query query = new Query(txtName.getText(), txtSql.getText());
					folder.addQuery(query, false);
					mdlTree.insertNodeInto(query.getTreeNode(), folder.getTreeNode(), folder.getTreeNode()
							.getChildCount());
				}
				setVisible(false);
			}
		});

		pack();
	}

	@Override
	public void setVisible(boolean b)
	{
		if (b)
		{
			setLocation(owner.getLocation().x / 2 + owner.getWidth() / 2 - getWidth() / 2, owner.getLocation().y / 2
					+ owner.getHeight() / 2 - getHeight() / 2);
		}
		super.setVisible(b);
		if (!b)
		{
			dispose();
		}
	}
}
