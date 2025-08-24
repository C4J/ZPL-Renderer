package com.commander4j.network;

@FunctionalInterface
public interface ZPLDataCallback {
    /** Called once per complete label (^XA ... ^XZ) detected by the socket thread. */
    void onLabel(String zplBlock);
}
