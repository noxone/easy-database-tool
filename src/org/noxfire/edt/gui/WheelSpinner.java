package org.noxfire.edt.gui;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class WheelSpinner extends JSpinner
{
	private static final long serialVersionUID = -5703362349983831228L;

	public WheelSpinner(int value, int minVal, int maxVal)
	{
		super(new SpinnerNumberModel(value, minVal, maxVal, 1));

		addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				int count = e.getUnitsToScroll();
				if (count < 0)
					count *= -1;
				for (int i = 0; i < count; ++i)
				{
					Object val = e.getUnitsToScroll() < 0 ? getModel().getNextValue() : getModel().getPreviousValue();
					if (val != null)
						getModel().setValue(val);
				}
			}
		});
	}

	public WheelSpinner(int value)
	{
		this(value, 0, Integer.MAX_VALUE);
	}
}
