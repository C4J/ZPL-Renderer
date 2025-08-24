package com.commander4j.zpl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedList;

import javax.swing.JPanel;

import com.commander4j.barcode.ZPLBarcode_Aztec;
import com.commander4j.barcode.ZPLBarcode_Code11;
import com.commander4j.barcode.ZPLBarcode_Code128;
import com.commander4j.barcode.ZPLBarcode_Code39;
import com.commander4j.barcode.ZPLBarcode_EAN128;
import com.commander4j.barcode.ZPLBarcode_EAN13;
import com.commander4j.barcode.ZPLBarcode_EAN8;
import com.commander4j.barcode.ZPLBarcode_Int2of5;
import com.commander4j.barcode.ZPLBarcode_PDF417;
import com.commander4j.barcode.ZPLBarcode_QRCode;
import com.commander4j.barcode.ZPLBarcode_Types;
import com.commander4j.barcode.ZPL_GS1_Interpreter;
import com.commander4j.cmd.ZPLCmd;
import com.commander4j.cmd.ZPL_GF_CmdRenderer;
import com.commander4j.font.ZPLFontCache;
import com.commander4j.font.ZPLOffsets;
import com.commander4j.memory.ZPLMemory;
import com.commander4j.util.ZPLUtility;

public class ZPLPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private ZPLUtility util = new ZPLUtility();
	private float magnification = (float) 0.5;
	private String uuid = "";
	private LinkedList<ZPLCmd> zpllist;

	private ZPLBarcode_Code128 bc_okapi_code128 = new ZPLBarcode_Code128();
	private ZPLBarcode_Code11 bc_okapi_code11 = new ZPLBarcode_Code11();
	private ZPLBarcode_Code39 bc_okapi_code39 = new ZPLBarcode_Code39();
	private ZPLBarcode_Int2of5 bc_okapi_Int2of5 = new ZPLBarcode_Int2of5();
	private ZPLBarcode_EAN8 bc_okapi_EAN8 = new ZPLBarcode_EAN8();
	private ZPLBarcode_EAN128 bc_okapi_EAN128 = new ZPLBarcode_EAN128();
	private ZPLBarcode_EAN13 bc_okapi_EAN13 = new ZPLBarcode_EAN13();
	private ZPLBarcode_PDF417 bc_okapi_pdf417 = new ZPLBarcode_PDF417();
	private ZPLBarcode_Aztec bc_okapi_aztec = new ZPLBarcode_Aztec();
	private ZPLBarcode_QRCode bc_okapi_qrcode = new ZPLBarcode_QRCode();

	private ZPL_GS1_Interpreter interpret = new ZPL_GS1_Interpreter();

	private ZPLHex zplHex = new ZPLHex();

	private ZPLCmd cmd;
	private ZPLMemory memory;
	public int pageNo = 0;

	public ZPLPanel(LinkedList<ZPLCmd> zpllist, String uuid, String filename, float viewmag, int page)
	{

		this.uuid = uuid;

		this.zpllist = zpllist;

		this.magnification = viewmag;

		this.pageNo = page;

		setBackground(Color.WHITE);

	}

	public void updateMagnification(float mag)
	{
		if (memory != null)
		{
			memory.zplFont.zplFontCache.clear();
		}

		float previous = this.magnification;
		this.magnification = mag;

		revalidate();
		repaint();
		firePropertyChange("magnification", previous, magnification);

	}

	@Override
	public Dimension getPreferredSize()
	{
		// Report label size as A4 at 300 DPI: 1240x1754 pixels
		return new Dimension((int) ZPLCommon.config.get(uuid).printerMariginRight, (int) ZPLCommon.config.get(uuid).printerMariginBottom);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		String ignored = "    ";

		boolean withinBarcode = false;

		memory = ZPLCommon.config.get(uuid);

		for (int x = 0; x < zpllist.size(); x++)
		{
			ignored = "    ";

			cmd = zpllist.get(x);

			switch (cmd.getCommand())
			{
			case "^A":

				break;

			case "^A0", "^A1", "^A2", "^A3", "^A4", "^A5", "^A6", "^A7", "^A18", "^A9", "^AA", "^AB", "^AC", "^AD", "^AE", "^AF", "^AG", "^AH", "^AI", "^AJ", "^AK", "^AL", "^AM", "^AN", "^AO", "^AP", "^AQ", "^AR", "^AS", "^AT", "^AU", "^AV", "^AW", "^AX", "^AY", "^AZ":

				memory.zplFont.setFont(cmd, memory, magnification);

				break;

			case "^B0", "^BO":

				withinBarcode = true;

				bc_okapi_aztec.getParameters(cmd, memory);

				break;

			case "^B1":

				// INT 2 OF 5

				withinBarcode = true;

				bc_okapi_code11.getParameters(cmd, memory);
				break;

			case "^B2":

				// INT 2 OF 5

				withinBarcode = true;

				bc_okapi_Int2of5.getParameters(cmd, memory);

				break;

			case "^B3":

				// CODE 39

				withinBarcode = true;

				bc_okapi_code39.getParameters(cmd, memory);

				break;

			case "^B7":

				// PDF417

				withinBarcode = true;

				bc_okapi_pdf417.getParameters(cmd, memory);

				break;

			case "^B8":

				// EAN8

				withinBarcode = true;

				bc_okapi_EAN8.getParameters(cmd, memory);

				break;

			case "^BC":

				// CODE 128 AND EAN 128

				withinBarcode = true;

				bc_okapi_code128.getParameters(cmd, memory);

				break;

			case "^BE":

				// EAN 13

				withinBarcode = true;

				bc_okapi_EAN13.getParameters(cmd, memory);

				break;

			case "^BQ":

				// QRCode

				withinBarcode = true;

				bc_okapi_qrcode.getParameters(cmd, memory);

				break;

			case "^BY":

				// COMMON PARAMETERS

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Module_Width, util.getDoubleWithDefault(cmd, 0, 2.0));

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Module_Width_Narrow_Ratio, util.getDoubleWithDefault(cmd, 1, 3.0));

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Barcode_Height, util.getDoubleWithDefault(cmd, 2, 30.0));

				break;

			case "^CD":

				break;

			case "^CF":

				memory.zplFont.setFont(cmd, memory, magnification);

				break;

			case "^CI":

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Character_Set, util.getStringWithDefault(cmd, 0, "27"));

				memory.zplFont.setCharsetByCICommand(cmd, memory, memory.bps.recallAsStringWithDefault(cmd.getCommand(), ZPLPropertyStore.Param_Character_Set, "27"));

				break;

			case "^FD":

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Text, decodeFDData(util.getStringWithDefault(cmd, 0, "").getBytes()));

				if (memory.bps.recallAsBooleanWithDefault("^FH", ZPLPropertyStore.Param_Hex_Mode, false) == true)
				{
					memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Text,
							zplHex.decodeHexSubstitutions(memory.bps.recallAsStringWithDefault(cmd.getCommand(), ZPLPropertyStore.Param_Text, ""), memory.bps.recallAsStringWithDefault("^FH", ZPLPropertyStore.Param_Hex_Prefix_Character, "_")));

				}

				if (withinBarcode == false)
				{

					ZPLFontCache fontCache = memory.zplFont.recallFont(g2d, cmd, memory, magnification, false);

					ZPLOffsets scaledFontOffsets = memory.zplFont.computeTopLeftOffsets(g2d, fontCache.font, "");

					switch (memory.anchor_mode)
					{
					case "^FO":
	
						memory.zplFont.drawAtTopLeft(g2d, memory.bps.recallAsStringWithDefault("^FD", ZPLPropertyStore.Param_Text, ""), fontCache.font, scaledFontOffsets,
								memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, ZPLPropertyStore.Param_X, 1), memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, ZPLPropertyStore.Param_Y, 1),
								memory.bps.recallAsBooleanWithDefault(cmd.getCommand(), ZPLPropertyStore.Param_Reverse_Colours, false));
						break;

					case "^FT":
						memory.zplFont.drawAtBottomLeft(g2d, memory.bps.recallAsStringWithDefault("^FD", ZPLPropertyStore.Param_Text, ""), fontCache.font, scaledFontOffsets,
								memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, ZPLPropertyStore.Param_X, 1), memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, ZPLPropertyStore.Param_Y, 1),
								memory.bps.recallAsBooleanWithDefault(cmd.getCommand(), ZPLPropertyStore.Param_Reverse_Colours, false));
						break;
					}

				}

				if (withinBarcode == true)
				{
					String FD_Text = util.getStringWithDefault(cmd, 0, "");

					// Remove all Code Change Commands

					FD_Text = FD_Text.replace(">;", "");
					FD_Text = FD_Text.replace(">:", "");
					FD_Text = FD_Text.replace(">9", "");

					// Does Barcode begin with FNC1 ?
					if (FD_Text.startsWith(">8"))
					{
						// Remove it and set barcode type to EAN128
						FD_Text = FD_Text.substring(2);
						memory.bps.store("^BY", ZPLPropertyStore.Param_Barcode_Type, ZPLBarcode_Types.BC_EAN128);
					}

					// Replace ZPL FNC1 with actual control character
					FD_Text = FD_Text.replace(">8", "\u00f1");

					memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Text, FD_Text);

					memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Text_Modified, (interpret.OKIBarCode128FromZPL(memory, memory.bps.recallAsStringWithDefault("^FD", ZPLPropertyStore.Param_Text, ""))));

					switch (memory.bps.recallAsIntegerWithDefault("^BY", ZPLPropertyStore.Param_Barcode_Type, ZPLBarcode_Types.BC_Code128))
					{
					case ZPLBarcode_Types.BC_Code128:

						bc_okapi_code128.create(g, magnification, memory);
						break;
					case ZPLBarcode_Types.BC_EAN128:
						bc_okapi_EAN128.create(g, magnification, memory);
						break;
					case ZPLBarcode_Types.B1_Code11:
						bc_okapi_code11.create(g, magnification, memory);
						break;
					case ZPLBarcode_Types.B3_Code39:
						bc_okapi_code39.create(g, magnification, memory);
						break;
					case ZPLBarcode_Types.B2_2of5:
						bc_okapi_Int2of5.create(g, magnification, memory);
						break;
					case ZPLBarcode_Types.B7_PDF417:
						bc_okapi_pdf417.create(g, magnification, memory);
						break;
					case ZPLBarcode_Types.BE_EAN13:
						bc_okapi_EAN13.create(g, magnification, memory);
						break;
					case ZPLBarcode_Types.B8_EAN8:
						bc_okapi_EAN8.create(g, magnification, memory);
						break;
					case ZPLBarcode_Types.B0_Aztec:
						bc_okapi_aztec.create(g, magnification, memory);
						break;
					case ZPLBarcode_Types.BQ_QRCode:
						bc_okapi_qrcode.create(g, magnification, memory);
						break;
					}
				};
				memory.bps.delete("^A");

			case "^FH":

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Hex_Prefix_Character, util.getStringWithDefault(cmd, 0, "_"));

				if (memory.bps.recallAsBooleanWithDefault(cmd.getCommand(), ZPLPropertyStore.Param_Hex_Mode, false) == false)
				{
					memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Hex_Mode, true);
				}

				break;

			case "^FO":

				memory.anchor_mode = cmd.getCommand();

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_X, (util.getIntWithDefault(cmd, 0, 0) + memory.bps.recallAsIntegerWithDefault("^LH", ZPLPropertyStore.Param_Label_Left, 0)) * magnification);

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Y,
						(util.getIntWithDefault(cmd, 1, 0) + memory.bps.recallAsIntegerWithDefault("^LH", ZPLPropertyStore.Param_Label_Top, 0) + memory.bps.recallAsIntegerWithDefault("^LT", ZPLPropertyStore.Param_Label_Top, 0)) * magnification);
				break;

			case "^FR":
				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Reverse_Colours, true);

				break;

			case "^FS":
				withinBarcode = false;

				memory.bps.store("^FH", ZPLPropertyStore.Param_Hex_Mode, false);
				memory.bps.store("^FR", ZPLPropertyStore.Param_Reverse_Colours, false);

				memory.bps.delete("^FO");
				memory.bps.delete("^FT");

				memory.anchor_mode = "";

				break;

			case "^FT":

				memory.anchor_mode = cmd.getCommand();

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_X, (util.getIntWithDefault(cmd, 0, 0) + memory.bps.recallAsIntegerWithDefault("^LH", ZPLPropertyStore.Param_Label_Left, 0)) * magnification);

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Y,
						(util.getIntWithDefault(cmd, 1, 0) + memory.bps.recallAsIntegerWithDefault("^LH", ZPLPropertyStore.Param_Label_Top, 0) + memory.bps.recallAsIntegerWithDefault("^LT", ZPLPropertyStore.Param_Label_Top, 0)) * magnification);

				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Justification, (util.getIntWithDefault(cmd, 2, memory.bps.recallAsIntegerWithDefault("^FW", ZPLPropertyStore.Param_Justification, 2))));
				break;

			case "^FW":
				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Orientation, (util.getStringWithDefault(cmd, 0, "N")));
				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Justification, (util.getIntWithDefault(cmd, 1, 2)));

				break;

			case "^FX":
				// Comment;
				break;

			case "^GB":

				float width = util.getIntWithDefault(cmd, 0, 1) * magnification;

				float height = util.getIntWithDefault(cmd, 1, 1) * magnification;

				float thickness = util.getIntWithDefault(cmd, 2, 1) * magnification;

				thickness = Math.max(1f, thickness);
				width = Math.max(width, thickness);
				height = Math.max(height, thickness);

				float x2 = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, ZPLPropertyStore.Param_X, 1);
				float y2 = memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, ZPLPropertyStore.Param_Y, 1);

				Graphics2D gbG2D = (Graphics2D) g.create();

				try
				{
					gbG2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

					if (memory.bps.recallAsBooleanWithDefault("^FR", ZPLPropertyStore.Param_Reverse_Colours, false))
					{
						gbG2D.setXORMode(Color.BLACK);
						gbG2D.setColor(Color.WHITE);
					}
					else
					{
						// gbG2D.setXORMode(Color.WHITE);
						gbG2D.setColor(Color.BLACK);
					}

					if (thickness >= Math.min(width, height))
					{
						gbG2D.fillRect((memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, ZPLPropertyStore.Param_X, 1)), (memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, ZPLPropertyStore.Param_Y, 1)), (int) width, (int) height);
					}
					else
					{
						float inset = thickness / 2f;

						gbG2D.setStroke(new BasicStroke(thickness));

						gbG2D.drawRect((int) (x2 + inset), (int) (y2 + inset), (int) (width - thickness), (int) (height - thickness));

					}
				}
				finally
				{
					gbG2D.setPaintMode(); // reset from XOR
					gbG2D.dispose();

				}

				memory.bps.store("^FR", ZPLPropertyStore.Param_Reverse_Colours, false);

				break;

			case "^GF":
				ZPL_GF_CmdRenderer.draw(g2d, cmd, magnification, memory.bps.recallAsIntegerWithDefault("^FO", ZPLPropertyStore.Param_X, 1), memory.bps.recallAsIntegerWithDefault(memory.anchor_mode, ZPLPropertyStore.Param_Y, 1));
				break;

			case "^LH":
				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Label_Left, util.getIntWithDefault(cmd, 0, 0) * magnification);
				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Label_Top, util.getIntWithDefault(cmd, 1, 0) * magnification);
				break;

			case "^LT":
				memory.bps.store(cmd.getCommand(), ZPLPropertyStore.Param_Label_Top, util.getIntWithDefault(cmd, 0, 0) * magnification);
				break;

			case "^XA":
				// Start of Label
				break;

			case "^XZ":
				// End of Label
				memory.bps.delete("^CF");
				break;

			default:
				if (memory.zplindex.zplDescription.containsKey(cmd.getCommand()))
				{
					ignored = "IGN ";
				}
				else
				{
					ignored = "ERR ";
				}
				break;
			}

			System.out.print(ignored);

			if (memory.bps.recallAsBooleanWithDefault("^FH", ZPLPropertyStore.Param_Hex_Mode, false))
			{
				System.out.print("HEX ");
			}
			else
			{
				System.out.print("    ");
			}

			if (memory.bps.recallAsBooleanWithDefault("^FR", ZPLPropertyStore.Param_Reverse_Colours, false))
			{
				System.out.print("REV ");
			}
			else
			{
				System.out.print("    ");
			}

			System.out.println(cmd);

		}
	}

	public String decodeFDData(byte[] rawData)
	{
		return new String(rawData, memory.currentCharset);
	}
}
