package com.commander4j.zpl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.UUID;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import com.commander4j.cmd.ZPLCmd;
import com.commander4j.cmd.ZPLCmdList;
import com.commander4j.dialog.JDialogAbout;
import com.commander4j.dialog.JDialogLicenses;
import com.commander4j.dialog.JDialogSettings;
import com.commander4j.filters.JFileFilterPDF;
import com.commander4j.filters.JFileFilterZPL;
import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JLabel4j_std;
import com.commander4j.gui.JToggleButton4j;
import com.commander4j.network.ZPLDataCallback;
import com.commander4j.network.ZPLSocketListener;
import com.commander4j.settings.SettingUtil;
import com.commander4j.settings.Settings;
import com.commander4j.util.JHelp;
import com.commander4j.util.JPanelToPDFVector;
import com.commander4j.util.VectorPdfExactSize;
import com.commander4j.util.ZPLUtility;

public class ZPLFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static final String version = "1.32";

	private JPanel outpanel = new JPanel();

	// Default Magnification
	private float magnification = 0.5f;

	// Amount to zoom in and out by
	private float zoomInOut = 0.05f;
	private float zoomMin = 0.10f;
	private float zoomMax = 2.00f;

	private int pageNo = 1;
	private int pageCount = 0;

	private String currentfilename;

	private JScrollPane scrollPane;

	private JComboBox<String> ipAddress;
	private JComboBox<String> sizeUOM;
	private JComboBox<Integer> printerDPI;

	private JTextField fld_Port;
	private JTextField fld_Magnification;
	private JTextField fld_Pages;

	private JSpinner spn_Width;
	private JSpinner spn_Height;

	private JButton4j btnRefreshFile;
	private JButton4j btnEdit;
	private JButton4j btnOpenFile;

	private String uuid = "";
	private LinkedList<ZPLCmd> zpllist;

	private ZPLUtility util = new ZPLUtility();

	private ZPLDataCallback callback_zpldata;
	private ZPLSocketListener listenerThread;
	private Thread socketThread;

	private Settings settings = new Settings();
	private SettingUtil setutil = new SettingUtil();

	private Dimension separator = new Dimension(10, 10);

	public ZPLFrame(float viewmag)
	{
		super();

		currentfilename = "";

		settings = setutil.readConfigFromXml();

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		util.setLookAndFeel("Nimbus");

		// Create after look and feel
		btnRefreshFile = new JButton4j(ZPLCommon.icon_reload);
		btnEdit = new JButton4j(ZPLCommon.icon_edit);
		btnOpenFile = new JButton4j(ZPLCommon.icon_open);
		ipAddress = new JComboBox<String>();
		fld_Port = new JTextField();
		fld_Magnification = new JTextField();
		fld_Pages = new JTextField();
		sizeUOM = new JComboBox<String>();
		printerDPI = new JComboBox<Integer>();

		resetMagnification();

		setAppTitle("");

		addWindowListener(new WindowListener());

		this.uuid = UUID.randomUUID().toString();

		ZPLCommon.init(uuid);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		outpanel.setBackground(Color.WHITE);
		outpanel.setVisible(true);
		outpanel.setLayout(new BoxLayout(outpanel, BoxLayout.PAGE_AXIS));

		scrollPane = new JScrollPane(outpanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(0, 0, 780, 400);

		getContentPane().add(scrollPane);

		JToolBar toolBarSide = new JToolBar();
		toolBarSide.setBorder(BorderFactory.createEmptyBorder());
		toolBarSide.setBackground(ZPLCommon.color_app_window);
		toolBarSide.setFloatable(false);
		toolBarSide.setOrientation(SwingConstants.VERTICAL);
		getContentPane().add(toolBarSide, BorderLayout.EAST);

		JToolBar toolBarTop = new JToolBar();
		toolBarTop.setBorder(BorderFactory.createEmptyBorder());
		toolBarTop.setBackground(ZPLCommon.color_app_window);
		toolBarTop.setFloatable(false);
		toolBarTop.setOrientation(SwingConstants.HORIZONTAL);
		getContentPane().add(toolBarTop, BorderLayout.NORTH);

		toolBarTop.addSeparator(separator);

		JLabel4j_std lbl_Mode = new JLabel4j_std();
		lbl_Mode.setText("Network : ");
		toolBarTop.add(lbl_Mode);

		JToggleButton4j btnStartNetwork = new JToggleButton4j(ZPLCommon.icon_disconnected);
		btnStartNetwork.setSelected(false);
		btnStartNetwork.setPreferredSize(new Dimension(32, 32));
		btnStartNetwork.setToolTipText("Network ON/OFF");
		btnStartNetwork.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				btnRefreshFile.setEnabled(!btnStartNetwork.isSelected());
				btnEdit.setEnabled(!btnStartNetwork.isSelected());
				btnOpenFile.setEnabled(!btnStartNetwork.isSelected());
				ipAddress.setEnabled(!btnStartNetwork.isSelected());
				fld_Port.setEnabled(!btnStartNetwork.isSelected());

				if (btnStartNetwork.isSelected())
				{

					btnStartNetwork.setIcon(ZPLCommon.icon_connected);

					startSocket();
				}
				else
				{
					btnStartNetwork.setIcon(ZPLCommon.icon_disconnected);

					stopSocket();
				}
			}
		});
		toolBarTop.add(btnStartNetwork);
		toolBarTop.addSeparator(separator);
		JLabel4j_std lbl_Address = new JLabel4j_std();
		lbl_Address.setText(" IP : ");
		toolBarTop.add(lbl_Address);

		Vector<String> ips = new Vector<String>();
		ips = util.getIPAddresses();

		ComboBoxModel<String> jComboBox2Model = new DefaultComboBoxModel<String>(ips);

		ipAddress.setModel(jComboBox2Model);

		ipAddress.setSize(new Dimension(140, 24));
		ipAddress.setMinimumSize(new Dimension(140, 24));
		ipAddress.setPreferredSize(new Dimension(140, 24));
		toolBarTop.add(ipAddress);
		toolBarTop.addSeparator(separator);

		JLabel4j_std lbl_Port = new JLabel4j_std();
		lbl_Port.setText(" Port : ");
		toolBarTop.add(lbl_Port);

		fld_Port.setText("9100");

		numericInput(fld_Port);

		fld_Port.setHorizontalAlignment(JTextField.CENTER);
		fld_Port.setSize(new Dimension(50, 24));
		fld_Port.setMinimumSize(new Dimension(50, 24));
		fld_Port.setPreferredSize(new Dimension(50, 24));

		toolBarTop.add(fld_Port);

		toolBarTop.addSeparator(separator);

		JLabel4j_std lbl_Magnification = new JLabel4j_std();
		lbl_Magnification.setText("Zoom : ");
		toolBarTop.add(lbl_Magnification);

		fld_Magnification.setHorizontalAlignment(JTextField.CENTER);
		fld_Magnification.setSize(new Dimension(50, 24));
		fld_Magnification.setMinimumSize(new Dimension(50, 24));
		fld_Magnification.setPreferredSize(new Dimension(50, 24));
		fld_Magnification.setEnabled(false);
		displayMagnification();

		toolBarTop.add(fld_Magnification);

		toolBarTop.addSeparator(separator);

		JLabel4j_std lbl_Pages = new JLabel4j_std();
		lbl_Pages.setText("Pages : ");
		toolBarTop.add(lbl_Pages);

		fld_Pages.setHorizontalAlignment(JTextField.CENTER);
		fld_Pages.setSize(new Dimension(40, 24));
		fld_Pages.setMinimumSize(new Dimension(40, 24));
		fld_Pages.setPreferredSize(new Dimension(40, 24));
		fld_Pages.setEnabled(false);
		displayPageCount();

		toolBarTop.add(fld_Pages);

		toolBarTop.addSeparator(separator);

		JLabel4j_std lbl_Width = new JLabel4j_std();
		lbl_Width.setText(" Size : ");
		toolBarTop.add(lbl_Width);

		SpinnerNumberModel model = new SpinnerNumberModel(1.0, 1.0, 99.0, 1.0);
		spn_Width = new JSpinner(model);

		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spn_Width, "#0.0");
		DecimalFormat format = editor.getFormat();
		format.setMinimumFractionDigits(1);
		format.setMaximumFractionDigits(1);
		spn_Width.setEditor(editor);
		spn_Width.setValue(15.2);
		spn_Width.setToolTipText("Label Width");
		spn_Width.setPreferredSize(new Dimension(70, 24));
		spn_Width.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				updatePaperSize();
			}
		});

		toolBarTop.add(spn_Width);
		JLabel4j_std lbl_w = new JLabel4j_std();
		lbl_w.setHorizontalAlignment(JLabel.CENTER);
		lbl_w.setText("x");
		lbl_w.setPreferredSize(new Dimension(10, 24));
		toolBarTop.add(lbl_w);

		SpinnerNumberModel model2 = new SpinnerNumberModel(1.0, 1.0, 99.0, 1.0);
		spn_Height = new JSpinner(model2);
		JSpinner.NumberEditor editor2 = new JSpinner.NumberEditor(spn_Height, "#0.0");
		DecimalFormat format2 = editor2.getFormat();
		format2.setMinimumFractionDigits(1);
		format2.setMaximumFractionDigits(1);

		spn_Height.setEditor(editor2);
		spn_Height.setValue(22);
		spn_Height.setToolTipText("Label Height");
		spn_Height.setPreferredSize(new Dimension(70, 24));
		spn_Height.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				updatePaperSize();
			}
		});

		toolBarTop.add(spn_Height);

		toolBarTop.addSeparator(separator);

		sizeUOM.setPreferredSize(new Dimension(60, 24));
		sizeUOM.setModel(new DefaultComboBoxModel<String>(new String[]
		{ "cm", "inch" }));
		sizeUOM.setSelectedItem(ZPLCommon.config.get(uuid).labelSizeUOM);
		sizeUOM.setToolTipText("Unit of Measure");
		sizeUOM.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updatePaperSize();
			}
		});
		toolBarTop.add(sizeUOM);

		toolBarTop.addSeparator(separator);

		JLabel4j_std lbl_dpi = new JLabel4j_std();
		lbl_dpi.setHorizontalAlignment(JLabel.CENTER);
		lbl_dpi.setText("dpi");
		lbl_dpi.setPreferredSize(new Dimension(30, 24));
		toolBarTop.add(lbl_dpi);

		printerDPI.setPreferredSize(new Dimension(60, 24));
		printerDPI.setModel(new DefaultComboBoxModel<Integer>(new Integer[]
		{ 152, 203, 300, 600 }));
		printerDPI.setSelectedItem(ZPLCommon.config.get(uuid).printerDPI);
		printerDPI.setToolTipText("Printer Dot Per Inch");
		printerDPI.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updatePaperSize();
			}
		});
		toolBarTop.add(printerDPI);

		// updatePaperSize();

		btnOpenFile = new JButton4j(ZPLCommon.icon_open);
		btnOpenFile.setPreferredSize(new Dimension(32, 32));
		btnOpenFile.setToolTipText("Open ZPL File");
		btnOpenFile.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				openZPL();
			}
		});
		toolBarSide.add(btnOpenFile);

		btnRefreshFile.setToolTipText("Reload File");
		btnRefreshFile.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fileZPLassignToPanels(currentfilename, uuid);

			}
		});

		toolBarSide.add(btnRefreshFile);

		btnEdit.setToolTipText("Edit ZPL");
		toolBarSide.add(btnEdit);
		btnEdit.setPreferredSize(new Dimension(32, 32));
		btnEdit.setFocusable(false);
		btnEdit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

			}
		});

		JButton4j btnZoomIn = new JButton4j(ZPLCommon.icon_add);
		btnZoomIn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				magnification = setMagnification(true);
			}
		});
		btnZoomIn.setToolTipText("Zoom In");
		toolBarSide.add(btnZoomIn);

		JButton4j btnZoomOut = new JButton4j(ZPLCommon.icon_delete);
		btnZoomOut.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				magnification = setMagnification(false);
			}
		});
		btnZoomOut.setToolTipText("Zoom Out");
		toolBarSide.add(btnZoomOut);

		JButton4j btnErase = new JButton4j(ZPLCommon.icon_erase);
		btnErase.setToolTipText("Clear Screen");
		toolBarSide.add(btnErase);
		btnErase.setPreferredSize(new Dimension(32, 32));
		btnErase.setFocusable(false);
		btnErase.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				clear();
			}
		});

		JButton4j btnSettings = new JButton4j(ZPLCommon.icon_settings);
		btnSettings.setToolTipText("Settings");
		toolBarSide.add(btnSettings);
		btnSettings.setPreferredSize(new Dimension(32, 32));
		btnSettings.setFocusable(false);
		btnSettings.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialogSettings settingDialog = new JDialogSettings(ZPLFrame.this, ZPLCommon.config.get(uuid), settings);
				settingDialog.setVisible(true);
			}
		});

		JButton4j btnPrint = new JButton4j(ZPLCommon.icon_print);
		btnPrint.setToolTipText("Print");
		toolBarSide.add(btnPrint);
		btnPrint.setPreferredSize(new Dimension(32, 32));
		btnPrint.setFocusable(false);
		btnPrint.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					VectorPdfExactSize.printPanelWithoutClipping(outpanel, "job");
				}
				catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		JButton4j btnPDF = new JButton4j(ZPLCommon.icon_pdf);
		btnPDF.setToolTipText("Save as PDF");
		toolBarSide.add(btnPDF);
		btnPDF.setPreferredSize(new Dimension(32, 32));
		btnPDF.setFocusable(false);
		btnPDF.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File savePDF = selectPDFFile();
				if (savePDF != null)
				{
					try
					{
						JPanelToPDFVector.savePanelAsVectorPDF(outpanel, savePDF);
					}
					catch (Exception e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		JButton4j btnAbout = new JButton4j(ZPLCommon.icon_about);
		btnAbout.setToolTipText("About");
		btnAbout.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialogAbout about = new JDialogAbout();
				about.setVisible(true);
			}
		});
		toolBarSide.add(btnAbout);

		JButton4j btnLicense = new JButton4j(ZPLCommon.icon_license);
		btnLicense.setToolTipText("Licences");
		btnLicense.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialogLicenses dl = new JDialogLicenses(ZPLFrame.this);
				dl.setVisible(true);
			}
		});
		toolBarSide.add(btnLicense);

		JButton4j btnHelp = new JButton4j(ZPLCommon.icon_help);
		btnHelp.setToolTipText("Help");

		final JHelp help = new JHelp();
		help.enableHelpOnButton(btnHelp, "https://wiki.commander4j.com");
		toolBarSide.add(btnHelp);

		JButton4j btnExit = new JButton4j(ZPLCommon.icon_exit);
		btnExit.setToolTipText("Exit Application");
		btnExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				confirmExit();
			}
		});
		toolBarSide.add(btnExit);

		updatePaperSize();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				fld_Port.requestFocus();
				fld_Port.setCaretPosition(fld_Port.getText().length());

			}
		});

		setSize(970, 920); // smaller viewport to allow scrolling
		setLocationRelativeTo(null);

		int widthadjustment = util.getOSWidthAdjustment();
		int heightadjustment = util.getOSHeightAdjustment();

		GraphicsDevice gd = util.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - ZPLFrame.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - ZPLFrame.this.getHeight()) / 2), ZPLFrame.this.getWidth() + widthadjustment, ZPLFrame.this.getHeight() + heightadjustment);
		setVisible(true);

	}

	private void startSocket()
	{
		callback_zpldata = zplBlock -> javax.swing.SwingUtilities.invokeLater(() -> processZPL(zplBlock, uuid));
		listenerThread = new ZPLSocketListener(Integer.valueOf(fld_Port.getText()), callback_zpldata, ipAddress.getSelectedItem().toString());
		socketThread = new Thread(listenerThread, "ZPL-Listener");
		socketThread.setDaemon(true);
		socketThread.start();
	}

	private void stopSocket()
	{
		try
		{
			if (listenerThread != null)
			{
				listenerThread.stop(); // closes the ServerSocket and unblocks
										// accept()
				socketThread.join(); // wait for the listener thread to finish
			}
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
		}
	}

	private void confirmExit()
	{

		int question = JOptionPane.showConfirmDialog(ZPLFrame.this, "Exit application ?", "Confirm", JOptionPane.YES_NO_OPTION, 0, ZPLCommon.icon_confirm);
		if (question == 0)
		{
			stopSocket();
			System.exit(0);
		}
	}

	class WindowListener extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			confirmExit();
		}
	}

	private void clear()
	{
		currentfilename = "";
		outpanel.removeAll();
		outpanel.revalidate();
		outpanel.repaint();

		setAppTitle(currentfilename);

		pageCount = 0;

		ZPLCommon.config.get(uuid).zplFont.zplFontCache.clear();

		resetMagnification();
		displayPageCount();
		updatePaperSize();

		scrollPane.revalidate();
		scrollPane.repaint();
	}

	private void resetMagnification()
	{
		magnification = Float.parseFloat(settings.magnification);
		displayMagnification();
	}

	private File selectZPLFile()
	{
		File result = null;

		ZPLCommon.zplFolderFile = new File(settings.defaultInputFolder);

		JFileChooser fc = new JFileChooser(ZPLCommon.zplFolderFile);
		fc.setSelectedFile(ZPLCommon.zplFolderFile);

		JFileFilterZPL ffi = new JFileFilterZPL();
		fc.setApproveButtonText("Open");
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(ZPLFrame.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();

			ZPLCommon.zplFolderFile = result;
		}

		return result;
	}

	private void openZPL()
	{
		File openZPL = selectZPLFile();

		if (openZPL != null)
		{
			clear();
			fileZPLassignToPanels(openZPL.getPath(), uuid);
		}
	}

	private void setAppTitle(String filename)
	{
		if (filename.equals(""))
		{
			setTitle("ZPL Viewer " + version);
		}
		else
		{
			setTitle("ZPL Viewer " + version + " (" + filename + ")");
		}
	}

	private void fileZPLassignToPanels(String filename, String uuid)
	{
		// All of the commands in the entire file.

		currentfilename = filename;

		pageNo = 1;

		ZPLCommon.config.get(uuid).zplFont.zplFontCache.clear();

		setAppTitle(currentfilename);

		resetPages();

		ZPLParser zplparse = new ZPLParser(this.uuid);

		String zpl = zplparse.readFromFile(currentfilename);

		processZPL(zpl, uuid);

	}

	public void socketZPLassignToPanels(String zpl, String uuid)
	{
		// All of the commands in the entire file.

		currentfilename = "";

		pageNo = 1;

		setAppTitle("");

		resetPages();

		processZPL(zpl, uuid);
	}

	private void resetPages()
	{
		outpanel.removeAll();
		pageCount = 0;
		displayPageCount();
	}

	private void processZPL(String zpl, String uuid)
	{
		ZPLParser zplparse = new ZPLParser(uuid);

		ZPLCmdList list = zplparse.parseBytes(zpl);

		zpllist = list.getCommands();

		boolean withinLabel = false;
		boolean labelPrints = false;

		LinkedList<ZPLCmd> currentLabel = new LinkedList<ZPLCmd>();

		// Assuming 'scrollPane' is your JScrollPane instance
		scrollPane.getViewport().setViewPosition(new Point(0, 0));

		for (int x = 0; x < zpllist.size(); x++)
		{
			String cmd = zpllist.get(x).getCommand();

			// Start of Label
			if (cmd.equals("^XA"))
			{
				withinLabel = true;
				currentLabel = new LinkedList<ZPLCmd>();
			}

			if (withinLabel)
			{
				// Add current Command to current Label List
				currentLabel.add(zpllist.get(x));

				if (ZPLCommon.config.get(uuid).zplindex.zplDescription.get(cmd).prints)
				{
					// If Command prints something set flag
					labelPrints = true;
				}
			}

			// End of Label
			if (cmd.equals("^XZ"))
			{

				if (labelPrints)
				{
					ZPLPanel viewPanel = new ZPLPanel(currentLabel, uuid, currentfilename, magnification, pageNo);
					Dimension size = new Dimension((int) ZPLCommon.config.get(uuid).printerMariginRight, (int) ZPLCommon.config.get(uuid).printerMariginBottom);
					viewPanel.setPreferredSize(size);
					viewPanel.setMinimumSize(size);
					viewPanel.setMaximumSize(size);
					viewPanel.setBorder(new LineBorder(Color.RED));
					pageNo++;
					pageCount++;


					if (settings.labelOrder.equals("Add First"))
					{
						outpanel.add(viewPanel, 0);
					}
					else
					{
						outpanel.add(viewPanel);
					}

					int maxLabels = Integer.valueOf(settings.maxPages);

					int currentLabels = outpanel.getComponents().length;
					

					if (currentLabels > maxLabels)
					{
						if (settings.labelOrder.equals("Add First"))
						{
							//outpanel.remove(0);
							outpanel.remove(maxLabels);
						}
						else
						{
							outpanel.remove(0);
						}
						pageCount=maxLabels;
					}
					displayPageCount();

					viewPanel.revalidate();
					viewPanel.repaint();
					scrollPane.revalidate();
					scrollPane.repaint();
				}

				withinLabel = false;

			}

		}
		scrollPane.revalidate();
		scrollPane.repaint();

	}

	private float setMagnification(boolean updown)
	{

		if ((updown && (magnification < zoomMax)) || (!updown && (magnification > zoomMin)))
		{

			int steps = Math.round(magnification / zoomInOut);

			// Apply change
			steps += updown ? 1 : -1;

			// Convert back to float
			float proposal = steps * zoomInOut;

			for (Component comp : outpanel.getComponents())
			{

				if (comp instanceof ZPLPanel)
				{
					ZPLPanel x = (ZPLPanel) comp;

					proposal = (int) (proposal * 100) / 100f;

					x.updateMagnification(proposal);

					magnification = proposal;
				}
			}

			displayMagnification();

			updatePaperSize();
		}

		return magnification;

	}

	private void setPaperSize(float width, float height, String uom, int dpi)
	{
		ZPLCommon.config.get(uuid).labelSizeWidth = width;
		ZPLCommon.config.get(uuid).labelSizeHeight = height;
		ZPLCommon.config.get(uuid).labelSizeUOM = uom;
		ZPLCommon.config.get(uuid).printerDPI = dpi;

		switch (uom)
		{
		case "cm":
		{
			ZPLCommon.config.get(uuid).printerMariginBottom = (int) Math.round((ZPLCommon.config.get(uuid).labelSizeHeight / 2.54) * ZPLCommon.config.get(uuid).printerDPI * magnification);
			ZPLCommon.config.get(uuid).printerMariginRight = (int) Math.round((ZPLCommon.config.get(uuid).labelSizeWidth / 2.54) * ZPLCommon.config.get(uuid).printerDPI * magnification);
			break;
		}
		case "inch":
		{
			ZPLCommon.config.get(uuid).printerMariginBottom = (int) Math.round(ZPLCommon.config.get(uuid).labelSizeHeight * ZPLCommon.config.get(uuid).printerDPI * magnification);
			ZPLCommon.config.get(uuid).printerMariginRight = (int) Math.round(ZPLCommon.config.get(uuid).labelSizeWidth * ZPLCommon.config.get(uuid).printerDPI * magnification);
			break;
		}
		}

		Dimension newSize = new Dimension((int) ZPLCommon.config.get(uuid).printerMariginRight, (int) ZPLCommon.config.get(uuid).printerMariginBottom);

		for (Component comp : outpanel.getComponents())
		{
			if (comp instanceof JPanel)
			{
				JPanel child = (JPanel) comp;

				child.setPreferredSize(newSize);
				child.setMinimumSize(newSize);
				child.setMaximumSize(newSize);

				child.revalidate();
				child.repaint();

			}
		}

		outpanel.revalidate();
		outpanel.repaint();
		scrollPane.revalidate();
		scrollPane.repaint();
	}

	private File selectPDFFile()
	{
		File result = null;

		JFileChooser fc = new JFileChooser(ZPLCommon.pdfFolderFile);
		fc.setSelectedFile(ZPLCommon.pdfFolderFile);

		JFileFilterPDF ffi = new JFileFilterPDF();
		fc.setApproveButtonText("Save");
		fc.addChoosableFileFilter(ffi);
		fc.setFileFilter(ffi);
		fc.setMultiSelectionEnabled(false);

		int returnVal = fc.showOpenDialog(ZPLFrame.this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			result = fc.getSelectedFile();
		}
		else
		{
			result = null;
		}

		return result;
	}

	private void numericInput(JTextField numberField)
	{
		((AbstractDocument) numberField.getDocument()).setDocumentFilter(new DocumentFilter()
		{
			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
			{
				if (string != null)
				{
					replace(fb, offset, 0, string, attr);
				}
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
			{
				Document doc = fb.getDocument();
				String oldText = doc.getText(0, doc.getLength());
				StringBuilder newText = new StringBuilder(oldText);
				newText.replace(offset, offset + length, text);

				if (isValidNumber(newText.toString()))
				{
					super.replace(fb, offset, length, text, attrs);
				}
				else
				{
					// Optional: beep on invalid input
					Toolkit.getDefaultToolkit().beep();
				}
			}

			private boolean isValidNumber(String text)
			{
				if (text.isEmpty())
					return false; // allow empty for editing
				try
				{
					float value = Float.parseFloat(text);
					return value >= 1 && value <= 99999;
				}
				catch (NumberFormatException e)
				{
					return false;
				}
			}
		});
	}

	private void displayMagnification()
	{
		fld_Magnification.setText(String.format("%.2f", magnification));
	}

	private void displayPageCount()
	{
		fld_Pages.setText(String.valueOf(pageCount));
	}

	private void updatePaperSize()
	{
		setPaperSize(Float.parseFloat(spn_Width.getValue().toString()), Float.parseFloat(spn_Height.getValue().toString()), sizeUOM.getSelectedItem().toString(), (int) printerDPI.getSelectedItem());
	}

}
