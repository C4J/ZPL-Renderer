package com.commander4j.zpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZPLHex {

    public String decodeHexSubstitutions(String input,String ch) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String regex = Pattern.quote(ch) + "([0-9A-Fa-f]{2})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1); // captured hex digits
            int value = Integer.parseInt(hex, 16);
            char replacement = (char) value;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(Character.toString(replacement)));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

}
