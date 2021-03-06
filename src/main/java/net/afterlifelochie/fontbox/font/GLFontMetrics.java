package net.afterlifelochie.fontbox.font;

import net.afterlifelochie.fontbox.api.exception.FontException;
import net.afterlifelochie.fontbox.api.font.IGLFontMetrics;
import net.afterlifelochie.fontbox.api.font.IGLGlyphMetric;
import net.afterlifelochie.fontbox.api.tracer.ITracer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A font metric digest. Contains information on the font, such as the
 * orientation and size of each glyph supported, the texture coordinate for each
 * glyph and the resource mappings to the font image file.
 *
 * @author AfterLifeLochie
 */
public class GLFontMetrics implements IGLFontMetrics {
    /**
     * Derive a font metric from a font file, a font render context and the
     * layout properties specified.
     *
     * @param trace           The debugging tracer object
     * @param font            The Font to read from
     * @param ctx             The rendering context
     * @param fontImageWidth  The font image width
     * @param fontImageHeight The font image height
     * @param charsPerRow     The number of characters per row on the image
     * @param minChar         The starting character
     * @param maxChar         The ending character
     * @return A GLFontMetrics object which appropriates the location of all
     * fonts on the buffer, based on the parameters provided.
     * @throws FontException Any exception which is raised by producing invalid metrics
     *                       information.
     */
    public static GLFontMetrics fromFontMetrics(ITracer trace, Font font, FontRenderContext ctx, int fontImageWidth,
                                                int fontImageHeight, int charsPerRow, char minChar, char maxChar) throws FontException {
        if (trace == null)
            throw new IllegalArgumentException("trace may not be null");
        if (font == null)
            throw new IllegalArgumentException("font may not be null");
        if (ctx == null)
            throw new IllegalArgumentException("ctx may not be null");
        int off = 0;
        GLFontMetrics metric = new GLFontMetrics(fontImageWidth, fontImageHeight);
        for (char k = minChar; k <= maxChar; k++, off++) {
            TextLayout layout = new TextLayout(String.valueOf(k), font, ctx);
            Rectangle2D rect = layout.getBounds();
            int x = (off % charsPerRow) * (fontImageWidth / charsPerRow);
            int y = (off / charsPerRow) * (fontImageWidth / charsPerRow);

            float cy = (float) rect.getHeight();
            Rectangle rect0 = layout.getPixelBounds(null, 100, 100);
            float cx = -(rect0.x - 100);

            int u = (int) Math.ceil(rect.getWidth() + cx);
            int v = (int) Math.ceil(layout.getAscent() + layout.getDescent());
            trace.trace("GLFontMetrics.fromFontMetrics", "placeGlyph", k, u, v, x - cx, y - cy);
            metric.glyphs.put((int) k,
                new GLGlyphMetric(u, v, (int) layout.getAscent(), (int) (x - cx), (int) (y - cy)));
        }
        trace.trace("GLFontMetrics.fromFontMetrics", metric);
        return metric;
    }

    /**
     * Derive a font metric from an XML document path and the layout properties
     * specified.
     *
     * @param trace           The debugging tracer object
     * @param fontMetricName  The path to the XML metrics document
     * @param fontImageWidth  The font image width
     * @param fontImageHeight The font image height
     * @return A GLFontMetrics object which appropriates the location of all
     * fonts on the buffer, based on the parameters provided.
     * @throws FontException Any exception which is raised by producing invalid metrics
     *                       information.
     */
    public static GLFontMetrics fromResource(ITracer trace, ResourceLocation fontMetricName, int fontImageWidth,
                                             int fontImageHeight) throws FontException {
        if (trace == null)
            throw new IllegalArgumentException("trace may not be null");
        if (fontMetricName == null)
            throw new IllegalArgumentException("fontMetricName may not be null");
        try {
            IResource metricResource = Minecraft.getMinecraft().getResourceManager().getResource(fontMetricName);
            InputStream stream = metricResource.getInputStream();
            if (stream == null)
                throw new IOException("Could not open font metric file.");

            GLFontMetrics metric = new GLFontMetrics(fontImageWidth, fontImageHeight);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(stream);
            Element metrics = doc.getDocumentElement();

            NodeList list_character = metrics.getElementsByTagName("character");
            for (int i = 0; i < list_character.getLength(); i++) {
                Element character = (Element) list_character.item(i);
                int c = Integer.parseInt(character.getAttributes().getNamedItem("key").getNodeValue());
                if (0 > c || c > 255)
                    throw new FontException(String.format("Unsupported character code %s", c));
                int w = -1, h = -1, u = -1, v = -1;
                NodeList character_properties = character.getChildNodes();
                for (int k = 0; k < character_properties.getLength(); k++) {
                    Node property = character_properties.item(k);
                    if (!(property instanceof Element))
                        continue;
                    Element elem = (Element) property;
                    String name = elem.getNodeName().toLowerCase();
                    int val = Integer.parseInt(elem.getFirstChild().getNodeValue());
                    switch (name) {
                        case "width":
                            w = val;
                            break;
                        case "height":
                            h = val;
                            break;
                        case "x":
                            u = val;
                            break;
                        case "y":
                            v = val;
                            break;
                        default:
                            throw new FontException(String.format("Unexpected metric command %s", name));
                    }
                }
                if (w == -1 || h == -1 || u == -1 || v == -1)
                    throw new FontException(String.format("Invalid metric properties set for key %s", c));
                trace.trace("GLFontMetrics.fromResource", "placeGlyph", (char) c, w, h, u, v);
                metric.glyphs.put(c, new GLGlyphMetric(w, h, 0, u, v));
            }
            trace.trace("GLFontMetrics.fromResource", metric);
            return metric;
        } catch (IOException e) {
            throw new FontException("Cannot setup font.", e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new FontException("Cannot read font metric data.", e);
        }
    }


    private final Map<Integer, IGLGlyphMetric> glyphs = new HashMap<>();
    private final float fontImageWidth, fontImageHeight;

    private GLFontMetrics(int fontImageWidth, int fontImageHeight) {
        this.fontImageWidth = fontImageWidth;
        this.fontImageHeight = fontImageHeight;
    }

    @Override
    public Map<Integer, IGLGlyphMetric> getGlyphs() {
        return glyphs;
    }

    @Override
    public float getFontImageWidth() {
        return fontImageWidth;
    }

    @Override
    public float getFontImageHeight() {
        return fontImageHeight;
    }

    @Override
    public String toString() {
        return "GLFontMetrics { hash: " + System.identityHashCode(this) + ", w: " + fontImageWidth + ", h: "
            + fontImageHeight + " }";
    }
}
