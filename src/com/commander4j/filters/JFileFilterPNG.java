package com.commander4j.filters;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class JFileFilterPNG extends FileFilter
{
	public boolean accept(File f) {
		if (f.isDirectory())
		{
			return true;
		}

		String extension = JFileFilterPNGTypes.getExtension(f);
		if (extension != null)
		{
			return extension.equals(JFileFilterPNGTypes.PNG);
		}

		return false;
	}

	public String getDescription() {
		return "PNG File";
	}

}
