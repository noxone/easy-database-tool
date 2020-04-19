package org.noxfire.edt.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

import org.noxfire.edt.QueryException;
import org.noxfire.edt.Utils;

public class GeneralTableCellEditor extends AbstractCellEditor implements TableCellEditor
{
	private static final long serialVersionUID = -8251056010449437066L;

	protected JComponent editorComponent;
	protected EditorDelegate delegate;
	protected int clickCountToStart = 1;

	private JPanel pnlEditor;
	private JButton btnSetNull;
	private boolean selectedNull = false;

	private int editingColumn = -1;
	private int editingRow = -1;
	private Object editingCellValue = null;

	public GeneralTableCellEditor(final Class<?> clazz, final boolean nullable, final TableCellEditorListener listener)
	{
		this.clickCountToStart = 2;

		pnlEditor = new JPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void requestFocus()
			{
				getComponent(0).requestFocus();
			}
		};
		pnlEditor.setLayout(new BorderLayout());

		if (clazz == Boolean.class)
		{
			final JCheckBox checkbox = new JCheckBox();
			editorComponent = checkbox;
			delegate = new EditorDelegate()
			{
				private static final long serialVersionUID = 6878123654052902744L;

				@Override
				public void setValue(Object value)
				{
					boolean selected = false;
					if (value instanceof Boolean)
					{
						selected = ((Boolean)value).booleanValue();
					}
					else if (value instanceof String)
					{
						selected = ((String)value).equalsIgnoreCase("true");
					}
					checkbox.setSelected(selected);
				}

				@Override
				public Object getCellEditorValue()
				{
					return Boolean.valueOf(checkbox.isSelected());
				}
			};
			checkbox.addActionListener(delegate);
			checkbox.setRequestFocusEnabled(false);
		}
		else
		{
			final JTextField textField = new JTextField();
			editorComponent = textField;

			if (Utils.isNumber(clazz))
				textField.setHorizontalAlignment(JTextField.RIGHT);
			delegate = new EditorDelegate()
			{
				private static final long serialVersionUID = -2181994258393565530L;

				@Override
				public boolean isCellEditable(EventObject arg0)
				{
					boolean editable = super.isCellEditable(arg0);
					if (editable)
					{
						try
						{
							clazz.getConstructor(String.class);
							return true;
						}
						catch (Exception e)
						{
							return false;
						}
					}
					return editable;
				}

				@Override
				public void setValue(Object value)
				{
					super.setValue(value);
					textField.setText((value != null) ? value.toString() : "");
				}

				@Override
				public Object getCellEditorValue()
				{
					String text = textField.getText();
					Constructor<?> con = null;
					try
					{
						con = clazz.getConstructor(String.class);
						return con.newInstance(text);
					}
					catch (SecurityException e)
					{
						return value;
					}
					catch (NoSuchMethodException e)
					{
						return value;
					}
					catch (IllegalArgumentException e)
					{
						return value;
					}
					catch (InstantiationException e)
					{
						return value;
					}
					catch (IllegalAccessException e)
					{
						return value;
					}
					catch (InvocationTargetException e)
					{
						return value;
					}
				}

				@Override
				public boolean stopCellEditing()
				{
					try
					{
						clazz.getConstructor(String.class).newInstance(textField.getText());
						return super.stopCellEditing();
					}
					catch (Exception e)
					{
						listener.userAttempsToSetFailingValue(textField.getText());
						return false;
					}
				}
			};
			textField.addActionListener(delegate);
			textField.addFocusListener(new FocusAdapter()
			{
				@Override
				public void focusGained(FocusEvent e)
				{
					textField.selectAll();
				}
			});
		}

		pnlEditor.add(editorComponent, BorderLayout.CENTER);
		if (nullable)
		{
			btnSetNull = new JButton("NULL");
			btnSetNull.setPreferredSize(new Dimension(20, 20));
			btnSetNull.setToolTipText("Set this value to NULL");
			btnSetNull.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					selectedNull = true;
					delegate.actionPerformed(e);
				}
			});
			pnlEditor.add(btnSetNull, BorderLayout.EAST);
		}

		addCellEditorListener(new CellEditorListener()
		{
			@Override
			public void editingCanceled(ChangeEvent ce)
			{
				editingColumn = -1;
				editingRow = 0;
				editingCellValue = null;
			}

			public void editingStopped(ChangeEvent ce)
			{
				if (listener != null)
				{
					Object nVal = listener.getTable().getValueAt(listener.getTable().getSelectedRow(),
							listener.getTable().getSelectedColumn());
					if (((editingCellValue == null || nVal == null) && editingCellValue != nVal)
							|| (editingCellValue != null && !editingCellValue.equals(nVal)))
					{
						if (!Utils.isNumber(editingCellValue)
								|| ((editingCellValue == null && nVal != null)
										|| (editingCellValue != null && nVal == null) || ((Number)editingCellValue)
										.longValue() != ((Number)nVal).longValue()))
						{
							try
							{
								if (listener.runUpdate(editingColumn, editingRow, editingCellValue))
									listener.setInfoText("Update OK!", false);
								else
									throw new QueryException(listener.getUpdateWarning());
							}
							catch (QueryException e)
							{
								listener.getTable().setValueAt(editingCellValue,
										listener.getTable().convertRowIndexToView(editingRow),
										listener.getTable().convertColumnIndexToView(editingColumn));
								listener.setInfoText("Update failed: " + e.getMessage(), true);
							}
						}
					}
					editingColumn = -1;
					editingRow = -1;
				}
			};
		});
	}

	public void setClickCountToStart(int count)
	{
		clickCountToStart = count;
	}

	public int getClickCountToStart()
	{
		return clickCountToStart;
	}

	public Object getCellEditorValue()
	{
		if (selectedNull)
		{
			selectedNull = false;
			return null;
		}
		else
			return delegate.getCellEditorValue();
	}

	@Override
	public boolean isCellEditable(EventObject anEvent)
	{
		return delegate.isCellEditable(anEvent);
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent)
	{
		return delegate.shouldSelectCell(anEvent);
	}

	@Override
	public boolean stopCellEditing()
	{
		return delegate.stopCellEditing();
	}

	@Override
	public void cancelCellEditing()
	{
		delegate.cancelCellEditing();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		editingColumn = table.convertColumnIndexToModel(column);
		editingRow = table.convertRowIndexToModel(row);
		editingCellValue = value;

		delegate.setValue(value);
		return pnlEditor;
	}

	protected abstract class EditorDelegate implements ActionListener, Serializable
	{
		private static final long serialVersionUID = -3455433983508731985L;

		protected Object value;

		public Object getCellEditorValue()
		{
			return value;
		}

		public void setValue(Object value)
		{
			this.value = value;
		}

		public boolean isCellEditable(EventObject anEvent)
		{
			if (anEvent instanceof MouseEvent)
			{
				return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
			}
			return true;
		}

		public boolean shouldSelectCell(EventObject anEvent)
		{
			return true;
		}

		public boolean startCellEditing(EventObject anEvent)
		{
			return true;
		}

		public boolean stopCellEditing()
		{
			fireEditingStopped();
			return true;
		}

		public void cancelCellEditing()
		{
			fireEditingCanceled();
		}

		public void actionPerformed(ActionEvent e)
		{
			GeneralTableCellEditor.this.stopCellEditing();
		}

		public void itemStateChanged(ItemEvent e)
		{
			GeneralTableCellEditor.this.stopCellEditing();
		}
	}

}
