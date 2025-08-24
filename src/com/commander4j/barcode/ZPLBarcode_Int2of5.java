package com.commander4j.barcode;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.commander4j.cmd.ZPLCmd;
import com.commander4j.font.ZPLFontCache;
import com.commander4j.memory.ZPLMemory;
import com.commander4j.util.ZPLUtility;
import com.commander4j.zpl.ZPLPropertyStore;

import uk.org.okapibarcode.backend.Code2Of5;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.graphics.TextAlignment;
import uk.org.okapibarcode.output.Java2DRenderer;

public class ZPLBarcode_Int2of5
{
	private ZPLUtility util = new ZPLUtility();
	
	public boolean getParameters(ZPLCmd cmd,ZPLMemory memory)
	{
		boolean result = true;

		memory.bps.store("^BY", ZPLPropertyStore.Param_Barcode_Type, ZPLBarcode_Types.B2_2of5);
				
		memory.bps.store("^B2",ZPLPropertyStore.Param_Orientation,util.getStringWithDefault(cmd, 0, memory.bps.recallAsStringWithDefault("^FW",ZPLPropertyStore.Param_Orientation, "N")));	

		memory.bps.store("^B2",ZPLPropertyStore.Param_Barcode_Height,util.getIntWithDefault(cmd, 1, memory.bps.recallAsIntegerWithDefault("^BY",ZPLPropertyStore.Param_Barcode_Height, 100)));	

		memory.bps.store("^B2",ZPLPropertyStore.Param_Barcode_Interpretation,(util.getStringWithDefault(cmd, 2, "Y")));	
		
		memory.bps.store("^B2",ZPLPropertyStore.Param_Barcode_Interpretation_Above,(util.getStringWithDefault(cmd, 3, "N")));	
		
		memory.bps.store("^B2",ZPLPropertyStore.Param_Mod10_CheckDigit,(util.getStringWithDefault(cmd, 4, "N")));
		
		
		return result;
	}
	
	public boolean create(Graphics g,float magnification,ZPLMemory memory)
	{

		boolean result = false;

		Graphics2D g2d = (Graphics2D) g;

		Code2Of5 bean = new Code2Of5();
		
		bean.setModuleWidth((int) memory.bps.recallAsDoubleWithDefault("^BY",ZPLPropertyStore.Param_Module_Width,0.2));
		
		bean.setBarHeight((int) (memory.bps.recallAsDoubleWithDefault("^B2",ZPLPropertyStore.Param_Barcode_Height,100)));
		
		bean.setQuietZoneHorizontal(0);
		bean.setQuietZoneVertical(0);
		
		int barHeight = 0;
		
		if (memory.bps.recallAsStringWithDefault("^B2",ZPLPropertyStore.Param_Barcode_Interpretation,"N").equals("Y"))
		{
			ZPLFontCache ch = memory.zplFont.recallFont(g2d, new ZPLCmd(""), memory, magnification,true);
			
			bean.setFont(ch.font);
			
			if (memory.bps.recallAsStringWithDefault("^B2",ZPLPropertyStore.Param_Barcode_Interpretation_Above,"N").equals("Y"))
			{
				bean.setHumanReadableLocation(HumanReadableLocation.TOP);
			}
			else
			{
				bean.setHumanReadableLocation(HumanReadableLocation.BOTTOM);
			}
			bean.setHumanReadableAlignment(TextAlignment.CENTER);
		}
		else
		{
			bean.setHumanReadableLocation(HumanReadableLocation.NONE);
		}
		
		barHeight = bean.getBarHeight();
		barHeight= (int) (barHeight*magnification);
		
		bean.setContent(memory.bps.recallAsStringWithDefault("^FD", ZPLPropertyStore.Param_Text, ""));

		int width = (int) (bean.getWidth()*magnification);
		int height = (int) (bean.getHeight()*magnification);

		// 2. Render to off-screen image
		BufferedImage barcodeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D imageG2D = barcodeImage.createGraphics();

		Java2DRenderer renderer = new Java2DRenderer(imageG2D, magnification, Color.WHITE, Color.BLACK);
		renderer.render(bean);
		imageG2D.dispose();

		int modeAdjust = 0;
		if (memory.anchor_mode.equals("^FT"))
		{
			modeAdjust = barHeight;
		}
		
		// 3. Draw image wherever you want
		int x = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "X", 0);
		int y = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "Y", 0);

		g2d.drawImage(barcodeImage, x, y-modeAdjust, null);
		return result;
	}
}
