package com.commander4j.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.commander4j.filters.JFileFilterTTF;
import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JComboBox4j;
import com.commander4j.gui.JTextField4j;
import com.commander4j.zpl.ZPLCommon;

public class JPanelFontData extends JPanel
{

	private static final long serialVersionUID = 1L;
	public JTextField4j fld_FontID;
	public JTextField4j fld_FontName;
	public JTextField4j fld_Filename;
	public JTextField4j fld_Height;
	public JTextField4j fld_Width;
	public JComboBox4j<String> fld_Render;
	public JComboBox4j<String> fld_Spacing;
	public JButton4j btn_FontFilename;
	public JButton4j btn_FontName;
	public int rowheight = 32;
	public int idwidth = 32;
	public int namewidth = 150;
	public int renderwidth = 100;
	public int spacingwidth = 120;
	public int filenamewidth = 180;
	public int buttonwidth = rowheight;
	public int heightwidth = 32;
	public int widthwidth = 32;
	public int totalwidth = idwidth+namewidth+buttonwidth+filenamewidth+buttonwidth+heightwidth+widthwidth+renderwidth+spacingwidth;
	JFrame parent;
	String RenderTypes[] = { "SCALEABLE", "BITMAP"};
	String SpacingTypes[] = { "PROPORTIONAL", "MONOSPACED"};

	/**
	 * Create the panel.
	 */
	public JPanelFontData(JFrame parent)
	{
		this.parent = parent;
		setBackground(new Color(255, 255, 255));
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setPreferredSize(new Dimension(totalwidth,rowheight));
		
		fld_FontID = new JTextField4j();
		fld_FontID.setHorizontalAlignment(SwingConstants.CENTER);
		fld_FontID.setEnabled(false);
		fld_FontID.setEditable(false);
		fld_FontID.setPreferredSize(new Dimension(idwidth,rowheight));
		fld_FontID.setMaximumSize(new Dimension(idwidth,rowheight));
		add(fld_FontID);
		

		fld_FontName = new JTextField4j();
		add(fld_FontName);
		fld_FontName.setPreferredSize(new Dimension(namewidth,rowheight));
		
		btn_FontName = new JButton4j(ZPLCommon.icon_font);
		btn_FontName.setPreferredSize(new Dimension(buttonwidth,buttonwidth));
		btn_FontName.setSize(new Dimension(buttonwidth,buttonwidth));
		btn_FontName.setMaximumSize(new Dimension(buttonwidth,buttonwidth));
		btn_FontName.setFocusable(false);
		btn_FontName.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String currentName = fld_FontName.getText();
				Font currentFont = new Font(currentName,Font.PLAIN,12);
				if (currentName.equals("")==false)
				{
					currentFont = new Font(currentName,Font.PLAIN,12);
				}
				JDialogFonts dialog = new JDialogFonts(parent,currentFont);
				dialog.setVisible(true);
				fld_FontName.setText(JDialogFonts.selectedFont.getFontName());
				fld_FontName.requestFocus();
				fld_Filename.setText("");
			}
		});
		
		add(btn_FontName);
		
		fld_Height = new JTextField4j();
		fld_Height.setHorizontalAlignment(SwingConstants.CENTER);
		add(fld_Height);
		fld_Height.setPreferredSize(new Dimension(heightwidth,rowheight));
		
		fld_Width = new JTextField4j();
		fld_Width.setHorizontalAlignment(SwingConstants.CENTER);
		add(fld_Width);
		fld_Width.setPreferredSize(new Dimension(widthwidth,rowheight));
		
		fld_Filename = new JTextField4j();
		add(fld_Filename);
		fld_Filename.setPreferredSize(new Dimension(filenamewidth,rowheight));
		
		
		btn_FontFilename = new JButton4j(ZPLCommon.icon_open);
		btn_FontFilename.setPreferredSize(new Dimension(buttonwidth,buttonwidth));
		btn_FontFilename.setSize(new Dimension(buttonwidth,buttonwidth));
		btn_FontFilename.setMaximumSize(new Dimension(buttonwidth,buttonwidth));
		btn_FontFilename.setFocusable(false);
		
		btn_FontFilename.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				
				File ttfFile = selectLoadTreeTTF(new File("./fonts"));

				if (ttfFile != null)
				{
					fld_Filename.setText(ttfFile.getName());
					fld_Filename.requestFocus();
					fld_Filename.setCaretPosition(fld_Filename.getText().length());
			        Font font;
					try
					{
						font = Font.createFont(Font.TRUETYPE_FONT, ttfFile);
						
				        System.out.println("Family: " + font.getFamily());

				        // Java logical name (usually same as family)
				        System.out.println("Font Name: " + font.getFontName());

				        // Full name (includes style, e.g. "Liberation Mono Regular")
				        System.out.println("Full Name: " + font.getPSName());
				        
				        fld_FontName.setText(font.getFontName());

					}
					catch (Exception e1)
					{

					}
				}
			}
		});

		add(btn_FontFilename);
		
		fld_Render = new JComboBox4j<String>();
		fld_Render.setPreferredSize(new Dimension(renderwidth,rowheight));
		fld_Render.setModel(new DefaultComboBoxModel<String>(RenderTypes));
		add(fld_Render);
		
		fld_Spacing = new JComboBox4j<String>();
		fld_Spacing.setPreferredSize(new Dimension(spacingwidth,rowheight));
		fld_Spacing.setModel(new DefaultComboBoxModel<String>(SpacingTypes));
		add(fld_Spacing);

	}
	
	private File selectLoadTreeTTF(File defaultPath)
	{
		File result = null;

		JFileChooser fc = new JFileChooser(defaultPath);
		fc.setSelectedFile(defaultPath);

		JFileFilterTTF ffi = new JFileFilterTTF();
		fc.setApproveButtonText("Open");
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(this.parent);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}

}
