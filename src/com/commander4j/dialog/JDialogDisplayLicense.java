package com.commander4j.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.commander4j.gui.JButton4j;
import com.commander4j.util.ZPLUtility;
import com.commander4j.xml.JLicenseInfo;
import com.commander4j.zpl.ZPLCommon;

public class JDialogDisplayLicense extends JDialog
{

	private static final long serialVersionUID = 1L;
	private JPanel contentPanel = new JPanel();
	private JTextPane textPane = new JTextPane();
	private JScrollPane scrollPane = new JScrollPane();
	private static int widthadjustment = 0;
	private static int heightadjustment = 0;
	private ZPLUtility utils = new ZPLUtility();

	public JDialogDisplayLicense(JDialogLicenses lll, JLicenseInfo license)
	{

		super(lll);
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setTitle(license.getDescription()+ " ("+license.type+")");
		setBounds(100, 100, 568, 562);
		
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);


		scrollPane.setBounds(0, 0, 548, 492);
		contentPanel.add(scrollPane);

		loadLicense(license.licenceFilename);
		
		textPane.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		textPane.setBackground(new Color(224, 255, 255));

		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);


		JPanel buttonPane = new JPanel();
		buttonPane.setBounds(0, 487, 755, 39);
		contentPanel.add(buttonPane);
		buttonPane.setLayout(null);
		{
			JButton4j okButton = new JButton4j(ZPLCommon.icon_ok);
			okButton.setText("Ok");
			okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dispose();
				}
			});
			okButton.setBounds(205, 6, 128, 32);
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);
		}


		setResizable(false);
		setModal(true);

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		widthadjustment = utils.getOSWidthAdjustment();
		heightadjustment = utils.getOSHeightAdjustment();

		GraphicsDevice gd = utils.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - JDialogDisplayLicense.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - JDialogDisplayLicense.this.getHeight()) / 2), JDialogDisplayLicense.this.getWidth() + widthadjustment,
				JDialogDisplayLicense.this.getHeight() + heightadjustment);

		
	}

	private boolean loadLicense(String filename)
	{
		boolean result = false;
		System.out.println(filename);
		String getfilename = "." + File.separator + "lib" + File.separator + "license" + File.separator + filename;
		System.out.println(getfilename);

		try
		{
            String content = new String ( Files.readAllBytes( Paths.get(getfilename) ) );
			textPane.setText(content);
			textPane.setCaretPosition(0);
			JViewport jv = scrollPane.getViewport();
			jv.setViewPosition(new Point(0,0));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return result;
	}


}
