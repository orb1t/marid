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

package org.marid.site.controller;

import org.marid.image.MaridIcon;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

@Controller
public class DynamicResourcesController {

  @RequestMapping(path = "/dyn/marid-icon.png", method = RequestMethod.GET)
  public void maridIcon(HttpServletResponse response, @RequestParam(defaultValue = "64") int size) throws IOException {
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("image/png");

    final BufferedImage icon = MaridIcon.getImage(size, Color.GREEN);
    try (final OutputStream outputStream = response.getOutputStream()) {
      ImageIO.write(icon, "PNG", outputStream);
    }
  }
}
