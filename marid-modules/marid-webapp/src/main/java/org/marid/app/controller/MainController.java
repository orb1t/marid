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

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

  @GetMapping(path = "/login")
  public String login() {
    return "login";
  }

  @GetMapping(path = {"/", "/index"})
  public String index() {
    return "index";
  }

  @Secured({"ROLE_USER"})
  @GetMapping(path = {"/cellars"})
  public String cellars() {
    return "cellars";
  }

  @Secured({"ROLE_USER"})
  @GetMapping(path = {"/racks"})
  public String racks() {
    return "racks";
  }

  @Secured({"ROLE_ADMIN"})
  @GetMapping(path = {"/admin"})
  public String admin() {
    return "admin";
  }
}
