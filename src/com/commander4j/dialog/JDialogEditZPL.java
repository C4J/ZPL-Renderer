package com.commander4j.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.commander4j.gui.JButton4j;
import com.commander4j.util.ZPLUtility;
import com.commander4j.zpl.ZPLCommon;

/**
 * @author David Garratt
 *
 * Project Name : Commander4j
 *
 * Filename     : JDialogEditZPL.java
 *
 * Package Name : com.commander4j.dialog
 *
 * License      : GNU General Public License
 *
 * Modal editor for the raw ZPL source. The caller passes in the current ZPL
 * text; if the user clicks "Render" the (possibly edited) text can be read back
 * via {@link #getZPLText()} and re-rendered. "Cancel" leaves things unchanged.
 */
public class JDialogEditZPL extends JDialog
{
	private static final long serialVersionUID = 1L;

	private final JTextArea textArea = new JTextArea();
	private boolean accepted = false;
	private final ZPLUtility utils = new ZPLUtility();

	public JDialogEditZPL(JFrame parent, String zpl)
	{
		super(parent);
		setModalityType(ModalityType.DOCUMENT_MODAL);
		setTitle("Edit ZPL");
		setBounds(100, 100, 700, 560);

		getContentPane().setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textArea.setBackground(new Color(224, 255, 255));
		textArea.setText(zpl == null ? "" : zpl);
		textArea.setCaretPosition(0);

		JScrollPane scrollPane = new JScrollPane(textArea);
		contentPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton4j okButton = new JButton4j(ZPLCommon.icon_ok);
		okButton.setText("Render");
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				accepted = true;
				dispose();
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton4j cancelButton = new JButton4j(ZPLCommon.icon_cancel);
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				accepted = false;
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		setModal(true);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		int widthadjustment = utils.getOSWidthAdjustment();
		int heightadjustment = utils.getOSHeightAdjustment();

		GraphicsDevice gd = utils.getGraphicsDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		Rectangle screenBounds = gc.getBounds();

		setBounds(screenBounds.x + ((screenBounds.width - getWidth()) / 2), screenBounds.y + ((screenBounds.height - getHeight()) / 2), getWidth() + widthadjustment, getHeight() + heightadjustment);
	}

	/**
	 * @return true if the user chose to render (OK), false if cancelled/closed.
	 */
	public boolean isAccepted()
	{
		return accepted;
	}

	/**
	 * @return the current contents of the editor.
	 */
	public String getZPLText()
	{
		return textArea.getText();
	}
}
