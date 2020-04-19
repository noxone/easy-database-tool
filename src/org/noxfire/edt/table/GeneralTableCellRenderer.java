package org.noxfire.edt.table;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.noxfire.edt.Utils;

import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import java.io.Serializable;

public class GeneralTableCellRenderer extends JLabel implements TableCellRenderer, Serializable
{
	private static final long serialVersionUID = 5907738139937360062L;
	
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

	// We need a place to store the color the JLabel should be returned
	// to after its foreground and background colors have been set
	// to the selection background color.
	// These ivars will be made protected when their names are finalized.
	private Color unselectedForeground;
	private Color unselectedBackground;

	public GeneralTableCellRenderer()
	{
		super();
		setOpaque(true);
		setBorder(getNoFocusBorder());
	}

	private static Border getNoFocusBorder()
	{
		if (System.getSecurityManager() != null)
		{
			return SAFE_NO_FOCUS_BORDER;
		}
		else
		{
			return noFocusBorder;
		}
	}

	@Override
	public void setForeground(Color c)
	{
		super.setForeground(c);
		unselectedForeground = c;
	}

	@Override
	public void setBackground(Color c)
	{
		super.setBackground(c);
		unselectedBackground = c;
	}

	@Override
	public void updateUI()
	{
		super.updateUI();
		setForeground(null);
		setBackground(null);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
	{

		Color fg = null;
		Color bg = null;

		JTable.DropLocation dropLocation = table.getDropLocation();
		if (dropLocation != null && !dropLocation.isInsertRow() && !dropLocation.isInsertColumn()
				&& dropLocation.getRow() == row && dropLocation.getColumn() == column)
		{

			fg = UIManager.getColor("Table.dropCellForeground");
			bg = UIManager.getColor("Table.dropCellBackground");

			isSelected = true;
		}

		final int darkerer = 18;
		if (isSelected)
		{
			super.setForeground(fg == null ? table.getSelectionForeground() : fg);
			Color cbg = bg == null ? table.getSelectionBackground() : bg;
			if (row % 2 == 0)
				super.setBackground(cbg);
			else
				super.setBackground(new Color(cbg.getRed() - darkerer, cbg.getGreen() - darkerer, cbg.getBlue()
						- darkerer));
		}
		else
		{
			super.setForeground(unselectedForeground != null ? unselectedForeground : table.getForeground());
			Color cbg = unselectedBackground != null ? unselectedBackground : table.getBackground();
			if (row % 2 == 0)
				super.setBackground(cbg);
			else
				super.setBackground(new Color(cbg.getRed() - darkerer, cbg.getGreen() - darkerer, cbg.getBlue()
						- darkerer));
		}

		setFont(table.getFont());

		if (hasFocus)
		{
			Border border = null;
			if (isSelected)
			{
				border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
			}
			if (border == null)
			{
				border = UIManager.getBorder("Table.focusCellHighlightBorder");
			}
			setBorder(border);

			if (!isSelected && table.isCellEditable(row, column))
			{
				Color col;
				col = UIManager.getColor("Table.focusCellForeground");
				if (col != null)
				{
					super.setForeground(col);
				}
				col = UIManager.getColor("Table.focusCellBackground");
				if (col != null)
				{
					super.setBackground(col);
				}
			}
		}
		else
		{
			setBorder(getNoFocusBorder());
		}

		setValue(value);

		if (value == null)
		{
			setHorizontalAlignment(CENTER);
			setValue("NULL");
			setFont(new Font(table.getFont().getFontName(), Font.ITALIC, table.getFont().getSize()));
		}
		else if (!Utils.isNumber(value))
		{
			setHorizontalAlignment(LEFT);
			setValue(" " + value);
		}
		else
		{
			setHorizontalAlignment(RIGHT);
			setValue(value + " ");
		}

		return this;
	}

	/*
	 * The following methods are overridden as a performance measure to to prune
	 * code-paths are often called in the case of renders but which we know are
	 * unnecessary. Great care should be taken when writing your own renderer to
	 * weigh the benefits and drawbacks of overriding methods like these.
	 */

	@Override
	public boolean isOpaque()
	{
		Color back = getBackground();
		Component p = getParent();
		if (p != null)
		{
			p = p.getParent();
		}
		// p should now be the JTable.
		boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
		return !colorMatch && super.isOpaque();
	}

	@Override
	public void invalidate()
	{}

	@Override
	public void validate()
	{}

	@Override
	public void revalidate()
	{}

	@Override
	public void repaint(long tm, int x, int y, int width, int height)
	{}

	@Override
	public void repaint(Rectangle r)
	{}

	@Override
	public void repaint()
	{}

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		// Strings get interned...
		if (propertyName == "text"
				|| propertyName == "labelFor"
				|| propertyName == "displayedMnemonic"
				|| ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null))
		{

			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue)
	{}

	protected void setValue(Object value)
	{
		setText((value == null) ? "" : value.toString());
	}

	public static class UIResource extends DefaultTableCellRenderer implements javax.swing.plaf.UIResource
	{
		private static final long serialVersionUID = -1949161536210810592L;
	}

}
