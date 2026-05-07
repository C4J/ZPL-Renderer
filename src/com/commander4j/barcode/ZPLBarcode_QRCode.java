package com.commander4j.barcode;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.commander4j.cmd.ZPLCmd;
import com.commander4j.memory.ZPLMemory;
import com.commander4j.util.ZPLUtility;
import com.commander4j.zpl.ZPLPropertyStore;

import uk.org.okapibarcode.backend.QrCode;
import uk.org.okapibarcode.backend.QrCode.EccLevel;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.Java2DRenderer;

public class ZPLBarcode_QRCode
{
	private ZPLUtility util = new ZPLUtility();

	public boolean getParameters(ZPLCmd cmd, ZPLMemory memory)
	{
		boolean result = true;

		memory.bps.store("^BY", ZPLPropertyStore.Param_Barcode_Type, ZPLBarcode_Types.BQ_QRCode);

		memory.bps.store("^BQ", ZPLPropertyStore.Param_Orientation, util.getStringWithDefault(cmd, 0, "N"));

		memory.bps.store("^BQ", ZPLPropertyStore.Param_Model, util.getIntWithDefault(cmd, 1, 2));

		memory.bps.store("^BQ", ZPLPropertyStore.Param_Magnification, util.getIntWithDefault(cmd, 2, 3));

		memory.bps.store("^BQ", ZPLPropertyStore.Param_Error_Correction, util.getStringWithDefault(cmd, 3, "Q"));

		memory.bps.store("^BQ", ZPLPropertyStore.Param_Mask_Value, util.getIntWithDefault(cmd, 4, 7));

		return result;
	}

	public boolean create(Graphics g, float magnification, ZPLMemory memory)
	{
		Graphics2D g2d = (Graphics2D) g;

		// ZPL ^FD for ^BQ carries an inline header that may override the ^BQ
		// error-correction argument: <H|Q|M|L><A|M>[,[<N|A|B[len]|K>,]]<data>
		// The header is stripped and the ECC level promoted into ^BQ before encoding.
		String rawFd = memory.bps.recallAsStringWithDefault("^FD", ZPLPropertyStore.Param_Text, "");
		String dataField = stripQrFdHeader(rawFd, memory);

		QrCode bean = new QrCode();
		switch (memory.bps.recallAsStringWithDefault("^BQ", ZPLPropertyStore.Param_Error_Correction, "Q"))
		{
			case "H": bean.setPreferredEccLevel(EccLevel.H); break;
			case "M": bean.setPreferredEccLevel(EccLevel.M); break;
			case "L": bean.setPreferredEccLevel(EccLevel.L); break;
			default:  bean.setPreferredEccLevel(EccLevel.Q); break;
		}

		// ^BQ c-parameter is "magnification" in printer dots per module (1..10).
		// Drive Okapi's natural module size with this; Java2DRenderer then scales
		// printer-dot space to screen-pixel space using the UI magnification.
		int zplMagnification = memory.bps.recallAsIntegerWithDefault("^BQ", ZPLPropertyStore.Param_Magnification, 3);
		bean.setModuleWidth(Math.max(1, zplMagnification));
		bean.setQuietZoneHorizontal(0);
		bean.setQuietZoneVertical(0);

		bean.setContent(dataField);

		int width = (int) (bean.getWidth() * magnification);
		int height = (int) (bean.getHeight() * magnification);
		if (width <= 0 || height <= 0)
		{
			return false;
		}

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig = img.createGraphics();
		try
		{
			ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			ig.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			Java2DRenderer renderer = new Java2DRenderer(ig, magnification, Color.WHITE, Color.BLACK);
			renderer.render(bean);
		}
		finally
		{
			ig.dispose();
		}

		int x = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "X", 0);
		int y = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "Y", 0);
		if ("^FT".equals(memory.anchor_mode))
		{
			y = y - height;
		}

		g2d.drawImage(img, x, y, null);
		return true;
	}

	private String stripQrFdHeader(String fd, ZPLMemory memory)
	{
		if (fd == null || fd.length() < 2)
		{
			return fd == null ? "" : fd;
		}

		char ecc = fd.charAt(0);
		char dataInput = fd.charAt(1);
		if ("HQML".indexOf(ecc) < 0 || (dataInput != 'A' && dataInput != 'M'))
		{
			return fd;
		}

		memory.bps.store("^BQ", ZPLPropertyStore.Param_Error_Correction, String.valueOf(ecc));

		int idx = 2;
		if (idx < fd.length() && fd.charAt(idx) == ',')
		{
			idx++;
		}

		if (dataInput == 'M' && idx < fd.length() && "NABK".indexOf(fd.charAt(idx)) >= 0)
		{
			idx++;
			while (idx < fd.length() && Character.isDigit(fd.charAt(idx)))
			{
				idx++;
			}
			if (idx < fd.length() && fd.charAt(idx) == ',')
			{
				idx++;
			}
		}

		return fd.substring(idx);
	}
}
