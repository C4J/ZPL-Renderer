package com.commander4j.util;

import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import com.commander4j.cmd.ZPLCmd;

public class ZPLUtility
{
	
	public File stringToPath(String path )
	{
		File result = null;
		
		Path pathDirectory = Paths.get(path);
		boolean directoryValid = Files.exists(pathDirectory);
		
		if (directoryValid)
		{
			result = new File(path);
		}
		
		return result;
	}
	
	public int parseFontStyle(String style)
	{
		int result = Font.PLAIN;
		
		switch (style)
		{
			case "Plain":
				result = Font.PLAIN;
				break;
			case "Bold":
				result = Font.BOLD;
				break;
			case "Italic":
				result = Font.ITALIC;
				break;
			case "Bold Italic":
				result = Font.BOLD | Font.ITALIC;
				break;
		}
		
		return result;
	}

	public String parseFontStyle(int style)
	{		
		String result = "Plain";
		
		switch (style)
		{
			case Font.PLAIN:
				result = "Plain";
				break;
			case Font.BOLD:
				result = "Bold";
				break;
			case Font.ITALIC:
				result = "Italic";
				break;
		}
		
		if (style == (Font.BOLD | Font.ITALIC))
		{
			result = "Bold Italic";
		}
		
		return result;
	}
	
	public String padString(String input, boolean right, int size, String character)
	{
		int inputlength = 0;
		String result = replaceNullStringwithBlank(input);
		inputlength = result.length();

		if (inputlength > size)
		{
			// result = result.substring(0,size-1);
			result = result.substring(0, size);
		}
		else
		{
			if (inputlength < size)
			{
				if (right == true)
				{
					result = result + padString(size - inputlength, character);
				}
				else
				{
					result = padString(size - inputlength, character) + result;
				}
			}
		}

		return result;
	}
	
	public String padString(int size, String character)
	{
		String s = "";

		for (int i = 0; i < size; i++)
		{
			s = s + character;
		}

		return s;
	}
	
	public String replaceNullStringwithBlank(String value)
	{
		if (value == null)
		{
			value = "";
		}

		return value;
	}
	
	public int getIntWithDefault(ZPLCmd cmd,int argument,int defaultVal)
	{
		int result = defaultVal;
		
		try
		{
			if (cmd.getArgumentCount()>=argument)
			{
				result = Integer.parseInt(cmd.getArgument(argument));
			}
			else
			{
				result = defaultVal;
			}
		}
		catch (Exception ex)
		{
			
		}
		
		return result;
	}
	
	public String getStringWithDefault(ZPLCmd cmd,int argument,String defaultVal)
	{
		String result = defaultVal;
		
		try
		{
			if (cmd.getArgumentCount()>=argument)
			{
				result = cmd.getArgument(argument);
			}
			else
			{
				result = defaultVal;
			}
		}
		catch (Exception ex)
		{
			
		}
		
		return result;
	}
	
	public Double getDoubleWithDefault(ZPLCmd cmd,int argument,Double defaultVal)
	{
		Double result = defaultVal;
		
		try
		{
			if (cmd.getArgumentCount()>=argument)
			{
			result = Double.parseDouble(cmd.getArgument(argument));
			}
			else
			{
				result = defaultVal;
			}
		}
		catch (Exception ex)
		{
			
		}
		
		return result;
	}
	
	public void setLookandFeel()
	{

		try
		{
			SetLookAndFeel("Metal", "Ocean");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void SetLookAndFeel(String LOOKANDFEEL, String THEME)
	{
		try
		{
			if (LOOKANDFEEL.equals("Metal"))
			{
				if (THEME.equals("DefaultMetal"))
					MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
				else if (THEME.equals("Ocean"))
					MetalLookAndFeel.setCurrentTheme(new OceanTheme());

				UIManager.setLookAndFeel(new MetalLookAndFeel());

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setLookAndFeel(String LAF)
	{
		try
		{
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
				if (LAF.equals(info.getName()))
				{
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (Exception e)
		{

		}
	}
	
	public  GraphicsDevice getGraphicsDevice()
	{
		GraphicsDevice result;

		Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice[] devices;

		try
		{
			devices = ge.getScreenDevices();

			GraphicsDevice currentDevice = null;

			for (GraphicsDevice device : devices)
			{
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				if (bounds.contains(mouseLocation))
				{
					currentDevice = device;
					break;
				}
			}

			GraphicsDevice[] gs = ge.getScreenDevices();

			String defaultID = currentDevice.getIDstring();

			int monitorIndex = 0;

			for (int x = 0; x < gs.length; x++)
			{
				if (gs[x].getIDstring().equals(defaultID))
				{
					monitorIndex = x;
					break;
				}
			}

			result = gs[monitorIndex];
		}
		catch (HeadlessException ex)
		{
			result = null;
		}

		return result;
	}
	
	public  int getOSWidthAdjustment()
	{
		int result = 0;
		if (isWindows())
		{
			result = 0;
		}
		if (isMac())
		{
			result = -15;
		}
		if (isSolaris())
		{
			result = 0;
		}
		if (isUnix())
		{
			result = 0;
		}
		return result;
	}
	
	public  int getOSHeightAdjustment()
	{
		int result = 0;
		if (isWindows())
		{
			result = 0;
		}
		if (isMac())
		{
			result = -13;
		}
		if (isSolaris())
		{
			result = 0;
		}
		if (isUnix())
		{
			result = 0;
		}
		return result;
	}
	
	public  boolean isWindows() {
		 
		String os = System.getProperty("os.name").toLowerCase();
		// windows
		return (os.indexOf("win") >= 0);
 
	}
 
	public  boolean isMac() {
 
		String os = System.getProperty("os.name").toLowerCase();
		// Mac
		return (os.indexOf("mac") >= 0);
 
	}
 
	public  boolean isUnix() {
 
		String os = System.getProperty("os.name").toLowerCase();
		// linux or unix
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
 
	}
 
	public  boolean isSolaris() {
 
		String os = System.getProperty("os.name").toLowerCase();
		// Solaris
		return (os.indexOf("sunos") >= 0);
 
	}
	
	public Vector<String> getIPAddresses()
	{
		Vector<String> result = new Vector<String>();

		Enumeration<NetworkInterface> nets;
		try
		{
			nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets))
			{
				Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses))
				{
					if (inetAddress.toString().contains(":") == false)
					{
						result.add(inetAddress.toString().replace("/", ""));
					}
				}
			}
		}
		catch (SocketException e)
		{

		}
		return result;

	}
}
