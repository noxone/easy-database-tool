package org.noxfire.edt.table;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class IntegerTableCellEditor extends AbstractTableCellEditor
{
	private static final long serialVersionUID = 1L;

	private JTextField txtEditor;

	public IntegerTableCellEditor(TableCellEditorListener listener, boolean nullable)
	{
		super(new JTextField(), listener, nullable);

		// txtEditor = new JTextField();
		txtEditor = (JTextField)super.editorComponent;
		txtEditor.setHorizontalAlignment(JTextField.RIGHT);
		txtEditor.setDocument(new PlainDocument()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
			{
				try
				{
					Integer.parseInt(str);
					super.insertString(offs, str, a);
				}
				catch (NumberFormatException e)
				{}
			}
		});
		txtEditor.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				txtEditor.selectAll();
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
		if (txtEditor.getText().length() == 0)
			return null;
		else
			return new Long(txtEditor.getText());
	}
}
