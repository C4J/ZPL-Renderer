package com.commander4j.network;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.commander4j.util.ZPLRender;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Lightweight HTTP server that exposes ZPL rendering as a REST endpoint.
 *
 * Single endpoint:
 *
 * <pre>
 *   POST /render          body = ZPL text (ISO-8859-1)
 *   query parameters (all optional, fall back to GUI defaults):
 *     dpi      printer DPI                     (e.g. 203, 300, 600)
 *     width    label width                     (number)
 *     height   label height                    (number)
 *     uom      "cm" or "inch"
 *     mag      output magnification            (default 1.0 = full DPI)
 *     page     1-indexed page selector         (default 1)
 *     format   "png" | "zip" | "pdf"           (default "png")
 *
 *   Responses:
 *     png  → image/png            single page (404 if page out of range)
 *     zip  → application/zip      page_1.png ... page_N.png
 *     pdf  → application/pdf      one page per label (raster, not vector)
 *
 *   Header X-Total-Pages is set on every successful response.
 * </pre>
 *
 * Designed to be safe for concurrent requests: each request gets its own UUID
 * inside {@link com.commander4j.util.ZPLRender}, so no shared mutable state
 * crosses requests. The defaults supplier is invoked from worker threads — the
 * caller is responsible for whatever marshalling it needs (e.g. EDT).
 */
public class ZPLRestServer
{

	public record Defaults(int dpi, double width, double height, String uom, float magnification)
	{
	}

	private final String bindIp;
	private final int port;
	private final Supplier<Defaults> defaultsSupplier;
	private HttpServer server;

	public ZPLRestServer(String bindIp, int port, Supplier<Defaults> defaultsSupplier)
	{
		this.bindIp = bindIp;
		this.port = port;
		this.defaultsSupplier = defaultsSupplier;
	}

	public void start() throws IOException
	{
		InetAddress addr = InetAddress.getByName(bindIp);
		server = HttpServer.create(new InetSocketAddress(addr, port), 0);
		server.createContext("/render", new RenderHandler());
		server.createContext("/", new RootHandler());
		server.setExecutor(Executors.newFixedThreadPool(4));
		server.start();
		System.out.println("REST server on " + bindIp + ":" + port);
	}

	public void stop()
	{
		if (server != null)
		{
			server.stop(0);
			server = null;
			System.out.println("REST server stopped");
		}
	}

	public boolean isRunning()
	{
		return server != null;
	}

	private class RenderHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange ex) throws IOException
		{
			try
			{
				addCors(ex);
				String method = ex.getRequestMethod();

				if ("OPTIONS".equalsIgnoreCase(method))
				{
					send(ex, 204, null, new byte[0]);
					return;
				}

				Map<String, String> params = parseQuery(ex.getRequestURI().getRawQuery());
				String zpl;

				if ("GET".equalsIgnoreCase(method))
				{
					zpl = params.getOrDefault("zpl", "");
					if (zpl.isEmpty())
					{
						sendText(ex, 400, "GET /render requires a 'zpl' query parameter (URL-encoded ZPL). For larger payloads use POST with the ZPL in the body.");
						return;
					}
				}
				else if ("POST".equalsIgnoreCase(method))
				{
					String contentType = headerValue(ex, "Content-Type");
					byte[] bodyBytes = readAllBytes(ex.getRequestBody());
					if (contentType != null && contentType.toLowerCase().startsWith("application/x-www-form-urlencoded"))
					{
						// Form post — body fields supplement (and override) URL query params.
						Map<String, String> form = parseQuery(new String(bodyBytes, StandardCharsets.UTF_8));
						params.putAll(form);
						zpl = params.getOrDefault("zpl", "");
						if (zpl.isEmpty())
						{
							sendText(ex, 400, "Form POST requires a 'zpl' field.");
							return;
						}
					}
					else
					{
						// Raw body is the ZPL itself.
						zpl = new String(bodyBytes, StandardCharsets.ISO_8859_1);
					}
				}
				else
				{
					sendText(ex, 405, "Use GET /render?zpl=... or POST /render (raw body or form-encoded with a 'zpl' field).");
					return;
				}

				Defaults d = defaultsSupplier.get();
				int dpi = parseInt(params.get("dpi"), d.dpi());
				double width = parseDouble(params.get("width"), d.width());
				double height = parseDouble(params.get("height"), d.height());
				String uom = params.getOrDefault("uom", d.uom());
				float mag = (float) parseDouble(params.get("mag"), d.magnification());
				int page = parseInt(params.get("page"), 1);
				String format = params.getOrDefault("format", "png").toLowerCase();

				List<BufferedImage> pages = ZPLRender.renderToImages(zpl, dpi, width, height, uom, mag);

				if (pages.isEmpty())
				{
					sendText(ex, 422, "No printable labels found in ZPL (need at least one ^XA...^XZ block with a printing command).");
					return;
				}

				ex.getResponseHeaders().set("X-Total-Pages", String.valueOf(pages.size()));

				switch (format)
				{
					case "zip":
						sendZip(ex, pages);
						break;
					case "pdf":
						sendPdf(ex, pages);
						break;
					default:
						sendPng(ex, pages, page);
						break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				sendText(ex, 500, "Render failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
	}

	private class RootHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange ex) throws IOException
		{
			addCors(ex);
			if (!"GET".equalsIgnoreCase(ex.getRequestMethod()))
			{
				sendText(ex, 405, "GET only.");
				return;
			}
			Defaults d = defaultsSupplier.get();
			String html = "<!doctype html>\n"
					+ "<html><head><meta charset='utf-8'><title>ZPL Renderer</title>\n"
					+ "<style>body{font-family:sans-serif;margin:2em;max-width:900px}"
					+ "textarea{width:100%;height:14em;font-family:monospace;font-size:13px}"
					+ "label{display:inline-block;margin-right:1em}"
					+ "input[type=number],input[type=text]{width:5em}"
					+ "fieldset{margin-top:1em}</style></head><body>\n"
					+ "<h2>ZPL Renderer</h2>\n"
					+ "<p>Paste ZPL below and submit. Defaults reflect the GUI's current settings.</p>\n"
					+ "<form method='POST' action='/render' enctype='application/x-www-form-urlencoded' target='_blank'>\n"
					+ "<textarea name='zpl' placeholder='^XA...^XZ' autofocus></textarea>\n"
					+ "<fieldset><legend>Options</legend>\n"
					+ "<label>DPI <input type='number' name='dpi' value='" + d.dpi() + "'></label>\n"
					+ "<label>Width <input type='text' name='width' value='" + d.width() + "'></label>\n"
					+ "<label>Height <input type='text' name='height' value='" + d.height() + "'></label>\n"
					+ "<label>UOM <select name='uom'>"
					+ "<option" + ("cm".equalsIgnoreCase(d.uom()) ? " selected" : "") + ">cm</option>"
					+ "<option" + ("inch".equalsIgnoreCase(d.uom()) ? " selected" : "") + ">inch</option>"
					+ "</select></label>\n"
					+ "<label>Mag <input type='text' name='mag' value='1.0'></label>\n"
					+ "<label>Page <input type='number' name='page' value='1' min='1'></label>\n"
					+ "<label>Format <select name='format'>"
					+ "<option value='png'>png (single page)</option>"
					+ "<option value='zip'>zip (all pages)</option>"
					+ "<option value='pdf'>pdf (all pages)</option>"
					+ "</select></label>\n"
					+ "</fieldset>\n"
					+ "<p><button type='submit'>Render</button></p>\n"
					+ "</form>\n"
					+ "<h3>API</h3>\n"
					+ "<ul>\n"
					+ "<li><code>POST /render</code> — body is the ZPL (or form-encoded with a <code>zpl</code> field).</li>\n"
					+ "<li><code>GET /render?zpl=...</code> — URL-encoded ZPL in the query string.</li>\n"
					+ "<li>Query params: <code>dpi width height uom mag page format</code> (format = png | zip | pdf).</li>\n"
					+ "<li>Response header <code>X-Total-Pages</code> reports total label count.</li>\n"
					+ "</ul>\n"
					+ "</body></html>";
			send(ex, 200, "text/html; charset=UTF-8", html.getBytes(StandardCharsets.UTF_8));
		}
	}

	private void sendPng(HttpExchange ex, List<BufferedImage> pages, int page) throws IOException
	{
		if (page < 1 || page > pages.size())
		{
			sendText(ex, 404, "Page " + page + " not found (total: " + pages.size() + ").");
			return;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(64 * 1024);
		ImageIO.write(pages.get(page - 1), "PNG", baos);
		send(ex, 200, "image/png", baos.toByteArray());
	}

	private void sendZip(HttpExchange ex, List<BufferedImage> pages) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256 * 1024);
		try (ZipOutputStream zos = new ZipOutputStream(baos))
		{
			int idx = 1;
			for (BufferedImage img : pages)
			{
				zos.putNextEntry(new ZipEntry("page_" + idx + ".png"));
				ImageIO.write(img, "PNG", zos);
				zos.closeEntry();
				idx++;
			}
		}
		send(ex, 200, "application/zip", baos.toByteArray());
	}

	private void sendPdf(HttpExchange ex, List<BufferedImage> pages) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256 * 1024);
		try (PDDocument doc = new PDDocument())
		{
			for (BufferedImage img : pages)
			{
				PDPage pdfPage = new PDPage(new PDRectangle(img.getWidth(), img.getHeight()));
				doc.addPage(pdfPage);
				PDImageXObject pdImage = LosslessFactory.createFromImage(doc, img);
				try (PDPageContentStream cs = new PDPageContentStream(doc, pdfPage))
				{
					cs.drawImage(pdImage, 0, 0);
				}
			}
			doc.save(baos);
		}
		send(ex, 200, "application/pdf", baos.toByteArray());
	}

	private static void addCors(HttpExchange ex)
	{
		ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		ex.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
		ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
	}

	private static void sendText(HttpExchange ex, int status, String body) throws IOException
	{
		send(ex, status, "text/plain; charset=UTF-8", body.getBytes(StandardCharsets.UTF_8));
	}

	private static void send(HttpExchange ex, int status, String contentType, byte[] body) throws IOException
	{
		if (contentType != null)
		{
			ex.getResponseHeaders().set("Content-Type", contentType);
		}
		ex.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
		if (body.length > 0)
		{
			try (OutputStream os = ex.getResponseBody())
			{
				os.write(body);
			}
		}
		else
		{
			ex.close();
		}
	}

	private static byte[] readAllBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		byte[] chunk = new byte[8192];
		int n;
		while ((n = in.read(chunk)) != -1)
		{
			buf.write(chunk, 0, n);
		}
		return buf.toByteArray();
	}

	private static String headerValue(HttpExchange ex, String name)
	{
		List<String> v = ex.getRequestHeaders().get(name);
		return (v == null || v.isEmpty()) ? null : v.get(0);
	}

	private static Map<String, String> parseQuery(String raw)
	{
		Map<String, String> out = new LinkedHashMap<>();
		if (raw == null || raw.isEmpty())
		{
			return out;
		}
		for (String pair : raw.split("&"))
		{
			int eq = pair.indexOf('=');
			if (eq <= 0)
			{
				continue;
			}
			String key = pair.substring(0, eq);
			String value = pair.substring(eq + 1);
			try
			{
				out.put(URLDecoder.decode(key, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
			}
			catch (Exception ignore)
			{
				out.put(key, value);
			}
		}
		return out;
	}

	private static int parseInt(String s, int dflt)
	{
		if (s == null || s.isEmpty())
		{
			return dflt;
		}
		try
		{
			return Integer.parseInt(s.trim());
		}
		catch (NumberFormatException e)
		{
			return dflt;
		}
	}

	private static double parseDouble(String s, double dflt)
	{
		if (s == null || s.isEmpty())
		{
			return dflt;
		}
		try
		{
			return Double.parseDouble(s.trim());
		}
		catch (NumberFormatException e)
		{
			return dflt;
		}
	}

}
