package com.commander4j.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.commander4j.cmd.ZPLCmd;
import com.commander4j.memory.ZPLMemory;
import com.commander4j.util.ZPLUtility;
import com.commander4j.zpl.ZPLPropertyStore;

public class ZPLFont
{
	private static final String DEFAULT_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789" + " !?.,;:'\"-_/\\()[]{}@#*$%&+^=<>";
	private ZPLUtility util = new ZPLUtility();
	public HashMap<String, ZPLFontProperties> zebraFontLookup = new HashMap<String, ZPLFontProperties>();
	public HashMap<String, ZPLFontCache> zplFontCache = new HashMap<String, ZPLFontCache>();

	public ZPLFont()
	{

		readFontsFromXml();
	}

	public ZPLFontCache getSizedFont(Graphics g, String fontName, double requiredHeight, double requiredWidth, float magnification, boolean whitespace)
	{
		float targetwidth = (float) (requiredWidth*magnification);
		
		float targetheight = (float) (requiredHeight*magnification);

		ZPLFontCache result;
		Graphics2D g2d = (Graphics2D) g;

		String key = fontName + String.valueOf(requiredHeight) + String.valueOf(requiredWidth) + whitespace;

		if (zplFontCache.containsKey(key))
		{
			result = zplFontCache.get(key);
		}
		else
		{
			Font scaled;

			scaled = createScaledFont(g2d, fontName, targetheight, targetwidth, whitespace);

			result = new ZPLFontCache(scaled);

			zplFontCache.put(key, result);
		}

		return result;
	}

	public void drawAtTopLeft(Graphics2D g2d, String text, Font font, ZPLOffsets offsets, int x, int y, boolean reverse)
	{
		int baselineX = x + offsets.offsetLeftPx;
		int baselineY = y + offsets.offsetTopPx;

		Font oldFont = g2d.getFont();
		Object aaOld = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		Stroke oldStroke = g2d.getStroke();

		try
		{
			g2d.setFont(font);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			if (reverse)
			{
				g2d.setXORMode(Color.BLACK);
				g2d.setColor(Color.WHITE);
			}
			else
			{
				g2d.setXORMode(Color.WHITE);
				g2d.setColor(Color.BLACK);
			}

			// Draw the text
			g2d.drawString(text, baselineX, baselineY);

		}
		finally
		{
			g2d.setFont(oldFont);
			if (aaOld != null)
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, aaOld);
			g2d.setStroke(oldStroke);
		}
	}
	
	public void drawAtBottomLeft(Graphics2D g2d, String text, Font font, ZPLOffsets offsets, int x, int y, boolean reverse)
	{
		int baselineX = x + offsets.offsetLeftPx;
		int baselineY = y+ offsets.offsetTopPx;

		Font oldFont = g2d.getFont();
		Object aaOld = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		Stroke oldStroke = g2d.getStroke();
		
		FontMetrics fm = g2d.getFontMetrics(font);
		int h = fm.getStringBounds(text, g2d).getBounds().height;

		try
		{
			g2d.setFont(font);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			if (reverse)
			{
				g2d.setXORMode(Color.BLACK);
				g2d.setColor(Color.WHITE);
			}
			else
			{
				g2d.setXORMode(Color.WHITE);
				g2d.setColor(Color.BLACK);
			}

			// Draw the text
			g2d.drawString(text, baselineX, baselineY-h);

		}
		finally
		{
			g2d.setFont(oldFont);
			if (aaOld != null)
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, aaOld);
			g2d.setStroke(oldStroke);
		}
	}

	public String getFontNameFromFontID(String id)
	{
		if (zebraFontLookup.containsKey(id) == false)
		{
			System.out.println("Warning unknown font id " + id + " using default 0");
			return zebraFontLookup.get("0").name;
		}
		return zebraFontLookup.get(id).name;
	}

	public void setFont(ZPLCmd cmd, ZPLMemory memory, float magnification)
	{

		if (cmd.getCommand().startsWith("^A"))
		{
			String id = cmd.getCommand().substring(2, 3);

			memory.bps.store("^A", ZPLPropertyStore.Param_Font_ID, id);
			memory.bps.store("^A", ZPLPropertyStore.Param_Font_Name, memory.zplFont.getFontNameFromFontID(id));
			memory.bps.store("^A", ZPLPropertyStore.Param_Font_Rotation, util.getStringWithDefault(cmd, 0, "N"));
			memory.bps.store("^A", ZPLPropertyStore.Param_Font_Height, util.getIntWithDefault(cmd, 1, 9));
			memory.bps.store("^A", ZPLPropertyStore.Param_Font_Width, util.getIntWithDefault(cmd, 2, memory.bps.recallAsIntegerWithDefault("^A", ZPLPropertyStore.Param_Font_Height, 9)));

			memory.bps.recallAsIntegerWithDefault("^A", ZPLPropertyStore.Param_Font_Height, 5);

		}

		if (cmd.getCommand().startsWith("^CF"))
		{
			// Default Font ID

			String id = "0";
			ZPLFontProperties prop;

			// Get Font ID

			if (cmd.getArgumentCount() >= 1)
			{
				id = cmd.getArgument(0);

				if (memory.zplFont.zebraFontLookup.containsKey(id) == false)
				{
					id = "0";
				}

				// Store Active Font ID
				memory.bps.store("^CF", ZPLPropertyStore.Param_Font_ID, id);
				// Store Active Font Name
				memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Name, memory.zplFont.getFontNameFromFontID(id));


			}
			
			if (memory.zplFont.zebraFontLookup.containsKey(id))
			{

				prop = memory.zplFont.zebraFontLookup.get(id);
				
				memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Filename, prop.filename);
				memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Zebra_Height, prop.height);
				memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Zebra_Width, prop.width);
				memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Zebra_Font_Render, prop.renderer);
				memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Zebra_Font_Spacing, prop.spacing);
				
				
				if (cmd.getArgumentCount() >= 2)
				{
					memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Height, util.getIntWithDefault(cmd, 1, memory.bps.recallAsIntegerWithDefault("^CF", ZPLPropertyStore.Param_Font_Zebra_Height, util.getIntWithDefault(cmd, 1, 9))));
				}

				if (cmd.getArgumentCount() >= 3)
				{
					//Width Provided so use that.
					memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Width, util.getIntWithDefault(cmd, 2, 9));
				}
				else
				{
					//Width NOT provided so work it out depending on font render type
					if (prop.renderer.equals("SCALEABLE"))
					{
						//PROPORTIONAL
						int fontHeight = memory.bps.recallAsIntegerWithDefault("^CF", ZPLPropertyStore.Param_Font_Height, -1);

						int proportialWidth = (int) Math.round(fontHeight / Double.valueOf(prop.height) * Double.valueOf(prop.width));

						memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Width, proportialWidth);
					}
					else
					{
						//BITMAP
						int fontHeight = memory.bps.recallAsIntegerWithDefault("^CF", ZPLPropertyStore.Param_Font_Height, -1);

						double step1 = ((double) fontHeight) / Double.valueOf(prop.height);
						double step2 =  Double.valueOf(prop.width) * step1;
						int bitmapWidth = (int) Math.round(step2);

						memory.bps.store("^CF", ZPLPropertyStore.Param_Font_Width, bitmapWidth);
					}
				}
				
			}

		}

	}

	public void readFontsFromXml()
	{
		try
		{
			zplFontCache.clear();
			File xmlFile = new File("./xml/config/fonts.xml");
			System.out.println("Loading fonts...");
			// Create DocumentBuilder
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			List<String> available_font_names = Arrays.asList(ge.getAvailableFontFamilyNames());

			zebraFontLookup.clear();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			// Parse the XML
			Document document = builder.parse(xmlFile);
			document.getDocumentElement().normalize();

			// Get all <font> elements
			NodeList fontNodes = document.getElementsByTagName("font");

			for (int i = 0; i < fontNodes.getLength(); i++)
			{
				Node node = fontNodes.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element fontElement = (Element) node;

					ZPLFontProperties newProp = new ZPLFontProperties();
					newProp.id = fontElement.getAttribute("id");
					newProp.name = fontElement.getAttribute("name");
					newProp.filename = fontElement.getAttribute("filename");
					newProp.height = fontElement.getAttribute("height");
					newProp.width = fontElement.getAttribute("width");
					newProp.renderer = fontElement.getAttribute("renderer");
					newProp.spacing = fontElement.getAttribute("spacing");

					boolean valid = true;
					if (newProp.filename.equals("") == false)
					{

						File fontfile = new File("./fonts/" + newProp.filename);

						if (fontfile.exists())
						{
							Font newFont = Font.createFont(Font.TRUETYPE_FONT, fontfile);

							if (!available_font_names.contains(newFont.getFontName()))
							{
								ge.registerFont(newFont);
							}
						}
						else
						{
							valid = false;
						}

						fontfile = null;
					}

					if (valid)
						zebraFontLookup.put(newProp.id, newProp);
					System.out.println(newProp.id + " = " + newProp.name);
				}

			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean saveFontsToXml(HashMap<String, ZPLFontProperties> list)
	{

		boolean result = false;

		try
		{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// Root element
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("fonts");
			doc.appendChild(rootElement);

			List<String> keys = new ArrayList<>(list.keySet());
			Collections.sort(keys);

			for (String key : keys)
			{

				ZPLFontProperties fp = list.get(key);

				Element font = doc.createElement("font");
				font.setAttribute("id", fp.id);
				font.setAttribute("name", fp.name);
				font.setAttribute("filename", fp.filename);
				font.setAttribute("height", fp.height);
				font.setAttribute("width", fp.width);
				font.setAttribute("renderer", fp.renderer);
				font.setAttribute("spacing", fp.spacing);
				rootElement.appendChild(font);
			}

			// Write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Pretty print
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DOMSource source = new DOMSource(doc);
			StreamResult streamResult = new StreamResult(new File("./xml/config/fonts.xml"));

			transformer.transform(source, streamResult);

			result = true;
		}
		catch (Exception ex)
		{

		}

		return result;

	}

	public void setCharsetByCICommand(ZPLCmd cmd, ZPLMemory memory, String ciCode)
	{

		switch (ciCode)
		{
		case "13":
			memory.currentCharset = StandardCharsets.ISO_8859_1;
			break;
		case "27":
			memory.currentCharset = StandardCharsets.UTF_8;
			break;
		case "28":
			memory.currentCharset = Charset.forName("windows-1252");
			break;
		default:
			System.err.println("Warning: Unsupported ^CI code: " + ciCode + " — defaulting to ISO-8859-1");
			memory.currentCharset = StandardCharsets.ISO_8859_1;
			break;
		}
	}

	public ZPLOffsets computeTopLeftOffsets(Graphics2D g2d, Font font, String charset)
	{
		if (charset == null || charset.isEmpty())
			charset = DEFAULT_CHARSET;

		FontRenderContext frc = g2d.getFontRenderContext();

		double maxLeftNeeded = 0.0; // = max(-minX)
		double maxTopNeeded = 0.0; // = max(-minY)
		double maxRightBear = 0.0; // = max(maxX)
		double maxBottomNeed = 0.0; // = max(maxY)

		for (int i = 0; i < charset.length(); i++)
		{
			char ch = charset.charAt(i);
			String s = String.valueOf(ch);

			GlyphVector gv = font.createGlyphVector(frc, s);
			Rectangle2D vb = gv.getVisualBounds(); // tight ink bounds relative
													// to baseline origin

			double minX = vb.getMinX();
			double minY = vb.getMinY();
			double maxX = vb.getMaxX();
			double maxY = vb.getMaxY();

			double leftNeeded = -minX; // how far right we must shift origin to
										// avoid left spill
			double topNeeded = -minY; // how far down we must shift origin to
										// avoid top spill
			double rightBearing = maxX; // positive extent to the right
										// (optional)
			double bottomNeed = maxY; // positive extent below baseline

			if (leftNeeded > maxLeftNeeded)
				maxLeftNeeded = leftNeeded;
			if (topNeeded > maxTopNeeded)
				maxTopNeeded = topNeeded;
			if (rightBearing > maxRightBear)
				maxRightBear = rightBearing;
			if (bottomNeed > maxBottomNeed)
				maxBottomNeed = bottomNeed;
		}

		// Round up to integer pixels for drawing
		int offL = (int) Math.ceil(maxLeftNeeded);
		int offT = (int) Math.ceil(maxTopNeeded);
		int padR = (int) Math.ceil(maxRightBear);
		int padB = (int) Math.ceil(maxBottomNeed);

		return new ZPLOffsets(offL, offT, padR, padB);
	}

	public Font createScaledFont(Graphics2D g2d, String fontName, float heightPx, float widthPx, boolean whitespace)
	{

		String key = fontName + " " + String.valueOf(heightPx) + " " + String.valueOf(widthPx) + " " + whitespace;
		ZPLFontCache result;

		if (zplFontCache.containsKey(key))
		{
			result = zplFontCache.get(key);
		}
		else
		{

			if (whitespace)
			{

				String referenceChar = "M";
				int w = 0;
				int h = 0;
				FontMetrics fm;
				double scaleY = 0;
				double scaleX = 0;

				Font testFontMatch = new Font(fontName, Font.PLAIN, 72);

				for (int size = 72; size >= 6; size--)
				{

					testFontMatch = new Font(fontName, Font.PLAIN, size);

					fm = g2d.getFontMetrics(testFontMatch);

					w = fm.getStringBounds(referenceChar, g2d).getBounds().width;

					h = fm.getStringBounds(referenceChar, g2d).getBounds().height;

					if ((w <= widthPx) && (h <= heightPx))
						break;
				}

				scaleY = Double.valueOf((double) (heightPx) / h);
				scaleX = Double.valueOf((double) (widthPx) / w);

				AffineTransform transform = AffineTransform.getScaleInstance(scaleX, scaleY);

				Font temp = testFontMatch.deriveFont(transform);

				result = new ZPLFontCache(temp);

				zplFontCache.put(key, result);

			}
			else
			{

				Font baseForMeasure = new Font(fontName, Font.PLAIN, Math.round(heightPx));
				FontRenderContext frc = g2d.getFontRenderContext();

				// Measure tight ink height using tall characters
				GlyphVector gvH = baseForMeasure.createGlyphVector(frc, "Hg");
				Rectangle2D visH = gvH.getVisualBounds();
				double baseHeight = Math.max(1e-6, visH.getHeight());

				// Vertical scale to requested pixel height
				double scaleY = (heightPx > 0) ? (heightPx / baseHeight) : 1.0;

				// Horizontal scale: use a wide reference to capture width
				// realistically
				double scaleX = 1.0;
				if (widthPx > 0)
				{
				//	GlyphVector gvW = baseForMeasure.createGlyphVector(frc, "MW");
					Rectangle2D visW = gvH.getVisualBounds();
					double baseWidth = Math.max(1e-6,visW.getWidth());
					scaleX = widthPx / baseWidth;
				}

				AffineTransform tx = AffineTransform.getScaleInstance(scaleX, scaleY);

				Font temp = baseForMeasure.deriveFont(tx);

				result = new ZPLFontCache(temp);

				zplFontCache.put(key, result);

			}

		}

		return result.font;
	}

	/** Tiny helper to get a Graphics2D if you need to precompute off-screen. */
	public Graphics2D scratchG2D()
	{
		BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return g2d;
	}

	public ZPLFontCache recallFont(Graphics2D g2d, ZPLCmd cmd, ZPLMemory memory, float magnification, boolean whitespace)
	{
		String FontName = "";
		float FontHeight = 0;
		float FontWidth = 0;
		
		FontName = memory.bps.recallAsStringWithDefault("^A", ZPLPropertyStore.Param_Font_Name,"");
		
		if (FontName.equals(""))
		{
			// ^A not set so try ^CF else use default;
			FontName = memory.bps.recallAsStringWithDefault("^CF", ZPLPropertyStore.Param_Font_Name, memory.zplFont.getFontNameFromFontID("0"));
			FontHeight = memory.bps.recallAsFloatWithDefault("^CF", ZPLPropertyStore.Param_Font_Height, 30);
			FontWidth = memory.bps.recallAsFloatWithDefault("^CF", ZPLPropertyStore.Param_Font_Width, 30);
		}
		else
		{
			FontName = memory.bps.recallAsStringWithDefault("^A", ZPLPropertyStore.Param_Font_Name, memory.zplFont.getFontNameFromFontID("0"));
			FontHeight = memory.bps.recallAsFloatWithDefault("^A", ZPLPropertyStore.Param_Font_Height, 30);
			FontWidth = memory.bps.recallAsFloatWithDefault("^A", ZPLPropertyStore.Param_Font_Width, 30);
		}

		ZPLFontCache fontCache = memory.zplFont.getSizedFont(g2d, FontName, FontHeight, FontWidth, magnification, whitespace);

		return fontCache;
	}

	public void expireFont(ZPLCmd cmd, ZPLMemory memory)
	{
		memory.bps.delete(cmd.getCommand());

	}
}
