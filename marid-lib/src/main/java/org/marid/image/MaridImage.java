/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.marid.image;

import java.awt.BasicStroke;
import static java.awt.BasicStroke.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import static java.awt.RenderingHints.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.*;

/**
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class MaridImage {

    /**
     * Get the Marid image.
     *
     * @param size Image size.
     * @return Image.
     */
    public static BufferedImage getIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        double scale = (double) size / 2.0;
        g.translate(size / 2, size / 2);
        g.scale(scale, scale);
        float[] fs = new float[16];
        Color[] cs = new Color[16];
        for (int i = 0; i < fs.length; i++) {
            fs[i] = (float) i / (float) (fs.length - 1);
        }
        for (int i = 0; i < fs.length; i++) {
            cs[i] = new Color(240 - i * 10, 240 - i * 10, 240);
        }
        g.setPaint(new LinearGradientPaint(0f, -1f, 0f, 0.5f, fs, cs));
        g.fill(new Rectangle2D.Float(-1f, -1f, 2f, 2f));
        AffineTransform tr = g.getTransform();
        g.rotate(1f / 3f, 0.0f, 0.0f);
        g.translate(+0.0f, +0.2f);
        GeneralPath path = new GeneralPath();
        path.moveTo(-0.15f, +0.3f);
        path.lineTo(+0.15f, +0.3f);
        path.quadTo(+0.3f, +0.2f, +0.06f, -0.4f);
        path.quadTo(+0.03f, -0.5f, +0.04f, -0.6f);
        path.lineTo(+0.04f, -0.7f);
        path.lineTo(-0.04f, -0.7f);
        path.lineTo(-0.04f, -0.6f);
        path.quadTo(-0.03f, -0.5f, -0.06f, -0.4f);
        path.quadTo(-0.3f, 0.2f, -0.15f, +0.3f);
        path.closePath();
        g.setStroke(new BasicStroke(0.05f, CAP_BUTT, JOIN_BEVEL));
        for (int i = 0; i < fs.length; i++) {
            cs[i] = new Color(255 - i * 4, 255 - i * 4, 255);
        }
        g.setPaint(new RadialGradientPaint(0f, 0f, 0.5f, fs, cs));
        g.fill(path);
        g.setColor(new Color(240, 240, 255));
        g.draw(path);
        g.setTransform(tr);
        path = new GeneralPath();
        path.moveTo(-1.0f, +0.0f);
        path.quadTo(-0.8f, +0.1f, -0.6f, +0.0f);
        path.quadTo(-0.4f, -0.1f, -0.2f, +0.0f);
        path.quadTo(+0.0f, +0.1f, +0.2f, +0.0f);
        path.quadTo(+0.4f, -0.1f, +0.6f, +0.0f);
        path.quadTo(+0.8f, +0.1f, +1.0f, +0.0f);
        path.lineTo(+1.0f, +1.0f);
        path.lineTo(-1.0f, +1.0f);
        path.closePath();
        for (int i = 0; i < fs.length; i++) {
            cs[i] = new Color(0, 0, 170 - i * 11, 100 + i * 9);
        }
        g.setPaint(new LinearGradientPaint(0f, 0f, 0f, 1f, fs, cs));
        g.fill(path);
        return img;
    }
}
