package com.commander4j.barcode;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.commander4j.cmd.ZPLCmd;
import com.commander4j.memory.ZPLMemory;
import com.commander4j.util.ZPLUtility;
import com.commander4j.zpl.ZPLPropertyStore;

import uk.org.okapibarcode.backend.HumanReadableLocation;
import uk.org.okapibarcode.backend.Pdf417;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.Java2DRenderer;

public class ZPLBarcode_PDF417
{
	private ZPLUtility util = new ZPLUtility();
	
	public boolean getParameters(ZPLCmd cmd,ZPLMemory memory)
	{
		boolean result = true;

		memory.bps.store("^BY", ZPLPropertyStore.Param_Barcode_Type, ZPLBarcode_Types.B7_PDF417);
				
		memory.bps.store("^B7",ZPLPropertyStore.Param_Orientation,util.getStringWithDefault(cmd, 0, memory.bps.recallAsStringWithDefault("^FW",ZPLPropertyStore.Param_Orientation, "N")));	

		memory.bps.store("^B7",ZPLPropertyStore.Param_Barcode_Height,util.getIntWithDefault(cmd, 1, memory.bps.recallAsIntegerWithDefault("^BY",ZPLPropertyStore.Param_Barcode_Height, 100)));	

		memory.bps.store("^B7",ZPLPropertyStore.Param_Security_Level,(util.getIntWithDefault(cmd, 2, 0)));	
		
		memory.bps.store("^B7",ZPLPropertyStore.Param_No_Cols_To_Encode,(util.getIntWithDefault(cmd, 3, 15)));	
		
		memory.bps.store("^B7",ZPLPropertyStore.Param_No_Rows_To_Encode,(util.getIntWithDefault(cmd, 4, 30)));	
		
		memory.bps.store("^B7",ZPLPropertyStore.Param_Truncate,(util.getStringWithDefault(cmd, 5, "N")));	
		
		
		return result;
	}
	
	public boolean create(Graphics g,float magnification,ZPLMemory memory)
	{

		boolean result = false;

		Graphics2D g2d = (Graphics2D) g;

		Pdf417 bean = new Pdf417();
		

		int rowheightdots =  memory.bps.recallAsIntegerWithDefault("^B7",ZPLPropertyStore.Param_Barcode_Height,2);
		
		int wdots = memory.bps.recallAsIntegerWithDefault("^B7",ZPLPropertyStore.Param_Module_Width,2);
		
		double rowheightmodules = (wdots > 0) ? (rowheightdots) : rowheightdots;
		
		bean.setModuleWidth(wdots);
		
		bean.setBarHeight  ((int) Math.round(rowheightmodules));

		bean.setPreferredEccLevel(memory.bps.recallAsIntegerWithDefault("^B7", ZPLPropertyStore.Param_Security_Level, 0));
		
		if (memory.bps.recallAsStringWithDefault("^B7", ZPLPropertyStore.Param_No_Cols_To_Encode, "").equals("")==false)
		{	
			bean.setDataColumns(Integer.valueOf(memory.bps.recallAsStringWithDefault("^B7", ZPLPropertyStore.Param_No_Cols_To_Encode, "1")));
			
		}
		
		int b7Cols = memory.bps.recallAsIntegerWithDefault("^B7",ZPLPropertyStore.Param_No_Cols_To_Encode,2);
		int b7Rows = memory.bps.recallAsIntegerWithDefault("^B7",ZPLPropertyStore.Param_No_Rows_To_Encode,2);
		
		bean.setDataColumns(b7Cols);
		bean.setRows(b7Rows);
		
		bean.setQuietZoneHorizontal(0);
		bean.setQuietZoneVertical(0);
		
		int barHeight = 0;

		bean.setHumanReadableLocation(HumanReadableLocation.NONE);

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
