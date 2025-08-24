package com.commander4j.zpl;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.HashMap;

import javax.swing.ImageIcon;

import com.commander4j.list.JListRenderer;
import com.commander4j.memory.ZPLMemory;
import com.commander4j.barcode.ZPL_GS1_Loader;
import com.commander4j.list.JListFontRenderer;

public class ZPLCommon
{
	public static String helpURL = "http://wiki.commander4j.com";
	
	public static File zplFolderFile = new File(".");
	public static File pdfFolderFile = new File(".");
	
	public static final JListRenderer renderer_list = new JListRenderer();
	public static final JListFontRenderer renderer_fontlist = new JListFontRenderer();
	
	public static HashMap<String,ZPLMemory> config = new  HashMap<String,ZPLMemory>();

	public final static Font font_dates = new Font("Arial", Font.PLAIN, 11);
	public final static Font font_std = new Font("Arial", Font.PLAIN, 11);
	public final static Font font_input = new Font("Arial", Font.PLAIN, 11);
	public final static Font font_input_large = new Font("Arial", Font.PLAIN, 13);
	public final static Font font_popup = new Font("Arial", Font.PLAIN, 11);
	public final static Font font_bold = new Font("Arial", Font.BOLD, 11);
	public final static Font font_italic = new Font("Arial", Font.ITALIC, 11);
	public final static Font font_btn = new Font("Arial", Font.PLAIN, 11);
	public final static Font font_btn_bold = new Font("Arial", Font.BOLD, 9);
	public final static Font font_btn_small = new Font("Arial", Font.PLAIN, 9);
	public final static Font font_btn_small_bold = new Font("Arial", Font.BOLD, 9);
	public final static Font font_title = new Font("Arial", Font.ITALIC, 12);
	public final static Font font_tree = new Font("Arial", Font.PLAIN, 12);
	public final static Font font_menu = new Font("Arial", Font.PLAIN, 12);
	public final static Font font_list = new Font("Monospaced", 0, 11);
	public final static Font font_list_weights = new Font("Monospaced", 0, 14);
	public final static Font font_combo = new Font("Monospaced", Font.PLAIN, 11);
	public final static Font font_table_header = new java.awt.Font("Arial", Font.PLAIN, 11);
	public final static Font font_table = new java.awt.Font("Monospaced", 0, 11);
	public final static Font font_textArea = new java.awt.Font("Monospaced", 0, 14);
	public final static Font font_tree_tooltip = new Font( "Monospaced", Font.PLAIN, 14);
	public final static Font font_bom = new Font("Arial", Font.PLAIN, 14);
	public final static Font font_tree_branch = new Font("Arial", Font.BOLD, 13);
	public final static Font font_tree_leaf = new Font("Arial", Font.PLAIN, 11);
	public final static Font font_tree_root = new Font("Arial", Font.BOLD, 14);
	public final static Font font_terminal = new Font("Dialog", Font.PLAIN, 14);	
	
	public final static Color color_textfield_foreground_focus_color = Color.BLACK;
	public final static Color color_textfield_forground_nofocus_color = Color.BLACK;
	public final static Color color_textfield_background_focus_color = new Color(255, 255, 200);
	public final static Color color_textfield_background_nofocus_color = Color.WHITE;
	public final static Color color_text_maxsize_color = Color.RED;
	public final static Color color_list_assigned = new Color(233, 255, 233);
	public final static Color color_list_unassigned = new Color(255, 240, 255);
	public final static Color color_listFontStandard = Color.BLUE;
	public final static Color color_listFontSelected = Color.BLACK;
	public final static Color color_listBackground = new Color(243,251,255);
	public final static Color color_listHighlighted = new Color(184, 207, 229);
	public final static Color color_tablerow1 = new Color(248, 226, 226);
	public final static Color color_tablerow2 = new Color(240,255,240);
	public final static Color color_tablerow3 = new Color(204, 255, 204);
	public final static Color color_tablebackground = new Color(233, 240, 249);
	public final static Color color_tableHeaderFont = Color.BLACK;
	public final static Color color_text_disabled = Color.BLACK;
	public final static Color color_edit_properties = new Color(241, 241, 241);
	public final static Color color_app_window = new Color(241, 241, 241);
	public final static Color color_button = new Color(233,236,242);
	public final static Color color_button_hover =  new Color(160, 160, 160);
	public final static Color color_button_font = Color.black;
	public final static Color color_button_font_hover = Color.black;
	public final static Color color_textfield_foreground_disabled = Color.BLUE;
	public final static Color color_textfield_background_disabled = new Color(241, 241, 241);
	
	public static String iconPath = "."+File.separator+"images"+File.separator+"appIcons"+File.separator;
	
	public final static ImageIcon icon_connected = new ImageIcon(iconPath+"connected.png");
	public final static ImageIcon icon_disconnected = new ImageIcon(iconPath+"disconnected.png");
	public final static ImageIcon icon_menuStructure = new ImageIcon(iconPath+"folder_tree.png");
	public final static ImageIcon icon_select_folder = new ImageIcon(iconPath+"/select_folder_24x24.png");
	public final static ImageIcon icon_select_file = new ImageIcon(iconPath+"/select_file_24x24.png");
	public final static ImageIcon icon_add = new ImageIcon(iconPath+"/add_24x24.png");
	public final static ImageIcon icon_open = new ImageIcon(iconPath+"/open_file_24x24.png");
	public final static ImageIcon icon_new = new ImageIcon(iconPath+"/new_file_24x24.png");
	public final static ImageIcon icon_save = new ImageIcon(iconPath+"/save_24x24.png");
	public final static ImageIcon icon_password = new ImageIcon(iconPath+"/password_24x24.png");
	public final static ImageIcon icon_erase = new ImageIcon(iconPath+"/eraser_24x24.png");
	public final static ImageIcon icon_font = new ImageIcon(iconPath+"/font_24x24.png");
	public final static ImageIcon icon_duplicate = new ImageIcon(iconPath+"/duplicate_24x24.png");
	public final static ImageIcon icon_release = new ImageIcon(iconPath+"/release_24x24.png");
	public final static ImageIcon icon_hold = new ImageIcon(iconPath+"/hold_24x24.png");
	public final static ImageIcon icon_button_key = new ImageIcon(iconPath+"/button_key_24x24.png");
	public final static ImageIcon icon_about = new ImageIcon(iconPath+"/about_24x24.png");
	public final static ImageIcon icon_help = new ImageIcon(iconPath+"/help_24x24.png");
	public final static ImageIcon icon_exit = new ImageIcon(iconPath+"/exit_24x24.png");
	public final static ImageIcon icon_ok = new ImageIcon(iconPath+"/ok_24x24.png");
	public final static ImageIcon icon_ok_red = new ImageIcon(iconPath+"/ok_24x24_red.png");
	public final static ImageIcon icon_cancel = new ImageIcon(iconPath+"/cancel_24x24.png");
	public final static ImageIcon icon_delete = new ImageIcon(iconPath+"/delete_24x24.png");
	public final static ImageIcon icon_edit = new ImageIcon(iconPath+"/edit_24x24.png");
	public final static ImageIcon icon_file_new = new ImageIcon(iconPath+"/exit_24x24.gif");
	public final static ImageIcon icon_file_open = new ImageIcon(iconPath+"/exit_24x24.gif");
	public final static ImageIcon icon_expandAll = new ImageIcon(iconPath+"/expandall.gif");
	public final static ImageIcon icon_expandNode = new ImageIcon(iconPath+"/expandnode.gif");
	public final static ImageIcon icon_collapseAll = new ImageIcon(iconPath+"/collapseall.gif");
	public final static ImageIcon icon_collapeNode = new ImageIcon(iconPath+"/collapsenode.gif");
	public final static ImageIcon icon_execute = new ImageIcon(iconPath+"/execute_24x24.png");
	public final static ImageIcon icon_settings = new ImageIcon(iconPath+"/settings_24x24.png");
	public final static ImageIcon icon_reload = new ImageIcon(iconPath+"/refresh_24x24.png");
	public final static ImageIcon icon_function = new ImageIcon(iconPath+"/function.gif");
	public final static ImageIcon icon_info = new ImageIcon(iconPath+"/info.gif");
	public final static ImageIcon icon_branchOpen = new ImageIcon(iconPath+"/folder_open.png");
	public final static ImageIcon icon_branchClose = new ImageIcon(iconPath+"/folder_closed.png");
	public final static ImageIcon icon_confirm = new ImageIcon(iconPath+"/Icon_Menu4j.png");
	public final static ImageIcon icon_license = new ImageIcon(iconPath+"/open_source_24x24.png");
	public final static ImageIcon icon_print = new ImageIcon(iconPath+"/print_24x24.png");
	public final static ImageIcon icon_pdf = new ImageIcon(iconPath+"/pdf_24x24.png");
	
	
	public static void init(String uuid)
	{
		ZPL_GS1_Loader gs1_loader = new ZPL_GS1_Loader();
		ZPLMemory memory = new ZPLMemory();
		gs1_loader.loadGS1Data(memory, new File("./xml/config/gs1_app_defs.xml"));
		
		memory.labelSizeWidth=14.8f;
		memory.labelSizeHeight=21f;
		memory.labelSizeUOM="cm";
		memory.printerDPI=300;
		
		config.put(uuid, memory);
	}
}
