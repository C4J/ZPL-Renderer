package com.commander4j.memory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.commander4j.barcode.ZPL_GS1_AppData;
import com.commander4j.cmd.ZPLCmdDescription;
import com.commander4j.font.ZPLFont;
import com.commander4j.zpl.ZPLPropertyStore;

public class ZPLMemory
{
	public  ZPLCmdDescription zplindex = new ZPLCmdDescription();
	
	public  ZPLState state = new ZPLState();
	
	public  ZPLPropertyStore bps = new  ZPLPropertyStore();
	
	public  ZPLFont zplFont = new ZPLFont();

    public Charset currentCharset = StandardCharsets.ISO_8859_1; // Default for ZPL (same as ^CI13)
    
    public HashMap<String,ZPL_GS1_AppData> app_id_data = new HashMap<String,ZPL_GS1_AppData>();
    
    public String labelSizeUOM = "cm";
    
    public float labelSizeWidth = 10;
    
    public float labelSizeHeight = 10;
    
    public int printerDPI = 300;
    
    public float printerMariginRight = 300;
    
    public float printerMariginBottom = 300;
    
    public String anchor_mode = "";
}
