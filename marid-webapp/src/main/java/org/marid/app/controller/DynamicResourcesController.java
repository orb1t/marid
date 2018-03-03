/*-
 * #%L
 * marid-webapp
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

package org.marid.app.controller;

import org.marid.image.MaridIcon;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RequestMapping(path = "/dyn")
@Controller
public class DynamicResourcesController {

  @GetMapping(path = {"/marid-icon.png", "/icon.png"}, produces = {IMAGE_PNG_VALUE})
  @ResponseBody
  public BufferedImage maridIcon(@RequestParam(defaultValue = "64") int size) {
    return MaridIcon.getImage(size, Color.GREEN);
  }
}
