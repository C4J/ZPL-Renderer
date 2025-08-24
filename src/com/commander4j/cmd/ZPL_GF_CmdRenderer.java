package com.commander4j.cmd;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Renders ^GF graphics from a parsed ZPLCmd.
 *
 * Expected ZPLCmd for ^GFA:
 *   getCommand() -> "^GFA"
 *   getArgument(0) -> "A" (format)
 *   getArgument(1) -> a = byteCount (not strictly enforced for A)
 *   getArgument(2) -> b = fieldCount (total bytes of image = rows * bytesPerRow)
 *   getArgument(3) -> c = bytesPerRow
 *   getArgument(4..n) -> ASCII-hex graphic data (with Zebra/Labelary shorthands)
 */
public final class ZPL_GF_CmdRenderer {

    // ---- Behavior toggles ----
    /** If true: data bit '1' renders black; if false: inverted. */
    private static final boolean ONE_IS_BLACK = true;

    /** If true: '!' pads remainder of row with 'F' (black); if false: pads with '0' (white). */
    private static final boolean EXCLAMATION_PADS_BLACK = true;

    private ZPL_GF_CmdRenderer() {}

    /** Decode ^GF (from your ZPLCmd) into a monochrome ARGB image (white bg, black=1). */
    public static BufferedImage decodeToImage(ZPLCmd cmd) {
        if (cmd == null) throw new IllegalArgumentException("cmd is null");
        String c = cmd.getCommand();
        if (c == null || !c.startsWith("^GF")) throw new IllegalArgumentException("Not a ^GF command: " + c);

        if (cmd.getArgumentCount() < 4) {
            throw new IllegalArgumentException("^GF needs at least 4 args: format,a,b,c");
        }

        // arg0 = format letter (A/B/C). If blank, fall back to suffix of command.
        String fmtStr = safe(cmd.getArgument(0)).trim();
        char format = fmtStr.isEmpty() ? ((c.length() >= 4) ? Character.toUpperCase(c.charAt(3)) : 'A')
                                       : Character.toUpperCase(fmtStr.charAt(0));
        if (format != 'A' && format != 'B' && format != 'C') format = 'A';

        // a, b, c
        @SuppressWarnings("unused")
		int byteCount   = parseInt(cmd.getArgument(1), "byteCount(a)");
        int fieldCount  = parseInt(cmd.getArgument(2), "fieldCount(b)");
        int bytesPerRow = parseInt(cmd.getArgument(3), "bytesPerRow(c)");

        if (bytesPerRow <= 0) throw new IllegalArgumentException("bytesPerRow must be > 0");
        if (fieldCount % bytesPerRow != 0) {
            throw new IllegalArgumentException("fieldCount (" + fieldCount + ") must be a multiple of bytesPerRow (" + bytesPerRow + ")");
        }

        int widthPx  = bytesPerRow * 8;
        int heightPx = fieldCount / bytesPerRow;

        // Rebuild data stream from arg4..end (reinsert commas literally)
        StringBuilder data = new StringBuilder();
        for (int i = 4; i < cmd.getArgumentCount(); i++) {
            if (i > 4) data.append(',');
            String part = cmd.getArgument(i);
            if (part != null) data.append(part);
        }

        switch (format) {
            case 'A': {
                String expandedHex = expandAsciiHexWithShorthand(data.toString(), bytesPerRow, heightPx);
                byte[] bytes = hexToBytes(expandedHex);

                if (bytes.length != fieldCount) {
                    throw new IllegalArgumentException("Expanded bytes (" + bytes.length + ") != fieldCount (" + fieldCount + ")");
                }
                // For 'A', many generators set a == b; not enforced here.
                return bytesToImage(bytes, bytesPerRow, widthPx, heightPx);
            }
            case 'B':
                throw new UnsupportedOperationException("^GFB (binary) not supported in this String-based path.");
            case 'C':
                throw new UnsupportedOperationException("^GFC (compressed binary) not implemented here.");
            default:
                throw new IllegalArgumentException("Unsupported ^GF format: " + format);
        }
    }

    /** Draw the decoded ^GF at (x,y) with float magnification (nearest-neighbor). */
    public static void draw(Graphics2D g2d, ZPLCmd cmd, float magnification, int x, int y) {
        BufferedImage img = decodeToImage(cmd);

        // Work on a copy to avoid restoring null hints
        Graphics2D g = (Graphics2D) g2d.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            at.scale(magnification, magnification);
            g.drawImage(img, at, null);
        } finally {
            g.dispose();
        }
    }

    // ---------------- Expansion (ASCII-hex with shorthands) ----------------

    /**
     * Expand ASCII-hex with Zebra/Labelary shorthands into pure hex, row-by-row:
     *  - ':'   => repeat previous row (supports ':::')
     *  - ','   => pad remainder of row with '0'
     *  - '!'   => pad remainder of row with 'F' (or '0' if EXCLAMATION_PADS_BLACK=false)
     *  - G..Z  => 1..20 repeats (uppercase); g..z => 20..400 (steps of 20) — **can chain** then apply to next nibble
     *  - 0..9, A..F, a..f => literal hex nibbles (no numeric run-lengths)
     */
    private static String expandAsciiHexWithShorthand(String data, int bytesPerRow, int rows) {
        final int rowHexLen   = bytesPerRow * 2;
        final int totalHexLen = rowHexLen * rows;

        StringBuilder out = new StringBuilder(totalHexLen);
        String prevRow = "0".repeat(rowHexLen); // if first ':' appears, repeat zeros
        int i = 0;

        for (int r = 0; r < rows; r++) {

            // Handle leading row repeats: ':' (supports :::)
            int j = i, repeatCount = 0;
            while (j < data.length()) {
                char ch = data.charAt(j);
                if (Character.isWhitespace(ch)) { j++; continue; }
                if (ch == ':') { repeatCount++; j++; } else break;
            }
            if (repeatCount > 0) {
                i = j; // consume the colons + whitespace we scanned
                int rowsToEmit = Math.min(repeatCount, rows - r);
                for (int k = 0; k < rowsToEmit; k++) {
                    out.append(prevRow);
                    if (k < rowsToEmit - 1) r++; // advance extra rows
                }
                continue;
            }

            // Build this row
            StringBuilder row = new StringBuilder(rowHexLen);
            while (row.length() < rowHexLen) {
                if (i >= data.length()) { // source exhausted -> pad
                    padRowWithNibble(row, rowHexLen, '0');
                    break;
                }

                char ch = data.charAt(i++);

                // ignore whitespace
                if (Character.isWhitespace(ch)) continue;

                // ',' => pad remainder of row with zeros
                if (ch == ',') {
                    padRowWithNibble(row, rowHexLen, '0');
                    break;
                }

                // '!' => pad remainder of row with 'F' (or '0' if configured)
                if (ch == '!') {
                    padRowWithNibble(row, rowHexLen, EXCLAMATION_PADS_BLACK ? 'F' : '0');
                    break;
                }

                // ':' mid-row: finish row, next loop will treat ':' as a leading repeat
                if (ch == ':') {
                    i--; // rewind so next row sees the ':' sequence
                    padRowWithNibble(row, rowHexLen, '0');
                    break;
                }

                // ---- Accumulate chained letter-repeat tokens, then apply to the next hex nibble
                int lettersCount = letterRepeatValue(ch);
                if (lettersCount > 0) {
                    // Consume further repeat letters (skipping whitespace)
                    while (i < data.length()) {
                        while (i < data.length() && Character.isWhitespace(data.charAt(i))) i++;
                        if (i < data.length()) {
                            int more = letterRepeatValue(data.charAt(i));
                            if (more > 0) { lettersCount += more; i++; continue; }
                        }
                        break;
                    }

                    // Skip whitespace before looking for the nibble to repeat
                    while (i < data.length() && Character.isWhitespace(data.charAt(i))) i++;

                    if (i < data.length() && isHexDigit(data.charAt(i))) {
                        char nibble = data.charAt(i++);
                        appendRepeatingNibbleCapped(row, rowHexLen, nibble, lettersCount);
                    } else {
                        // No nibble follows (e.g., ',', ':', EOF): repeat '0' safely
                        appendRepeatingNibbleCapped(row, rowHexLen, '0', lettersCount);
                    }
                    continue;
                }

                // Literal hex nibble
                if (isHexDigit(ch)) {
                    row.append(ch);
                    continue;
                }

                // Unknown token
                throw new IllegalArgumentException("Unsupported token in ^GFA data: '" + ch + "'");
            }

            // finalize row
            if (row.length() < rowHexLen) padRowWithNibble(row, rowHexLen, '0');
            String rowStr = row.toString();
            out.append(rowStr);
            prevRow = rowStr;
        }

        if (out.length() != totalHexLen) {
            throw new IllegalStateException("Expanded hex length " + out.length() +
                " != expected " + totalHexLen + " (rows=" + rows + ", rowHexLen=" + rowHexLen + ")");
        }
        return out.toString();
    }

    // ---------- helpers ----------

    /** Repeat a nibble without exceeding row length. */
    private static void appendRepeatingNibbleCapped(StringBuilder row, int rowHexLen, char nibble, int count) {
        while (count-- > 0 && row.length() < rowHexLen) {
            row.append(nibble);
        }
    }

    /** Pad the remainder of the row with a nibble ('0' or 'F'). */
    private static void padRowWithNibble(StringBuilder row, int rowHexLen, char nibble) {
        while (row.length() < rowHexLen) row.append(nibble);
    }

    /**
     * Map one repeat letter to its value; supports chaining via summation.
     * Uppercase: G..Z -> 1..20; Lowercase: g..z -> 20..400 (step 20).
     */
    private static int letterRepeatValue(char c) {
        if (c >= 'G' && c <= 'Z') return (c - 'G') + 1;      // 1..20
        if (c >= 'g' && c <= 'z') return (c - 'g' + 1) * 20; // 20..400
        return 0;
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private static int parseInt(String s, String label) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid integer for " + label + ": '" + s + "'");
        }
    }

    private static String safe(String s) { return (s == null) ? "" : s; }

    private static byte[] hexToBytes(String hex) {
        int n = hex.length();
        if ((n & 1) != 0) throw new IllegalArgumentException("Odd number of hex characters");
        byte[] out = new byte[n / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = fromHex(hex.charAt(i * 2));
            int lo = fromHex(hex.charAt(i * 2 + 1));
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }

    private static int fromHex(char c) {
        if (c >= '0' && c <= '9') return (c - '0');
        if (c >= 'A' && c <= 'F') return (c - 'A' + 10);
        if (c >= 'a' && c <= 'f') return (c - 'a' + 10);
        throw new IllegalArgumentException("Non-hex char: " + c);
    }

    private static BufferedImage bytesToImage(byte[] imgBytes, int bytesPerRow, int width, int height) {
        final int WHITE = 0xFFFFFFFF, BLACK = 0xFF000000;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int off = 0;
        for (int y = 0; y < height; y++) {
            for (int b = 0; b < bytesPerRow; b++) {
                int val = imgBytes[off++] & 0xFF;
                for (int bit = 7; bit >= 0; bit--) {
                    int x = (b * 8) + (7 - bit);
                    boolean bitOn = ((val >> bit) & 1) == 1;
                    int argb = ONE_IS_BLACK ? (bitOn ? BLACK : WHITE)
                                            : (bitOn ? WHITE : BLACK);
                    img.setRGB(x, y, argb);
                }
            }
        }
        return img;
    }
}
