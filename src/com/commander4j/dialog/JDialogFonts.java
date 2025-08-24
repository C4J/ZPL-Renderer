package com.commander4j.dialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JLabel4j_std;
import com.commander4j.gui.JList4j;
import com.commander4j.util.ZPLUtility;
import com.commander4j.zpl.ZPLCommon;


public class JDialogFonts extends JDialog
{

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private static int widthadjustment = 0;
	private static int heightadjustment = 0;
	private ZPLUtility util = new ZPLUtility();
	public static Font selectedFont = ZPLCommon.font_input;

	private JList4j<String> fontNamesList = new JList4j<String>();
	private JList4j<String> fontStylesList = new JList4j<String>();
	private JList4j<Integer> fontSizeList = new JList4j<Integer>();
	private JLabel4j_std lblPreviewFont = new JLabel4j_std("Preview of selected font");

	/**
	 * Create the dialog.
	 */
	public JDialogFonts(JFrame parent, Font target)
	{
		super(parent);
		setResizable(false);
		setTitle("Font");
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);

		String currentFontFamilyName = target.getFamily();
		int currentFontStyle = target.getStyle();
		int currentFontSize = target.getSize();

		setBounds(100, 100, 517, 369);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JScrollPane scrollPaneName = new JScrollPane();
		scrollPaneName.setBounds(18, 22, 227, 229);
		contentPanel.add(scrollPaneName);

		JScrollPane scrollPaneStyle = new JScrollPane();
		scrollPaneStyle.setBounds(267, 22, 121, 229);
		contentPanel.add(scrollPaneStyle);

		JScrollPane scrollPaneSize = new JScrollPane();
		scrollPaneSize.setBounds(420, 22, 70, 229);
		contentPanel.add(scrollPaneSize);

		JButton4j btnOk = new JButton4j("Ok");
		btnOk.setIcon(ZPLCommon.icon_ok);
		btnOk.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				selectedFont = lblPreviewFont.getFont();

				dispose();
			}
		});
		btnOk.setBounds(138, 296, 117, 29);
		contentPanel.add(btnOk);

		JButton4j btnCancel = new JButton4j("Cancel");
		btnCancel.setIcon(ZPLCommon.icon_cancel);
		btnCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				selectedFont = target;

				dispose();
			}
		});
		btnCancel.setBounds(254, 296, 117, 29);
		contentPanel.add(btnCancel);

		JLabel4j_std lblName = new JLabel4j_std("Name");
		lblName.setBounds(18, 6, 61, 16);
		contentPanel.add(lblName);

		JLabel4j_std lblStyle = new JLabel4j_std("Style");
		lblStyle.setBounds(269, 6, 61, 16);
		contentPanel.add(lblStyle);

		JLabel4j_std lblSize = new JLabel4j_std("Size");
		lblSize.setBounds(420, 6, 61, 16);
		contentPanel.add(lblSize);

		lblPreviewFont.setBounds(18, 255, 462, 39);
		contentPanel.add(lblPreviewFont);

		// FONT NAMES //

		GraphicsEnvironment ge;
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		String[] fontNames = ge.getAvailableFontFamilyNames();
		DefaultComboBoxModel<String> fontNamesModel = new DefaultComboBoxModel<String>();
		fontNamesList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				previewFont();
			}
		});

		fontNamesList.setModel(fontNamesModel);
		int selectedFontName = -1;

		for (int y = 0; y < fontNames.length; y++)
		{
			fontNamesModel.addElement(fontNames[y]);
			if (fontNames[y].equals(currentFontFamilyName))
			{
				selectedFontName = y;
			}
		}

		fontNamesList.setModel(fontNamesModel);
		fontNamesList.setCellRenderer(ZPLCommon.renderer_fontlist);
		scrollPaneName.setViewportView(fontNamesList);
		fontNamesList.setSelectedIndex(selectedFontName);
		fontNamesList.ensureIndexIsVisible(selectedFontName);

		// FONT STYLES //

		DefaultComboBoxModel<String> fontStylesModel = new DefaultComboBoxModel<String>();
		fontStylesList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				previewFont();
			}
		});

		fontStylesList.setModel(fontStylesModel);
		String[] styleNames = new String[]
		{ "Plain", "Bold", "Italic", "Bold Italic" };

		int selectedStyleName = -1;

		for (int y = 0; y < styleNames.length; y++)
		{
			fontStylesModel.addElement(styleNames[y]);
			if (styleNames[y].equals(util.parseFontStyle(currentFontStyle)))
			{
				selectedStyleName = y;
			}
		}

		fontStylesList.setModel(fontStylesModel);
		fontStylesList.setCellRenderer(ZPLCommon.renderer_list);
		scrollPaneStyle.setViewportView(fontStylesList);
		fontStylesList.setSelectedIndex(selectedStyleName);
		fontStylesList.ensureIndexIsVisible(selectedStyleName);

		// FONT SIZES //

		DefaultComboBoxModel<Integer> fontSizeModel = new DefaultComboBoxModel<Integer>();
		fontSizeList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				previewFont();
			}
		});

		fontSizeList.setModel(fontSizeModel);
		int[] sizeNames = new int[]
		{ 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72 };

		int selectedSizeName = -1;

		for (int y = 0; y < sizeNames.length; y++)
		{
			fontSizeModel.addElement(sizeNames[y]);
			if (sizeNames[y] == currentFontSize)
			{
				selectedSizeName = y;
			}
		}

		fontSizeList.setModel(fontSizeModel);
		fontSizeList.setCellRenderer(ZPLCommon.renderer_list);
		scrollPaneSize.setViewportView(fontSizeList);
		fontSizeList.setSelectedIndex(selectedSizeName);
		fontSizeList.ensureIndexIsVisible(selectedSizeName);

		widthadjustment = util.getOSWidthAdjustment();
		heightadjustment = util.getOSHeightAdjustment();

		GraphicsDevice gd = util.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - JDialogFonts.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - JDialogFonts.this.getHeight()) / 2), JDialogFonts.this.getWidth() + widthadjustment,
				JDialogFonts.this.getHeight() + heightadjustment);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				previewFont();
			}
		});

	}

	private void previewFont()
	{
		if (fontNamesList.isSelectionEmpty() == false)
		{
			if (fontStylesList.isSelectionEmpty() == false)
			{
				if (fontSizeList.isSelectionEmpty() == false)
				{
					String tempName = fontNamesList.getSelectedValue().toString();
					String tempStyle = fontStylesList.getSelectedValue().toString();

					int tempSize = fontSizeList.getSelectedValue().intValue();

					Font newFont = new Font(tempName, util.parseFontStyle(tempStyle), tempSize);

					lblPreviewFont.setFont(newFont);
				}
			}

		}
	}
}
