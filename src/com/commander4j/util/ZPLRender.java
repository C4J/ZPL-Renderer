package com.commander4j.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.commander4j.cmd.ZPLCmd;
import com.commander4j.cmd.ZPLCmdInfo;
import com.commander4j.cmd.ZPLCmdList;
import com.commander4j.memory.ZPLMemory;
import com.commander4j.zpl.ZPLCommon;
import com.commander4j.zpl.ZPLPanel;
import com.commander4j.zpl.ZPLParser;

/**
 * Off-GUI ZPL rendering.
 *
 * Each invocation registers a fresh {@link ZPLMemory} under a per-call UUID in
 * {@link ZPLCommon#config}, so concurrent callers do not share parser state,
 * font caches, or label dimensions. The UUID is removed in a finally block.
 *
 * Returned images are one-per-printable-label (the same definition the GUI
 * uses: a label is "printable" if it contains at least one command whose
 * {@link ZPLCmdInfo#prints} flag is true).
 */
public final class ZPLRender
{

	private ZPLRender()
	{
	}

	public static List<BufferedImage> renderToImages(String zpl, int dpi, double labelWidth, double labelHeight, String uom, float magnification)
	{
		String uuid = UUID.randomUUID().toString();
		ZPLCommon.init(uuid);
		try
		{
			ZPLMemory mem = ZPLCommon.config.get(uuid);
			mem.printerDPI = dpi;
			mem.labelSizeWidth = (float) labelWidth;
			mem.labelSizeHeight = (float) labelHeight;
			mem.labelSizeUOM = uom;

			int pixelW;
			int pixelH;
			if ("cm".equalsIgnoreCase(uom))
			{
				pixelW = (int) Math.round((labelWidth / 2.54) * dpi * magnification);
				pixelH = (int) Math.round((labelHeight / 2.54) * dpi * magnification);
			}
			else
			{
				pixelW = (int) Math.round(labelWidth * dpi * magnification);
				pixelH = (int) Math.round(labelHeight * dpi * magnification);
			}
			mem.printerMariginRight = pixelW;
			mem.printerMariginBottom = pixelH;

			ZPLParser parser = new ZPLParser(uuid);
			ZPLCmdList list = parser.parseBytes(zpl == null ? "" : zpl);

			List<LinkedList<ZPLCmd>> pages = groupPages(list.getCommands(), mem);

			List<BufferedImage> result = new ArrayList<>(pages.size());
			int pageNo = 1;
			for (LinkedList<ZPLCmd> pageCmds : pages)
			{
				ZPLPanel panel = new ZPLPanel(pageCmds, uuid, "", magnification, pageNo);
				panel.setSize(pixelW, pixelH);
				panel.doLayout();
				result.add(panel.renderAtMagnification(magnification));
				pageNo++;
			}
			return result;
		}
		finally
		{
			ZPLCommon.config.remove(uuid);
		}
	}

	private static List<LinkedList<ZPLCmd>> groupPages(LinkedList<ZPLCmd> commands, ZPLMemory mem)
	{
		List<LinkedList<ZPLCmd>> pages = new ArrayList<>();
		LinkedList<ZPLCmd> current = null;
		boolean withinLabel = false;
		boolean labelPrints = false;

		for (ZPLCmd cmd : commands)
		{
			String c = cmd.getCommand();

			if ("^XA".equals(c))
			{
				withinLabel = true;
				current = new LinkedList<>();
				labelPrints = false;
			}

			if (withinLabel && current != null)
			{
				current.add(cmd);
				ZPLCmdInfo info = mem.zplindex.zplDescription.get(c);
				if (info != null && info.prints)
				{
					labelPrints = true;
				}
			}

			if ("^XZ".equals(c))
			{
				if (withinLabel && labelPrints && current != null)
				{
					pages.add(current);
				}
				withinLabel = false;
				current = null;
				labelPrints = false;
			}
		}

		return pages;
	}
}
