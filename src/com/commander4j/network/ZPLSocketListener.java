package com.commander4j.network;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;


public class ZPLSocketListener implements Runnable {
    private final int port;
    private final ZPLDataCallback callback;
    private final String ipAddress;

    private volatile boolean running = true;
    private volatile ServerSocket server;
    private volatile Socket client;

    public ZPLSocketListener(int port, ZPLDataCallback callback, String ipAddress) {
        this.port = port;
        this.callback = callback;
        this.ipAddress = ipAddress;
    }

    @Override public void run() {
        try (ServerSocket ss = new ServerSocket()) {
            this.server = ss;
            ss.setReuseAddress(true);
            InetAddress bindAddr = InetAddress.getByName(ipAddress);
            ss.bind(new InetSocketAddress(bindAddr, port));
            ss.setSoTimeout(1000);
            System.out.println("ZPL listener on " + ipAddress + ":" + port);

            while (running) {
                try {
                    Socket s;
                    try {
                        s = ss.accept();
                    } catch (SocketTimeoutException to) {
                        continue;
                    }
                    this.client = s;
                    try {
                        handleClient(s);
                    } finally {
                        safeClose(s);
                        this.client = null;
                    }
                } catch (SocketException se) {
                    if (running) se.printStackTrace();
                } catch (IOException ioe) {
                    if (running) ioe.printStackTrace();
                }
            }
        } catch (IOException ioe) {
            if (running) ioe.printStackTrace();
        } finally {
            safeClose(server);
            server = null;
        }
        System.out.println("ZPL listener stopped");
    }

    public void stop() {
        running = false;
        safeClose(server);
        safeClose(client);
    }

    private void handleClient(Socket s) throws IOException {
        s.setSoTimeout(500);
        try (BufferedInputStream in = new BufferedInputStream(s.getInputStream())) {
            StringBuilder buf = new StringBuilder(16 * 1024);
            byte[] tmp = new byte[4096];
            while (running) {
                int n;
                try {
                    n = in.read(tmp);
                } catch (SocketTimeoutException to) {
                    continue;
                }
                if (n == -1) break;
                String chunk = new String(tmp, 0, n, StandardCharsets.ISO_8859_1);
                buf.append(chunk);
                extractBlocks(buf);
            }
        }
    }

    private void extractBlocks(StringBuilder buf) {
        int searchFrom = 0;
        while (true) {
            int start = indexOf(buf, "^XA", searchFrom);
            if (start < 0) {
                if (buf.length() > 65536) buf.delete(0, Math.max(0, buf.length() - 8192));
                return;
            }
            int end = indexOf(buf, "^XZ", start + 3);
            if (end < 0) {
                if (start > 0) buf.delete(0, start);
                return;
            }
            int endInclusive = end + 3;
            String zplBlock = buf.substring(start, endInclusive);
            buf.delete(0, endInclusive);
            try { callback.onLabel(zplBlock); } catch (Throwable t) { t.printStackTrace(); }
            searchFrom = 0;
        }
    }

    private static int indexOf(StringBuilder sb, String needle, int fromIndex) {
        int nlen = needle.length();
        int max = sb.length() - nlen;
        outer: for (int i = Math.max(0, fromIndex); i <= max; i++) {
            for (int j = 0; j < nlen; j++) if (sb.charAt(i + j) != needle.charAt(j)) continue outer;
            return i;
        }
        return -1;
    }

    private static void safeClose(ServerSocket s) {
        if (s != null) try { s.close(); } catch (IOException ignore) {}
    }
    private static void safeClose(Socket s) {
        if (s != null) try { s.close(); } catch (IOException ignore) {}
    }
}

