package com.commander4j.zpl;
import java.util.HashMap;
import java.util.Map;

public class ZPLPropertyStore {
	
	public static final String Param_Label_Top 						= "Label Top";
	public static final String Param_Label_Left 					= "Label Left";
	public static final String Param_X 								= "X";
	public static final String Param_Y 								= "Y";
	public static final String Param_Module_Width 					= "Module Width";
	public static final String Param_Module_Width_Narrow_Ratio  	= "Module Width Narrow Ratio";
	public static final String Param_Barcode_Height 				= "Barcode Height";
	public static final String Param_Security_Level 				= "Security Level";
	public static final String Param_No_Cols_To_Encode 				= "No Of Columns to Encode";
	public static final String Param_No_Rows_To_Encode 				= "No Of Rows to Encode";
	public static final String Param_Truncate 						= "Truncate";
	public static final String Param_Text 							= "Text";
	public static final String Param_Text_Modified 					= "Text for OKAPI EAN128";
	public static final String Param_Hex_Mode 						= "Hex Mode";
	public static final String Param_Hex_Prefix_Character 			= "Hex Prefix Character";
	public static final String Param_Reverse_Colours 				= "Reverse Colours";
	public static final String Param_Barcode_Type					= "Barcode Type";
	public static final String Param_CheckDigit			    		= "CheckDigit";
	public static final String Param_Mod43_CheckDigit			    = "Mod-43 CheckDigit";
	public static final String Param_Mod10_CheckDigit			    = "Mod-10 CheckDigit";
	public static final String Param_Orientation 					= "Orientation";
	public static final String Param_Justification					= "Justification";
	public static final String Param_Barcode_Interpretation 		= "Intertpretation";
	public static final String Param_Barcode_Interpretation_Above	= "Intertpretation Above";
	public static final String Param_Barcode_UCC_Check_Digit		= "UCC Check Digit";
	public static final String Param_Barcode_Code128_Mode			= "Code128 Mode";
	public static final String Param_Font_Name						= "Font Name";
	public static final String Param_Font_Filename				    = "Font Filename";
	public static final String Param_Font_Zebra_Height				= "Zebra Font Height";
	public static final String Param_Font_Zebra_Width				= "Zebra Font Width";
	public static final String Param_Font_Zebra_Font_Render		    = "Zebra Font Render";
	public static final String Param_Font_Zebra_Font_Spacing		= "Zebra Font Spacing";
	public static final String Param_Font_ID						= "Font ID";
	public static final String Param_Font_Width						= "Font Width";
	public static final String Param_Font_Height					= "Font Height";
	public static final String Param_Font_Rotation					= "Font Rotation";
	public static final String Param_Magnification					= "Magnification";
	public static final String Param_ECICs							= "ECICs";
	public static final String Param_Error_Control_Symbol_Size_Type	= "Error Control Symbol Size Type";
	public static final String Param_Menu_Symbol_Indicator			= "Menu Symbol Indicator";
	public static final String Param_No_Of_Symbols					= "Number of Symbols";
	public static final String Param_Optional_ID_Field				= "Optional ID Field";
	public static final String Param_Character_Set					= "Character Set";
	public static final String Param_Model							= "Model";
	public static final String Param_Error_Correction				= "Error Correction";
	public static final String Param_Mask_Value						= "Mask Value";

    // Outer map: Key is barcode type (e.g. "^B1"), value is another map of properties
    private final Map<String, Map<String, Object>> barcodeProperties = new HashMap<>();

    public void delete(String barcodeType)
    {
    	if (barcodeProperties.containsKey(barcodeType))
    	{
    		barcodeProperties.remove(barcodeType);
    	}
    }
    
    /**
     * Stores a parameter value for a given barcode type and parameter name.
     *
     * @param barcodeType    The barcode ZPL command (e.g. "^B1")
     * @param parameterName  The name of the parameter (e.g. "Orientation")
     * @param parameterValue The value of the parameter (any Object: String, Integer, etc.)
     */
    public void store(String barcodeType, String parameterName, Object parameterValue) {
        barcodeProperties
            .computeIfAbsent(barcodeType, k -> new HashMap<>())
            .put(parameterName, parameterValue);
    }

    /**
     * Retrieves a parameter value for a given barcode type and parameter name.
     *
     * @param barcodeType   The barcode ZPL command
     * @param parameterName The name of the parameter
     * @return The value stored, or null if not found
     */
    public Object recall(String barcodeType, String parameterName) {
        Map<String, Object> params = barcodeProperties.get(barcodeType);
        return (params != null) ? params.get(parameterName) : null;
    }
        
    public int recallAsIntegerWithDefault(String barcodeType, String parameterName, int defaultValue) {
        Map<String, Object> params = barcodeProperties.get(barcodeType);
        if (params == null) return defaultValue;

        Object value = params.get(parameterName);
        if (value == null) return defaultValue;

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public double recallAsDoubleWithDefault(String barcodeType, String parameterName, double defaultValue) {
        Map<String, Object> params = barcodeProperties.get(barcodeType);
        if (params == null) return defaultValue;

        Object value = params.get(parameterName);
        if (value == null) return defaultValue;

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public float recallAsFloatWithDefault(String barcodeType, String parameterName, float defaultValue) {
        Map<String, Object> params = barcodeProperties.get(barcodeType);
        if (params == null) return defaultValue;

        Object value = params.get(parameterName);
        if (value == null) return defaultValue;

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        try {
            return Float.parseFloat(value.toString().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String recallAsStringWithDefault(String barcodeType, String parameterName, String defaultValue) {
        Map<String, Object> params = barcodeProperties.get(barcodeType);

        if (params == null) return defaultValue;

        Object value = params.get(parameterName);
        return (value != null) ? value.toString() : defaultValue;
    }

    
    public boolean recallAsBooleanWithDefault(String barcodeType, String parameterName, Boolean defaultValue) {
        Map<String, Object> params = barcodeProperties.get(barcodeType);

        if (params == null) return defaultValue;

        Object value = params.get(parameterName);
        if (value == null) return defaultValue;

        // Accepts Boolean or String representation ("true"/"false")
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            String strValue = value.toString().trim().toLowerCase();
            if ("true".equals(strValue)) return true;
            if ("false".equals(strValue)) return false;
            return defaultValue; // fallback if value is not clearly true/false
        }
    }

    // Optional: a typed variant for convenience
    public <T> T recall(String barcodeType, String parameterName, Class<T> clazz) {
        Object value = recall(barcodeType, parameterName);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    // For demonstration purposes
    public static void main(String[] args) {
        ZPLPropertyStore store = new ZPLPropertyStore();

        store.store("^B1", "Orientation", "N");
        store.store("^B1", "BarcodeHeight", 100);
        store.store("^B1", "ModuleWidth", 2.5);

        String orientation = store.recall("^B1", "Orientation", String.class);
        Integer height = store.recall("^B1", "BarcodeHeight", Integer.class);
        Double moduleWidth = store.recall("^B1", "ModuleWidth", Double.class);

        System.out.println("Orientation: " + orientation);
        System.out.println("Height: " + height);
        System.out.println("Module Width: " + moduleWidth);
    }
}
