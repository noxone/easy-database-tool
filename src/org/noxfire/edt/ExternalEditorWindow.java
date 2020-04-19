package org.noxfire.edt;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.noxfire.edt.jedit.JEditTextArea;
import org.noxfire.edt.jedit.TSQLTokenMarker;

class ExternalEditorWindow extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private JEditTextArea txtEditor;
	private JPanel pnlBottom;
	private JButton btnClose;
	
	private JPopupMenu popEditorRightclick;
	private JMenuItem mnuCut;
	private JMenuItem mnuCopy;
	private JMenuItem mnuPaste;

	public ExternalEditorWindow(final MainWindow owner)
	{
		super(owner);
		
		setTitle(EDT.APPLICATION_TITLE + " - Query editor");
		setIconImage(Toolkit.getDefaultToolkit().getImage("gfx/logo.gif"));
		
		pnlBottom = new JPanel();
		btnClose = new JButton("Close");
		pnlBottom.add(btnClose);
		txtEditor = new JEditTextArea(owner);
		txtEditor.setTokenMarker(new TSQLTokenMarker());
		txtEditor.setAutoscrolls(true);
		txtEditor.setCaretBlinkEnabled(true);
		setLayout(new BorderLayout());
		add(txtEditor, BorderLayout.CENTER);
		add(pnlBottom, BorderLayout.SOUTH);
		
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
		
		popEditorRightclick = new JPopupMenu();
		mnuCut = new JMenuItem("Cut", IconFactory.getIcon("cut"));
		mnuCopy = new JMenuItem("Copy", IconFactory.getIcon("copy"));
		mnuPaste = new JMenuItem("Paste", IconFactory.getIcon("paste"));
		mnuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		mnuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		mnuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		popEditorRightclick.add(mnuCut);
		popEditorRightclick.add(mnuCopy);
		popEditorRightclick.add(mnuPaste);
		txtEditor.setComponentPopupMenu(popEditorRightclick);

		mnuCut.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				owner.cut(txtEditor);
			}
		});
		mnuCopy.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				owner.copy(txtEditor);
			}
		});
		mnuPaste.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				owner.paste(txtEditor);
			}
		});
		
		pack();
	}
}
