/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
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
