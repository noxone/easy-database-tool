package org.noxfire.edt.table;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public class StringTableCellEditor extends AbstractTableCellEditor
{
	private static final long serialVersionUID = 1L;

	private JTextField txtEditor;
	
	public StringTableCellEditor(TableCellEditorListener listener, boolean nullable)
	{
		super(new JTextField(), listener, nullable);

		// txtEditor = new JTextField();
		txtEditor = (JTextField)super.editorComponent;
		txtEditor.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					fireEditingCanceled();
			}
		});
	}

	@Override
	protected Component getTableCellEditorComponent(Object value)
	{
		if (value != null)
			txtEditor.setText(value.toString());
		else
			txtEditor.setText("");
		txtEditor.setSelectionStart(0);
		txtEditor.setSelectionEnd(txtEditor.getText().length());
		return txtEditor;
	}

	@Override
	public Object getRealCellEditorValue()
	{
		return txtEditor.getText();
	}

}
