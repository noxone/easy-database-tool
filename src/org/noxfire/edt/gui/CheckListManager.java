package org.noxfire.edt.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CheckListManager extends MouseAdapter implements ListSelectionListener, ActionListener
{
	private ListSelectionModel selectionModel = new DefaultListSelectionModel();
	private JList list = new JList();
	int hotspot = new JCheckBox().getPreferredSize().width;

	public CheckListManager(JList list)
	{
		this.list = list;
		list.setCellRenderer(new CheckListCellRenderer(list.getCellRenderer(), selectionModel));
		list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
		list.addMouseListener(this);
		selectionModel.addListSelectionListener(this);
	}

	public ListSelectionModel getSelectionModel()
	{
		return selectionModel;
	}

	private void toggleSelection(int index)
	{
		if (index < 0)
			return;

		if (selectionModel.isSelectedIndex(index))
			selectionModel.removeSelectionInterval(index, index);
		else
			selectionModel.addSelectionInterval(index, index);
	}

	/*------------------------------[ MouseListener ]-------------------------------------*/

	@Override
	public void mouseClicked(MouseEvent me)
	{
		int index = list.locationToIndex(me.getPoint());
		if (index < 0)
			return;
		if (me.getX() > list.getCellBounds(index, index).x + hotspot)
			return;
		toggleSelection(index);
	}

	/*-----------------------------[ ListSelectionListener ]---------------------------------*/

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		list.repaint(list.getCellBounds(e.getFirstIndex(), e.getLastIndex()));
	}

	/*-----------------------------[ ActionListener ]------------------------------*/

	@Override
	public void actionPerformed(ActionEvent e)
	{
		toggleSelection(list.getSelectedIndex());
	}

	public static class CheckListCellRenderer extends JPanel implements ListCellRenderer
	{
		private static final long serialVersionUID = -1683114811332681785L;
		private ListCellRenderer delegate;
		private ListSelectionModel selectionModel;
		private JCheckBox checkBox = new JCheckBox();

		public CheckListCellRenderer(ListCellRenderer renderer, ListSelectionModel selectionModel)
		{
			this.delegate = renderer;
			this.selectionModel = selectionModel;
			setLayout(new BorderLayout());
			setOpaque(false);
			checkBox.setOpaque(false);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
		{
			Component renderer = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			checkBox.setSelected(selectionModel.isSelectedIndex(index));
			removeAll();
			add(checkBox, BorderLayout.WEST);
			add(renderer, BorderLayout.CENTER);
			return this;
		}
	}
}
