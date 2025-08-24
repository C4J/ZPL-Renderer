package com.commander4j.barcode;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.commander4j.cmd.ZPLCmd;
import com.commander4j.font.ZPLFontCache;
import com.commander4j.memory.ZPLMemory;
import com.commander4j.util.ZPLUtility;
import com.commander4j.zpl.ZPLPropertyStore;

import uk.org.okapibarcode.backend.AztecCode;
import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.graphics.TextAlignment;
import uk.org.okapibarcode.output.Java2DRenderer;

public class ZPLBarcode_Aztec
{
	private ZPLUtility util = new ZPLUtility();
	
	
	public boolean getParameters(ZPLCmd cmd,ZPLMemory memory)
	{
		boolean result = true;

		memory.bps.store("^BY",ZPLPropertyStore.Param_Barcode_Type, ZPLBarcode_Types.B0_Aztec);
				
		memory.bps.store( "^B0",ZPLPropertyStore.Param_Orientation,util.getStringWithDefault(cmd, 0, memory.bps.recallAsStringWithDefault("^FW",ZPLPropertyStore.Param_Orientation, "N")));	

		memory.bps.store( "^B0",ZPLPropertyStore.Param_Magnification,(util.getIntWithDefault(cmd, 1, 3)));
		
		memory.bps.store( "^B0",ZPLPropertyStore.Param_ECICs,(util.getStringWithDefault(cmd, 2, "N")));	
		
		memory.bps.store( "^B0",ZPLPropertyStore.Param_Error_Control_Symbol_Size_Type,(util.getIntWithDefault(cmd, 3, 33)));	
		
		memory.bps.store( "^B0",ZPLPropertyStore.Param_Menu_Symbol_Indicator,(util.getStringWithDefault(cmd, 4, "N")));	
		
		memory.bps.store( "^B0",ZPLPropertyStore.Param_No_Of_Symbols,(util.getIntWithDefault(cmd, 5, 1)));	
		
		memory.bps.store( "^B0",ZPLPropertyStore.Param_Optional_ID_Field,(util.getStringWithDefault(cmd, 6, "")));	
			
		return result;
	}
	
	public boolean create(Graphics g,float magnification,ZPLMemory memory)
	{

		boolean result = false;

		Graphics2D g2d = (Graphics2D) g;

		AztecCode bean = new AztecCode();
		
	    //int moduleScale = memory.bps.recallAsIntegerWithDefault("^B0", ZPLPropertyStore.Param_Magnification, 7);
		
		bean.setEciMode(memory.bps.recallAsIntegerWithDefault( "^B0", ZPLPropertyStore.Param_Error_Control_Symbol_Size_Type,0));
		
		bean.setModuleWidth((int) memory.bps.recallAsDoubleWithDefault("^BY",ZPLPropertyStore.Param_Module_Width,0.2));
		
		bean.setBarHeight((int) (memory.bps.recallAsDoubleWithDefault("^B0",ZPLPropertyStore.Param_Barcode_Height,100)));
		
		bean.setQuietZoneHorizontal(0);
		bean.setQuietZoneVertical(0);
		
		int barHeight = 0;

		if (memory.bps.recallAsStringWithDefault("^B1",ZPLPropertyStore.Param_Barcode_Interpretation,"N").equals("Y"))
		{

			ZPLFontCache ch = memory.zplFont.recallFont(g2d, new ZPLCmd(""), memory, magnification,true);
			
			bean.setFont(ch.font);
			
			if (memory.bps.recallAsStringWithDefault("^B0",ZPLPropertyStore.Param_Barcode_Interpretation_Above,"N").equals("Y"))
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

		BufferedImage barcodeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D imageG2D = barcodeImage.createGraphics();
		
		imageG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		imageG2D.fillRect(0, 0, width, height);
		
		Java2DRenderer renderer = new Java2DRenderer(imageG2D, magnification, Color.WHITE, Color.BLACK);
		renderer.render(bean);
		imageG2D.dispose();

		int modeAdjust = 0;
		if (memory.anchor_mode.equals("^FT"))
		{
			modeAdjust = barHeight;
		}
		
		int x = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "X", 0);
		int y = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "Y", 0);

		g2d.drawImage(barcodeImage, x, y-modeAdjust, null);
		
		return result;
	
	}
}
