package com.commander4j.font;

public class ZPLOffsets {
    public final int offsetLeftPx; // add to x
    public final int offsetTopPx;  // add to y
    public final int rightPadPx;   // optional info
    public final int bottomPadPx;  // optional info
    public ZPLOffsets(int left, int top, int right, int bottom) {
        this.offsetLeftPx = left;
        this.offsetTopPx  = top;
        this.rightPadPx   = right;
        this.bottomPadPx  = bottom;
    }
}