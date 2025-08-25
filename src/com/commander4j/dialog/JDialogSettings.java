package com.commander4j.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.commander4j.font.ZPLFontProperties;
import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JLabel4j_std;
import com.commander4j.memory.ZPLMemory;
import com.commander4j.settings.SettingUtil;
import com.commander4j.settings.Settings;
import com.commander4j.util.ZPLUtility;
import com.commander4j.zpl.ZPLCommon;

public class JDialogSettings extends JDialog
{

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	SettingUtil setutil = new SettingUtil();
	JTextField fld_InputFolder = new JTextField();
	JTextField fld_PortNo = new JTextField();
	JTextField fld_MaxPages = new JTextField();
	JTextField fld_Magnification = new JTextField();
	JCheckBox fld_SaveToHome = new JCheckBox();
	JTextField fld_alternateSaveLocation = new JTextField();
	JButton4j btn_SaveFolder = new JButton4j(ZPLCommon.icon_select_folder);
	JComboBox<String> comboBoxAppendLabelSequence = new JComboBox<String>();
	ZPLUtility utils = new ZPLUtility();

	private JFrame parent;
	JPanel main;

	/**
	 * Create the dialog.
	 */
	public JDialogSettings(JFrame parent, ZPLMemory memory,Settings settings)
	{
		super(parent);

		this.parent = parent;

		setTitle("Settings");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		ZPLUtility util = new ZPLUtility();
		setSize(new Dimension(800, 680));

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		util.setLookAndFeel("Nimbus");
		contentPanel.setLayout(null);

		main = new JPanel();

		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

		JPanelFontData one = new JPanelFontData(this.parent);
		int width = one.totalwidth + 40;
		int height = 350;

		List<String> keys = new ArrayList<>(memory.zplFont.zebraFontLookup.keySet());
		Collections.sort(keys);

		for (String key : keys) {
			
			ZPLFontProperties fp = memory.zplFont.zebraFontLookup.get(key);
			
			one = new JPanelFontData(this.parent);
			one.fld_FontID.setHorizontalAlignment(SwingConstants.CENTER);
			one.fld_FontID.setText(fp.id);
			one.fld_FontName.setText(fp.name);
			one.fld_Height.setText(fp.height);
			one.fld_Width.setText(fp.width);
			one.fld_Filename.setText(fp.filename);
			one.fld_Render.setSelectedItem(fp.renderer);
			one.fld_Spacing.setSelectedItem(fp.spacing);
			
			main.add(one);
		}

		JScrollPane scrollPane = new JScrollPane(main);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		scrollPane.setBounds(18, 40, width, height);
		
		contentPanel.add(scrollPane);

		JLabel4j_std lblNewLabel = new JLabel4j_std("Font ID");
		lblNewLabel.setText("ID");
		lblNewLabel.setFont(ZPLCommon.font_bold);
		lblNewLabel.setBounds(scrollPane.getX() + 5, scrollPane.getY() - 15, 43, 16);
		contentPanel.add(lblNewLabel);

		JLabel4j_std lblFontName = new JLabel4j_std("Font Name");
		lblFontName.setFont(ZPLCommon.font_bold);
		lblFontName.setBounds(scrollPane.getX() + one.idwidth+5, scrollPane.getY() - 15, 128, 16);
		contentPanel.add(lblFontName);
		
		JLabel4j_std lblFontDimensions = new JLabel4j_std("H  x  W");
		lblFontDimensions.setFont(ZPLCommon.font_bold);
		lblFontDimensions.setBounds(scrollPane.getX() + one.idwidth + one.namewidth+one.buttonwidth+25, scrollPane.getY() - 15, 128, 16);
		contentPanel.add(lblFontDimensions);

		JLabel4j_std lblFilename = new JLabel4j_std("Font Filename");
		lblFilename.setFont(ZPLCommon.font_bold);
		lblFilename.setBounds(scrollPane.getX() + one.idwidth + one.namewidth +one.buttonwidth+one.heightwidth +one.widthwidth + 20, scrollPane.getY() - 15, 128, 16);
		contentPanel.add(lblFilename);
		
		JLabel4j_std lblRender = new JLabel4j_std("Rendering");
		lblRender.setFont(ZPLCommon.font_bold);
		lblRender.setBounds(scrollPane.getX() + one.idwidth + one.namewidth +one.buttonwidth+one.heightwidth +one.widthwidth +one.filenamewidth+one.buttonwidth+ 20, scrollPane.getY() - 15, 128, 16);
		contentPanel.add(lblRender);
		
		JLabel4j_std lblSpacing = new JLabel4j_std("Spacing");
		lblSpacing.setFont(ZPLCommon.font_bold);
		lblSpacing.setBounds(scrollPane.getX() + one.idwidth + one.namewidth +one.buttonwidth+one.heightwidth +one.widthwidth +one.filenamewidth+one.buttonwidth+one.renderwidth+ 20, scrollPane.getY() - 15, 128, 16);
		contentPanel.add(lblSpacing);

		JLabel4j_std lbl_PanelOrder = new JLabel4j_std("New Label Order");
		lbl_PanelOrder.setHorizontalAlignment(SwingConstants.TRAILING);
		lbl_PanelOrder.setFont(ZPLCommon.font_bold);
		lbl_PanelOrder.setBounds(scrollPane.getX(),scrollPane.getY()+scrollPane.getHeight()+20,100,24);
		contentPanel.add(lbl_PanelOrder);
		
		comboBoxAppendLabelSequence.setBounds(lbl_PanelOrder.getX()+20+lbl_PanelOrder.getWidth(),lbl_PanelOrder.getY(),100,24);
		comboBoxAppendLabelSequence.setModel(new DefaultComboBoxModel<String>(new String[] {"Add First","Add Last"}));
		comboBoxAppendLabelSequence.setSelectedItem(settings.labelOrder);
		contentPanel.add(comboBoxAppendLabelSequence);
		
		JLabel4j_std lbl_PortNo = new JLabel4j_std("Port Number");
		lbl_PortNo.setHorizontalAlignment(SwingConstants.TRAILING);
		lbl_PortNo.setFont(ZPLCommon.font_bold);
		lbl_PortNo.setBounds(comboBoxAppendLabelSequence.getX()+comboBoxAppendLabelSequence.getWidth()+20,comboBoxAppendLabelSequence.getY(),80,24);
		contentPanel.add(lbl_PortNo);
		
		fld_PortNo.setBounds(lbl_PortNo.getX()+lbl_PortNo.getWidth()+10,lbl_PortNo.getY(),50,24);
		fld_PortNo.setText(settings.portNumber);
		fld_PortNo.setHorizontalAlignment(SwingConstants.CENTER);
		contentPanel.add(fld_PortNo);
		
		JLabel4j_std lbl_Magnification = new JLabel4j_std("Default Magnification");
		lbl_Magnification.setHorizontalAlignment(SwingConstants.TRAILING);
		lbl_Magnification.setFont(ZPLCommon.font_bold);
		lbl_Magnification.setBounds(fld_PortNo.getX()+fld_PortNo.getWidth()+20,fld_PortNo.getY(),120,24);
		contentPanel.add(lbl_Magnification);
		
		fld_Magnification.setBounds(lbl_Magnification.getX()+lbl_Magnification.getWidth()+10,lbl_Magnification.getY(),50,24);
		fld_Magnification.setText(settings.magnification);
		fld_Magnification.setHorizontalAlignment(SwingConstants.CENTER);
		contentPanel.add(fld_Magnification);
		
		JLabel4j_std lbl_InputFolder = new JLabel4j_std("Input Folder");
		lbl_InputFolder.setHorizontalAlignment(SwingConstants.TRAILING);
		lbl_InputFolder.setFont(ZPLCommon.font_bold);
		lbl_InputFolder.setBounds(lbl_PanelOrder.getX(),lbl_PanelOrder.getY()+lbl_PanelOrder.getHeight()+10,100,24);
		contentPanel.add(lbl_InputFolder);

		fld_InputFolder.setBounds(comboBoxAppendLabelSequence.getX(),lbl_InputFolder.getY(),600,24);
		fld_InputFolder.setText(settings.defaultInputFolder);
		contentPanel.add(fld_InputFolder);
		
		JLabel4j_std lbl_MaxPages = new JLabel4j_std("Max Pages");
		lbl_MaxPages.setHorizontalAlignment(SwingConstants.TRAILING);
		lbl_MaxPages.setFont(ZPLCommon.font_bold);
		lbl_MaxPages.setBounds(lbl_InputFolder.getX(),lbl_InputFolder.getY()+lbl_InputFolder.getHeight()+10,100,24);
		contentPanel.add(lbl_MaxPages);
		
		fld_MaxPages.setBounds(fld_InputFolder.getX(),lbl_MaxPages.getY(),50,24);
		fld_MaxPages.setText(settings.maxPages);
		fld_MaxPages.setHorizontalAlignment(SwingConstants.CENTER);
		contentPanel.add(fld_MaxPages);
		
		
		JLabel4j_std lbl_SaveToHome = new JLabel4j_std("Save to Home");
		lbl_SaveToHome.setHorizontalAlignment(SwingConstants.TRAILING);
		lbl_SaveToHome.setFont(ZPLCommon.font_bold);
		lbl_SaveToHome.setBounds(lbl_MaxPages.getX(),lbl_MaxPages.getY()+lbl_MaxPages.getHeight()+10,100,24);
		contentPanel.add(lbl_SaveToHome);
		
		boolean savetohome = Boolean.valueOf(settings.saveToHome);
		fld_SaveToHome.setBounds(fld_InputFolder.getX(),lbl_SaveToHome.getY(),30,30);
		fld_SaveToHome.setSelected(savetohome);
		fld_SaveToHome.setHorizontalAlignment(SwingConstants.LEADING);
		fld_SaveToHome.setVerticalAlignment(SwingConstants.NORTH);
		contentPanel.add(fld_SaveToHome);
		fld_SaveToHome.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fld_alternateSaveLocation.setEnabled(!fld_SaveToHome.isSelected());
				btn_SaveFolder.setEnabled(!fld_SaveToHome.isSelected());
				if (fld_alternateSaveLocation.getText().equals(""))
				{
					fld_alternateSaveLocation.setText(System.getProperty("user.home"));
				}
			}
		});
		
		JLabel4j_std lbl_alternateSaveLocation = new JLabel4j_std("Save Location");
		lbl_alternateSaveLocation.setHorizontalAlignment(SwingConstants.TRAILING);
		lbl_alternateSaveLocation.setFont(ZPLCommon.font_bold);
		lbl_alternateSaveLocation.setBounds(lbl_SaveToHome.getX(),lbl_SaveToHome.getY()+lbl_SaveToHome.getHeight()+10,100,24);
		contentPanel.add(lbl_alternateSaveLocation);
		
		fld_alternateSaveLocation.setBounds(fld_InputFolder.getX(),lbl_alternateSaveLocation.getY(),600,24);
		fld_alternateSaveLocation.setText(settings.alternateSaveLocation);
		fld_alternateSaveLocation.setEnabled(!savetohome);
		contentPanel.add(fld_alternateSaveLocation);
		
		JButton4j btn_InputFolder = new JButton4j(ZPLCommon.icon_select_folder);
		btn_InputFolder.setBounds(fld_InputFolder.getX()+fld_InputFolder.getWidth(),fld_InputFolder.getY()-(28-fld_InputFolder.getHeight()),32,32);
		btn_InputFolder.setPreferredSize(new Dimension(32,32));
		btn_InputFolder.setSize(new Dimension(32,32));
		btn_InputFolder.setMaximumSize(new Dimension(32,32));
		btn_InputFolder.setFocusable(false);
		contentPanel.add(btn_InputFolder);
		btn_InputFolder.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File workingFolder = utils.stringToPath(fld_InputFolder.getText());
				File directory = selectDirectory(workingFolder);

				if (directory != null)
				{
					fld_InputFolder.setText(directory.getPath());

				}
			}
		});
		

		btn_SaveFolder.setBounds(fld_alternateSaveLocation.getX()+fld_alternateSaveLocation.getWidth(),fld_alternateSaveLocation.getY()-(28-fld_alternateSaveLocation.getHeight()),32,32);
		btn_SaveFolder.setPreferredSize(new Dimension(32,32));
		btn_SaveFolder.setSize(new Dimension(32,32));
		btn_SaveFolder.setMaximumSize(new Dimension(32,32));
		btn_SaveFolder.setEnabled(!savetohome);
		btn_SaveFolder.setFocusable(false);
		contentPanel.add(btn_SaveFolder);
		btn_SaveFolder.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File workingFolder = utils.stringToPath(fld_alternateSaveLocation.getText());
				File directory = selectDirectory(workingFolder);

				if (directory != null)
				{
					fld_alternateSaveLocation.setText(directory.getPath());

				}
			}
		});
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton4j okButton = new JButton4j(ZPLCommon.icon_ok);
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveFonts(memory);
				
				settings.defaultInputFolder=fld_InputFolder.getText();
				settings.portNumber=fld_PortNo.getText();
				settings.labelOrder=comboBoxAppendLabelSequence.getSelectedItem().toString();
				settings.magnification=fld_Magnification.getText();
				settings.maxPages=fld_MaxPages.getText();
				settings.saveToHome=String.valueOf(fld_SaveToHome.isSelected());
				settings.alternateSaveLocation=fld_alternateSaveLocation.getText();
				setutil.saveConfigToXml(settings);
				
				dispose();
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton4j cancelButton = new JButton4j(ZPLCommon.icon_cancel);
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		int widthadjustment = util.getOSWidthAdjustment();
		int heightadjustment = util.getOSHeightAdjustment();

		GraphicsDevice gd = util.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - JDialogSettings.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - JDialogSettings.this.getHeight()) / 2), JDialogSettings.this.getWidth() + widthadjustment,
				JDialogSettings.this.getHeight() + heightadjustment);

		setVisible(true);
	}

	private void saveFonts(ZPLMemory memory)
	{
		Component[] pans = main.getComponents();
		HashMap<String, ZPLFontProperties> list = new HashMap<String, ZPLFontProperties>();

		for (int x = 0; x < pans.length; x++)
		{
			JPanelFontData data = (JPanelFontData) pans[x];
			ZPLFontProperties property = new ZPLFontProperties();

			property.id = data.fld_FontID.getText();
			property.name = data.fld_FontName.getText();
			property.filename = data.fld_Filename.getText();
			property.height = data.fld_Height.getText();
			property.width = data.fld_Width.getText();
			property.renderer = (String) data.fld_Render.getSelectedItem();
			property.spacing = (String) data.fld_Spacing.getSelectedItem();

			list.put(property.id, property);
		}

		memory.zplFont.saveFontsToXml(list);
		
		memory.zplFont.readFontsFromXml();

	}
	
	private File selectDirectory(File defaultPath)
	{
		File result = null;

		JFileChooser fc = new JFileChooser(defaultPath);

		fc.setApproveButtonText("Select");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(JDialogSettings.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}

		return result;
	}
}
