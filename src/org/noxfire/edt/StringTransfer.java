package org.noxfire.edt;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class StringTransfer implements Transferable
{
	private String content;

	public StringTransfer(String content)
	{
		this.content = content;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,
			IOException
	{
		return content;
	}

	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { DataFlavor.stringFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor == DataFlavor.stringFlavor;
	}
}