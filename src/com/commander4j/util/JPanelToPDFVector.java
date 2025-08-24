package com.commander4j.util;

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

import com.commander4j.zpl.ZPLPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class JPanelToPDFVector
{
	
	public static void savePanelAsVectorPDF(JPanel parent, File out) throws Exception {

	    List<ZPLPanel> pages = new ArrayList<>();
	    
	    for (Component c : parent.getComponents()) {
	        if (c instanceof ZPLPanel zp) pages.add(zp);
	    }
	    
	    if (pages.isEmpty()) throw new IllegalArgumentException("No ZPLPanel children found.");


	    RepaintManager rm = RepaintManager.currentManager(parent);
	    boolean oldDb = rm.isDoubleBufferingEnabled();
	    rm.setDoubleBufferingEnabled(false);

	    try (PDDocument doc = new PDDocument()) {

	        for (ZPLPanel child : pages) {
	            // Ensure layout & size
	            child.invalidate();
	            child.doLayout();
	            child.validate();
	            int w = Math.max(1, child.getWidth());
	            int h = Math.max(1, child.getHeight());
	            if (w == 1 || h == 1) {
	                java.awt.Dimension pref = child.getPreferredSize();
	                w = Math.max(w, Math.max(1, pref.width));
	                h = Math.max(h, Math.max(1, pref.height));
	            }

	            PDPage page =
	                new PDPage(new PDRectangle(w, h));
	            doc.addPage(page);


	            PdfBoxGraphics2D g2 =
	                new PdfBoxGraphics2D(doc, w, h);
	            try {
	                g2.setBackground(Color.WHITE);
	                g2.clearRect(0, 0, w, h);

	                child.print(g2);
	            } finally {
	                g2.dispose();
	            }
	            PDFormXObject form = g2.getXFormObject();

	            try (PDPageContentStream cs =
	                     new PDPageContentStream(doc, page)) {
	                cs.drawForm(form);
	            }
	        }

	        doc.save(out);
	    } finally {
	        rm.setDoubleBufferingEnabled(oldDb);
	    }
	}

}
