/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * Marid icon.
 *
 * @author Dmitry Ovchinnikov
 */
public class MaridIcon {

    public static BufferedImage getImage(int size, Color color) {
        final BufferedImage img = new BufferedImage(size, size, TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final float[] fs = new float[16];
        final Color[] cs = new Color[16];
        for (int i = 0; i < fs.length; i++) {
            fs[i] = (i + 1) / (float)fs.length;
            cs[i] = new Color(150 - i * 10, 150 - i * 10, 255 - i * 10);
        }
        g.setPaint(new LinearGradientPaint(0.0f, 0.0f, 0.0f, size - 1.0f, fs, cs));
        g.fillRect(0, 0, size, size);
        g.scale(size / 2.0, -size / 2.0);
        final BasicStroke stroke = new BasicStroke(0.15f);
        g.translate(1.0, -1.0);
        g.setStroke(stroke);
        g.scale(2.0 / (2.0 + stroke.getLineWidth()), 2.0 / (2.0 + stroke.getLineWidth()));
        g.setColor(Color.WHITE);
        final GeneralPath onda1 = new GeneralPath();
        onda1.moveTo(-1.1f, +0.1f);
        onda1.quadTo(-0.8f, +0.3f, -0.4f, +0.15f);
        g.draw(onda1);
        final GeneralPath onda2 = new GeneralPath();
        onda2.moveTo(+1.1f, +0.1f);
        onda2.quadTo(+0.8f, +0.3f, +0.4f, +0.15f);
        g.draw(onda2);
        for (int i = 0; i < fs.length; i++) {
            cs[i] = new Color(255, 255, 255, 255 - i * 15);
        }
        final GeneralPath botella = new GeneralPath();
        botella.moveTo(-0.5f, -1.0f);
        botella.curveTo(-0.6f, +0.0f, -0.1f, +0.3f, -0.1f, +1.0f);
        botella.lineTo(+0.1f, +1.0f);
        botella.curveTo(+0.1f, +0.3f, +0.6f, +0.0f, +0.5f, -1.0f);
        botella.closePath();
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 210));
        g.fill(botella);
        g.setPaint(new LinearGradientPaint(+0.0f, +1.0f, +0.0f, -1.0f, fs, cs));
        g.fill(botella);
        g.setColor(Color.WHITE);
        g.draw(botella);
        return img;
    }

    public static ImageIcon getIcon(int size, Color color) {
        return new ImageIcon(getImage(size, color));
    }

    public static void main(String... args) throws Exception {
        final int size = args.length < 1 ? 32 : Integer.parseInt(args[0]);
        final Color color = args.length < 2 || "-".equals(args[1]) ? Color.GREEN : Color.decode(args[1]);
        final String format = args.length < 3 ? "PNG" : args[2];
        final BufferedImage image = getImage(size, color);
        final File file = new File("marid." + format.toLowerCase());
        ImageIO.write(image, format, file);
        System.out.println(file.getAbsolutePath() + " " + file.exists());
    }
}
