package org.noxfire.edt.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class DnDTabbedPane extends JTabbedPane
{
	private static final long serialVersionUID = 1L;
	private static final int LINEWIDTH = 3;
	private static final String NAME = "test";
	private final GhostGlassPane glassPane = new GhostGlassPane();
	private final Rectangle lineRect = new Rectangle();
	private final Color lineColor = new Color(0, 100, 255);
	private int dragTabIndex = -1;

	public DnDTabbedPane(int tabPlacement, int tabLayoutPolicy)
	{
		super(tabPlacement, tabLayoutPolicy);
		final DragSourceListener dsl = new DragSourceListener()
		{
			@Override
			public void dragEnter(DragSourceDragEvent e)
			{
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			@Override
			public void dragExit(DragSourceEvent e)
			{
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				lineRect.setRect(0, 0, 0, 0);
				glassPane.setPoint(new Point(-1000, -1000));
				glassPane.repaint();
			}

			@Override
			public void dragOver(DragSourceDragEvent e)
			{
				// e.getLocation()
				// This method returns a Point indicating the cursor location in
				// screen coordinates at the moment
				Point tabPt = e.getLocation();
				SwingUtilities.convertPointFromScreen(tabPt, DnDTabbedPane.this);
				Point glassPt = e.getLocation();
				SwingUtilities.convertPointFromScreen(glassPt, glassPane);
				int targetIdx = getTargetTabIndex(glassPt);
				if (getTabAreaBound().contains(tabPt) && targetIdx >= 0 && targetIdx != dragTabIndex
						&& targetIdx != dragTabIndex + 1)
				{
					e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				}
				else
				{
					e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			}

			@Override
			public void dragDropEnd(DragSourceDropEvent e)
			{
				lineRect.setRect(0, 0, 0, 0);
				dragTabIndex = -1;
				if (hasGhost())
				{
					glassPane.setVisible(false);
					glassPane.setImage(null);
				}
			}

			@Override
			public void dropActionChanged(DragSourceDragEvent e)
			{}
		};
		final Transferable t = new Transferable()
		{
			private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);

			@Override
			public Object getTransferData(DataFlavor flavor)
			{
				return DnDTabbedPane.this;
			}

			@Override
			public DataFlavor[] getTransferDataFlavors()
			{
				DataFlavor[] f = new DataFlavor[1];
				f[0] = this.FLAVOR;
				return f;
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor)
			{
				return flavor.getHumanPresentableName().equals(NAME);
			}
		};
		final DragGestureListener dgl = new DragGestureListener()
		{
			@Override
			public void dragGestureRecognized(DragGestureEvent e)
			{
				Point tabPt = e.getDragOrigin();
				dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
				// another solution of a "disabled tab problem".
				// if(dragTabIndex < 0 || !isEnabledAt(dragTabIndex)) return;
				if (dragTabIndex < 0)
					return;
				initGlassPane(e.getComponent(), e.getDragOrigin());
				try
				{
					e.startDrag(DragSource.DefaultMoveDrop, t, dsl);
				}
				catch (InvalidDnDOperationException idoe)
				{
					idoe.printStackTrace();
				}
			}
		};
		// dropTarget =
		new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
	}

	class CDropTargetListener implements DropTargetListener
	{
		@Override
		public void dragEnter(DropTargetDragEvent e)
		{
			if (isDragAcceptable(e))
				e.acceptDrag(e.getDropAction());
			else
				e.rejectDrag();
		}

		@Override
		public void dragExit(DropTargetEvent e)
		{}

		@Override
		public void dropActionChanged(DropTargetDragEvent e)
		{}

		@Override
		public void dragOver(final DropTargetDragEvent e)
		{
			Point pt = e.getLocation();
			if (getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM)
			{
				initTargetLeftRightLine(getTargetTabIndex(pt));
			}
			else
			{
				initTargetTopBottomLine(getTargetTabIndex(pt));
			}
			repaint();
			if (hasGhost())
			{
				glassPane.setPoint(pt);
				glassPane.repaint();
			}
			// autoScrollTest(pt);
		}

		@Override
		public void drop(DropTargetDropEvent e)
		{
			if (isDropAcceptable(e))
			{
				convertTab(dragTabIndex, getTargetTabIndex(e.getLocation()));
				e.dropComplete(true);
			}
			else
			{
				e.dropComplete(false);
			}
			repaint();
		}

		public boolean isDragAcceptable(DropTargetDragEvent e)
		{
			Transferable t = e.getTransferable();
			if (t == null)
				return false;
			DataFlavor[] f = e.getCurrentDataFlavors();
			if (t.isDataFlavorSupported(f[0]) && dragTabIndex >= 0)
			{
				return true;
			}
			return false;
		}

		public boolean isDropAcceptable(DropTargetDropEvent e)
		{
			Transferable t = e.getTransferable();
			if (t == null)
				return false;
			DataFlavor[] f = t.getTransferDataFlavors();
			if (t.isDataFlavorSupported(f[0]) && dragTabIndex >= 0)
			{
				return true;
			}
			return false;
		}
	}
	private boolean hasGhost = true;

	public void setPaintGhost(boolean flag)
	{
		hasGhost = flag;
	}

	public boolean hasGhost()
	{
		return hasGhost;
	}

	private int getTargetTabIndex(Point glassPt)
	{
		Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, DnDTabbedPane.this);
		boolean isTB = getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM;
		for (int i = 0; i < getTabCount(); i++)
		{
			Rectangle r = getBoundsAt(i);
			if (isTB)
				r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
			else
				r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
			if (r.contains(tabPt))
				return i;
		}
		Rectangle r = getBoundsAt(getTabCount() - 1);
		if (isTB)
			r.setRect(r.x + r.width / 2, r.y, r.width, r.height);
		else
			r.setRect(r.x, r.y + r.height / 2, r.width, r.height);
		return r.contains(tabPt) ? getTabCount() : -1;
	}

	private void convertTab(int prev, int next)
	{
		if (next < 0 || prev == next)
		{
			return;
		}
		Component cmp = getComponentAt(prev);
		Component tab = getTabComponentAt(prev);
		String str = getTitleAt(prev);
		Icon icon = getIconAt(prev);
		String tip = getToolTipTextAt(prev);
		boolean flg = isEnabledAt(prev);
		int tgtindex = prev > next ? next : next - 1;
		remove(prev);
		insertTab(str, icon, cmp, tip, tgtindex);
		setEnabledAt(tgtindex, flg);
		// When you drag'n'drop a disabled tab, it finishes enabled and
		// selected.
		// pointed out by dlorde
		if (flg)
			setSelectedIndex(tgtindex);

		// I have a component in all tabs (jlabel with an X to close the tab)
		// and when i move a tab the component disappear.
		// pointed out by Daniel Dario Morales Salas
		setTabComponentAt(tgtindex, tab);
	}

	private void initTargetLeftRightLine(int next)
	{
		if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1)
		{
			lineRect.setRect(0, 0, 0, 0);
		}
		else if (next == 0)
		{
			Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
			lineRect.setRect(r.x - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
		}
		else
		{
			Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next - 1), glassPane);
			lineRect.setRect(r.x + r.width - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
		}
	}

	private void initTargetTopBottomLine(int next)
	{
		if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1)
		{
			lineRect.setRect(0, 0, 0, 0);
		}
		else if (next == 0)
		{
			Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
			lineRect.setRect(r.x, r.y - LINEWIDTH / 2, r.width, LINEWIDTH);
		}
		else
		{
			Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next - 1), glassPane);
			lineRect.setRect(r.x, r.y + r.height - LINEWIDTH / 2, r.width, LINEWIDTH);
		}
	}

	private void initGlassPane(Component c, Point tabPt)
	{
		getRootPane().setGlassPane(glassPane);
		if (hasGhost())
		{
			Rectangle rect = getBoundsAt(dragTabIndex);
			BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			c.paint(g);
			if (rect.x < 0)
				rect.x = 0;
			image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
			glassPane.setImage(image);
		}
		Point glassPt = SwingUtilities.convertPoint(c, tabPt, glassPane);
		glassPane.setPoint(glassPt);
		glassPane.setVisible(true);
	}

	private Rectangle getTabAreaBound()
	{
		Rectangle lastTab = getUI().getTabBounds(this, getTabCount() - 1);
		return new Rectangle(0, 0, getWidth(), lastTab.y + lastTab.height);
	}

	class GhostGlassPane extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private final AlphaComposite composite;
		private Point location = new Point(0, 0);
		private BufferedImage draggingGhost = null;

		public GhostGlassPane()
		{
			setOpaque(false);
			composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		}

		public void setImage(BufferedImage draggingGhost)
		{
			this.draggingGhost = draggingGhost;
		}

		public void setPoint(Point location)
		{
			this.location = location;
		}

		@Override
		public void paintComponent(Graphics g)
		{
			if (draggingGhost == null)
				return;
			Graphics2D g2 = (Graphics2D)g;
			g2.setComposite(composite);
			double xx = location.getX() - (draggingGhost.getWidth(this) / 2d);
			double yy = location.getY() - (draggingGhost.getHeight(this) / 2d);
			g2.drawImage(draggingGhost, (int)xx, (int)yy, null);

			if (dragTabIndex >= 0)
			{
				g2.setPaint(lineColor);
				g2.fill(lineRect);
			}
		}
	}
}