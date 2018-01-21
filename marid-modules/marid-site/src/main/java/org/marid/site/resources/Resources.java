/*-
 * #%L
 * marid-site
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.site.resources;

import org.marid.image.MaridIcon;
import org.marid.site.annotation.DynRes;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

@ApplicationScoped
public class Resources {

  @DynRes("marid-icon.png")
  @Produces
  @Dependent
  public DynResource maridIcon() {
    return (req, resp) -> {
      final BufferedImage icon = MaridIcon.getImage(64, Color.GREEN);

      resp.setContentType("image/png");
      try (final OutputStream outputStream = resp.getOutputStream()) {
        ImageIO.write(icon, "PNG", outputStream);
      }
    };
  }
}
