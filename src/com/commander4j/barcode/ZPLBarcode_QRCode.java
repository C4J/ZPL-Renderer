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
import uk.org.okapibarcode.output.Java2DRenderer;

public class ZPLBarcode_QRCode
{
	private ZPLUtility util = new ZPLUtility();
	
	public boolean getParameters(ZPLCmd cmd,ZPLMemory memory)
	{
		boolean result = true;

		memory.bps.store("^BY", ZPLPropertyStore.Param_Barcode_Type, ZPLBarcode_Types.BQ_QRCode);

		memory.bps.store("^BQ",ZPLPropertyStore.Param_Orientation,(util.getStringWithDefault(cmd, 0, "N")));

		memory.bps.store("^BQ",ZPLPropertyStore.Param_Model,util.getIntWithDefault(cmd, 1, 2));	

		memory.bps.store("^BQ",ZPLPropertyStore.Param_Magnification,(util.getIntWithDefault(cmd, 2, 3)));	
		
		memory.bps.store("^BQ",ZPLPropertyStore.Param_Error_Correction,(util.getStringWithDefault(cmd, 3, "Q")));	

		memory.bps.store("^BQ",ZPLPropertyStore.Param_Mask_Value,(util.getIntWithDefault(cmd, 4, 7)));	
		
		return result;
	}
	
	
	public boolean create(Graphics g, float uiMagnification, ZPLMemory memory) {
	    Graphics2D g2d = (Graphics2D) g;

	    // 1) Build QR symbol (ECC + content)
	    QrCode bean = new QrCode();
	    switch (memory.bps.recallAsStringWithDefault("^BQ", ZPLPropertyStore.Param_Error_Correction, "Q")) {
	        case "H": bean.setPreferredEccLevel(EccLevel.H); break;
	        case "M": bean.setPreferredEccLevel(EccLevel.M); break;
	        case "L": bean.setPreferredEccLevel(EccLevel.L); break;
	        default:  bean.setPreferredEccLevel(EccLevel.Q); break;
	    }
	    String data = memory.bps.recallAsStringWithDefault("^FD", ZPLPropertyStore.Param_Text, "");

	    bean.setContent(data != null ? data : "");

	    // 2) Magnification (dots per module from ^BQ), converted to screen pixels
	    int zplMagnification = memory.bps.recallAsIntegerWithDefault("^BQ", ZPLPropertyStore.Param_Magnification, 3); // 1..100
	    int pxPerModule = Math.max(1, Math.round(zplMagnification * uiMagnification));

	    // 3) Symbol dimensions in modules (Okapi: width/height are in modules)
	    int modules = bean.getWidth();                 // QR is square


	    int qh = bean.getQuietZoneHorizontal();        // modules
	    int qv = bean.getQuietZoneVertical();          // modules

	    // Total image size including quiet zone (avoid clipping)
	    int imgW = (modules + 2 * qh) * pxPerModule;
	    int imgH = (modules + 2 * qv) * pxPerModule;

	    // 4) Render to an ARGB image
	    BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D ig = img.createGraphics();
	    try {
	        ig.setBackground(java.awt.Color.WHITE);
	        ig.clearRect(0, 0, imgW, imgH);
	        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	        ig.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

	        // This constructor scales each module to pxPerModule and draws including quiet zone at (0,0)
	        Java2DRenderer renderer = new Java2DRenderer(ig, pxPerModule,
	                uk.org.okapibarcode.graphics.Color.BLACK,
	                uk.org.okapibarcode.graphics.Color.WHITE);
	        renderer.render(bean);
	    } finally {
	        ig.dispose();
	    }

	    // 5) Anchor: ^FO = top-left; ^FT = bottom-left for barcodes/QR → shift up by full height
	    int x = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "X", 0);
	    int y = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "Y", 0);
	    //int modeAdjust = memory.anchor_mode.equals("^FT") ? imgH : 0;
	    
		int modeAdjust = 0;
		if (memory.anchor_mode.equals("^FT"))
		{
			modeAdjust = imgH;
		}

	    g2d.drawImage(img, x, y - modeAdjust+modeAdjust, null);
	    return true;
	}

	
	public boolean create4(Graphics g, float magnification, ZPLMemory memory) {
	    Graphics2D g2d = (Graphics2D) g;

	    // --- 1) Build symbol with ECL & content ---
	    QrCode bean = new QrCode();
	    
	    switch (memory.bps.recallAsStringWithDefault("^BQ", ZPLPropertyStore.Param_Error_Correction, "Q")) {
	        case "H": bean.setPreferredEccLevel(EccLevel.H); break;
	        case "M": bean.setPreferredEccLevel(EccLevel.M); break;
	        case "L": bean.setPreferredEccLevel(EccLevel.L); break;
	        default:  bean.setPreferredEccLevel(EccLevel.Q); break;
	    }
	    String data = memory.bps.recallAsStringWithDefault("^FD", ZPLPropertyStore.Param_Text, "");
	    
	    bean.setContent(data);
	    
		int barHeight = 0;
		
		barHeight = bean.getBarHeight();
		barHeight= (int) (barHeight*magnification);

	    // --- 2) Derive pixels-per-module from ^BQ h at your printer DPI ---
//	    int zplMag = memory.bps.recallAsIntegerWithDefault("^BQ", ZPLPropertyStore.Param_Magnification, 3); // 1..10
//	    int printerDPI = memory.printerDPI;
	   
	    int zplMagnification = memory.bps.recallAsIntegerWithDefault("^BQ", ZPLPropertyStore.Param_Magnification, 3);;   // from ^BQ
	    float uiMagnification = magnification; // your preview zoom
	    int pxPerModule = Math.max(1, Math.round(zplMagnification * uiMagnification)); // 5
	    int modules = bean.getWidth();
//	    int symbolDots = modules * zplMagnification;      // for mm/in calc
//	    int symbolPixels = modules * pxPerModule;         // for image size

	    	    
	    int imgW = modules * pxPerModule;
	    int imgH = modules * pxPerModule;

	    BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D ig = img.createGraphics();
	    ig.setBackground(java.awt.Color.WHITE);
	    ig.clearRect(0, 0, imgW, imgH);
	    
	    Java2DRenderer renderer = new Java2DRenderer(ig,pxPerModule,uk.org.okapibarcode.graphics.Color.BLACK,uk.org.okapibarcode.graphics.Color.WHITE);
	    renderer.render(bean);
	    ig.dispose();
	    
		int modeAdjust = 0;
		if (memory.anchor_mode.equals("^FT"))
		{
			modeAdjust = barHeight;
		}

		// 3. Draw image wherever you want
		int x = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "X", 0);
		int y = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, "Y", 0);

		g2d.drawImage(img, x, y-modeAdjust, null);

	    return true;
	}

}
