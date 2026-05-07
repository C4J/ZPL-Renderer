package com.commander4j.util;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.RepaintManager;

import com.commander4j.zpl.ZPLPanel;

public class JPanelToPNG
{

	private static final float OUTPUT_MAGNIFICATION = 1.0f;

	public static int savePanelAsPNG(JPanel parent, File chosenFile) throws Exception
	{
		List<ZPLPanel> pages = new ArrayList<>();
		for (Component c : parent.getComponents())
		{
			if (c instanceof ZPLPanel zp)
			{
				pages.add(zp);
			}
		}

		if (pages.isEmpty())
		{
			throw new IllegalArgumentException("No ZPLPanel children found.");
		}

		RepaintManager rm = RepaintManager.currentManager(parent);
		boolean oldDb = rm.isDoubleBufferingEnabled();
		rm.setDoubleBufferingEnabled(false);

		try
		{
			int pageIndex = 1;
			for (ZPLPanel child : pages)
			{
				BufferedImage image = child.renderAtMagnification(OUTPUT_MAGNIFICATION);
				ImageIO.write(image, "PNG", pageFile(chosenFile, pageIndex));
				pageIndex++;
			}
		}
		finally
		{
			rm.setDoubleBufferingEnabled(oldDb);
		}

		return pages.size();
	}

	private static File pageFile(File chosenFile, int pageIndex)
	{
		String name = chosenFile.getName();
		String dir = chosenFile.getParent();
		int dot = name.lastIndexOf('.');

		String stem;
		String ext;
		if (dot > 0 && name.substring(dot + 1).equalsIgnoreCase("png"))
		{
			stem = name.substring(0, dot);
			ext = name.substring(dot);
		}
		else
		{
			stem = name;
			ext = ".png";
		}

		String paged = stem + "_" + pageIndex + ext;
		return dir == null ? new File(paged) : new File(dir, paged);
	}
}
