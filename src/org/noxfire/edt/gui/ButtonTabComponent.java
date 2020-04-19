/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc. All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: -
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - Neither the name of Sun Microsystems nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission. THIS SOFTWARE IS
 * PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.noxfire.edt.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.noxfire.edt.gui.TabButton.TabButtonType;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class ButtonTabComponent extends JPanel
{
	private static final long serialVersionUID = 1L;

	public final JTabbedPane pane;
	private final JLabel label;
	private final JButton btnClose;
	private final JButton btnReload;

	public void setTitle(String text)
	{
		pane.setTitleAt(getTabIndex(), text);
	}

	public String getTitle()
	{
		return pane.getTitleAt(getTabIndex());
	}

	public void addActionListenerToReloadButton(ActionListener l)
	{
		if (btnReload != null)
			btnReload.addActionListener(l);
	}

	public void removeActionListenerFromReloadButton(ActionListener l)
	{
		if (btnReload != null)
			btnReload.removeActionListener(l);
	}

	public ButtonTabComponent(final JTabbedPane pane, boolean showReloadButton)
	{
		// unset default FlowLayout' gaps
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		if (pane == null)
		{
			throw new NullPointerException("TabbedPane is null");
		}
		this.pane = pane;
		setOpaque(false);

		// make JLabel read titles from JTabbedPane
		label = new JLabel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getText()
			{
				int i = pane.indexOfTabComponent(ButtonTabComponent.this);
				if (i != -1)
				{
					return pane.getTitleAt(i);
				}
				return null;
			}
		};
		add(label);
		// add more space between the label and the button
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

		// reload button
		if (showReloadButton)
		{
			btnReload = new TabButton(TabButtonType.RELOAD);
			add(btnReload);
		}
		else
			btnReload = null;
		// close button
		btnClose = new TabButton();
		btnClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				closeTab();
			}
		});
		add(btnClose);
		// add more space to the top of the component
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

		this.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				pane.dispatchEvent(SwingUtilities.convertMouseEvent(ButtonTabComponent.this, e, pane));
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				pane.dispatchEvent(SwingUtilities.convertMouseEvent(ButtonTabComponent.this, e, pane));
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				pane.dispatchEvent(SwingUtilities.convertMouseEvent(ButtonTabComponent.this, e, pane));
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				pane.dispatchEvent(SwingUtilities.convertMouseEvent(ButtonTabComponent.this, e, pane));
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				pane.dispatchEvent(SwingUtilities.convertMouseEvent(ButtonTabComponent.this, e, pane));
			}
		});
		this.addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				pane.dispatchEvent(SwingUtilities.convertMouseEvent(ButtonTabComponent.this, e, pane));
			}

			@Override
			public void mouseMoved(MouseEvent e)
			{
				pane.dispatchEvent(SwingUtilities.convertMouseEvent(ButtonTabComponent.this, e, pane));
			}
		});
	}

	public void closeTab()
	{
		if (getTabIndex() != -1)
		{
			pane.remove(getTabIndex());
		}
	}

	public int getTabIndex()
	{
		return pane.indexOfTabComponent(this);
	}

	public void setIcon(Icon icon)
	{
		label.setIcon(icon);
	}

	@Override
	public void repaint()
	{
		if (btnClose != null)
			btnClose.repaint();
		super.repaint();
	}
}
