package com.commander4j.util;

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;

import com.commander4j.zpl.ZPLPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

public class VectorPdfExactSize
{

	/**
	 * Build a PDF page whose size == the printer's imageable area selected in
	 * the dialog.
	 */
	public static PDDocument createPdfForImageable(JPanel panel, PageFormat pf) throws Exception
	{
		int pw = Math.max(1, panel.getWidth());
		int ph = Math.max(1, panel.getHeight());

		// Imageable (what the printer will actually print on)
		float iw = (float) pf.getImageableWidth();
		float ih = (float) pf.getImageableHeight();

		PDDocument doc = new PDDocument();
		PDPage page = new PDPage(new PDRectangle(iw, ih));
		doc.addPage(page);

		// Draw the panel to a vector XForm at panel-native size
		PdfBoxGraphics2D g2 = new PdfBoxGraphics2D(doc, pw, ph);
		g2.setBackground(Color.WHITE);
		g2.clearRect(0, 0, pw, ph);
		panel.printAll(g2);
		g2.dispose();
		PDFormXObject form = g2.getXFormObject();

		// Scale+center *inside* the page (which equals imageable area)
		double scale = Math.min(iw / pw, ih / ph);
		float drawW = (float) (pw * scale);
		float drawH = (float) (ph * scale);
		float tx = (iw - drawW) / 2f;
		float ty = (ih - drawH) / 2f;

		try (var cs = new PDPageContentStream(doc, page))
		{
			cs.saveGraphicsState();
			cs.transform(new org.apache.pdfbox.util.Matrix((float) scale, 0, 0, (float) scale, tx, ty));
			cs.drawForm(form);
			cs.restoreGraphicsState();
		}
		return doc;
	}

	/**
	 * Show the print dialog first, then produce a *matching* PDF and print
	 * ACTUAL_SIZE.
	 */
	public static void printPanelWithoutClipping(JPanel panel, String jobName) throws Exception
	{
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setJobName(jobName != null ? jobName : "ZPL Vector (Exact Imageable)");

		PageFormat pf = job.pageDialog(job.defaultPage());

		PDDocument parentdoc = new PDDocument();

		Component[] pages = panel.getComponents();

		if (pages.length > 0)
		{

			for (int x = 0; x < pages.length; x++)
			{
				PDDocument childdoc = createPdfForImageable(((ZPLPanel) pages[x]), pf);
				parentdoc.addPage(childdoc.getPage(0));
			}

			job.setPrintable(new PDFPrintable(parentdoc, Scaling.ACTUAL_SIZE));
			
			if (job.printDialog())	job.print();

		}
	}
}
