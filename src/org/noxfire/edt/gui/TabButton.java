package org.noxfire.edt.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

public class TabButton extends JButton
{
	private static final long serialVersionUID = 1L;

	final TabButtonType type;

	public TabButton()
	{
		this(TabButtonType.CLOSE);
	}

	public TabButton(TabButtonType type)
	{
		this.type = type;
		int size = 17;
		setPreferredSize(new Dimension(size, size));
		switch (type)
		{
			case RELOAD:
				setToolTipText("Reload this tab");
				break;
			case CLOSE:
			default:
				setToolTipText("Close this tab");
				break;
		}
		// Make the button looks the same for all Laf's
		setUI(new BasicButtonUI());
		// Make it transparent
		setContentAreaFilled(false);
		// No need to be focusable
		setFocusable(false);
		setBorder(BorderFactory.createEtchedBorder());
		setBorderPainted(false);
		// Making nice rollover effect
		// we use the same listener for all buttons
		addMouseListener(buttonMouseListener);
		setRolloverEnabled(true);
	}

	// we don't want to update UI for this button
	@Override
	public void updateUI()
	{}

	// paint the cross
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g.create();
		// shift the image for pressed buttons
		if (getModel().isPressed())
		{
			g2.translate(1, 1);
		}
		g2.setStroke(new BasicStroke(2.0f));
		int delta = 5;
		switch (type)
		{
			case RELOAD:
				g2.setColor(Color.green.darker());
				if (getModel().isRollover())
					g2.setColor(Color.green);
				g2.fillOval(delta, delta, getWidth() - 2 * delta, getHeight() - 2 * delta);
				break;
			case CLOSE:
			default:
				g2.setColor(Color.BLACK);
				if (getModel().isRollover())
					g2.setColor(Color.RED);
				g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
				g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
				break;
		}
		g2.dispose();
	}

	private final static MouseListener buttonMouseListener = new MouseAdapter()
	{
		@Override
		public void mouseEntered(MouseEvent e)
		{
			Component component = e.getComponent();
			if (component instanceof AbstractButton)
			{
				AbstractButton button = (AbstractButton)component;
				button.setBorderPainted(true);
			}
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			Component component = e.getComponent();
			if (component instanceof AbstractButton)
			{
				AbstractButton button = (AbstractButton)component;
				button.setBorderPainted(false);
			}
		}
	};

	public enum TabButtonType
	{
		CLOSE, RELOAD;
	}
}