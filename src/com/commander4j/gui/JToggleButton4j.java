package com.commander4j.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

import com.commander4j.zpl.ZPLCommon;


public class JToggleButton4j extends JToggleButton
{

	private static final long serialVersionUID = 1L;

	private void init()
	{
		setFont(ZPLCommon.font_btn);
		setForeground(ZPLCommon.color_button_font);
		setOpaque(false);
		setBackground(ZPLCommon.color_button);
		
		setBorderPainted(false);
		setContentAreaFilled(false);
		setFocusable(false);
        setFocusPainted(false);

		// Hover listener
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				if (isEnabled())
				{
					setBackground(ZPLCommon.color_button_hover);
					setForeground(ZPLCommon.color_button_font_hover);
					setFont(ZPLCommon.font_bold);

				}
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				setBackground(ZPLCommon.color_button);
				setForeground(ZPLCommon.color_button_font);
				setFont(ZPLCommon.font_btn);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (isEnabled())
				{
					setBackground(ZPLCommon.color_button_hover);
					setForeground(ZPLCommon.color_button_font_hover);
					setFont(ZPLCommon.font_bold);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				setBackground(ZPLCommon.color_button);
				setForeground(ZPLCommon.color_button_font);
				setFont(ZPLCommon.font_btn);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (isEnabled())
				{
					setBackground(ZPLCommon.color_button);
					setForeground(ZPLCommon.color_button_font);
				}
			}
		});
	}
	
	public JToggleButton4j()
	{
		super();
		init();
	}

	public JToggleButton4j(Icon icon)
	{
		super(icon);
		init();
	}

	public JToggleButton4j(String text)
	{
		super(text);
		setToolTipText(text);
		init();
	}

	public JToggleButton4j(Action a)
	{
		super(a);
		init();
	}

	public JToggleButton4j(String text, Icon icon)
	{
		super(text, icon);
		setToolTipText(text);
		init();
	}

	@Override
	public void setText(String text)
	{
		super.setText(text);
		setToolTipText(text);
	}
}
