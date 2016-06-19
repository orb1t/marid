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
 * @author Dmitry Ovchinnikov
 */
public class MaridIcon {

    private static final int FRACTION_COUNT = 16;
    private static final float[] FRACTIONS = new float[FRACTION_COUNT];
    private static final Color[] COLORS = new Color[FRACTION_COUNT];
    private static final Color[] BCOLORS = new Color[FRACTION_COUNT];
    private static final GeneralPath ONDA1 = new GeneralPath();
    private static final GeneralPath ONDA2 = new GeneralPath();
    private static final GeneralPath BOTELLA = new GeneralPath();
    private static final BasicStroke STROKE = new BasicStroke(0.15f);
    private static final double SCALE_X = 2.0 / (2.0 + STROKE.getLineWidth());
    private static final double SCALE_Y = 2.0 / (2.0 + STROKE.getLineWidth());
    private static final LinearGradientPaint BOTELLA_GRADIENT;

    static {
        for (int i = 0; i < FRACTION_COUNT; i++) {
            FRACTIONS[i] = (i + 1) / (float) FRACTION_COUNT;
            COLORS[i] = new Color(150 - i * 10, 150 - i * 10, 255 - i * 10);
            BCOLORS[i] = new Color(255, 255, 255, 255 - i * 15);
        }
        ONDA1.moveTo(-1.1f, +0.1f);
        ONDA1.quadTo(-0.8f, +0.3f, -0.475f, +0.17f);
        ONDA2.moveTo(+1.1f, +0.1f);
        ONDA2.quadTo(+0.8f, +0.3f, +0.475f, +0.17f);
        BOTELLA.moveTo(-0.5f, -1.0f);
        BOTELLA.curveTo(-0.6f, +0.0f, -0.1f, +0.3f, -0.1f, +1.0f);
        BOTELLA.lineTo(+0.1f, +1.0f);
        BOTELLA.curveTo(+0.1f, +0.3f, +0.6f, +0.0f, +0.5f, -1.0f);
        BOTELLA.closePath();
        BOTELLA_GRADIENT = new LinearGradientPaint(+0.0f, +1.0f, +0.0f, -1.0f, FRACTIONS, BCOLORS);
    }

    public static void draw(int w, int h, Color color, Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new LinearGradientPaint(0, 0, 0, h, FRACTIONS, COLORS));
        g.fillRect(0, 0, w, h);
        final int size = Math.min(w, h);
        g.translate((w - size) / 2, (h - size) / 2);
        final double s = size / 2.0;
        g.scale(s, -s);
        g.translate(1.0, -1.0);
        g.setStroke(STROKE);
        g.scale(SCALE_X, SCALE_Y);
        g.setColor(Color.WHITE);
        g.draw(ONDA1);
        g.draw(ONDA2);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 210));
        g.fill(BOTELLA);
        g.setPaint(BOTELLA_GRADIENT);
        g.fill(BOTELLA);
        g.setColor(Color.WHITE);
        g.draw(BOTELLA);
    }

    public static BufferedImage getImage(int size, Color color) {
        final BufferedImage img = new BufferedImage(size, size, TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        draw(size, size, color, g);
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
