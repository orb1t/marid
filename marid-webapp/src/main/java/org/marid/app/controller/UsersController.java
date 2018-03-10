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

import org.marid.app.dao.UserDao;
import org.marid.app.model.MaridUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(path = "/users")
public class UsersController {

  private final UserDao userDao;

  public UsersController(UserDao userDao) {
    this.userDao = userDao;
  }

  @GetMapping(path = "/users.html")
  public String users() {
    return "users/users";
  }

  @GetMapping(path = "/user.html")
  public String user(@RequestParam String name, Model model) {
    model.addAttribute("user", userDao.loadUserByUsername(name));
    return "users/user";
  }

  @GetMapping(path = "/names")
  @ResponseBody
  public String[] usersData() {
    return userDao.getUsers().stream().map(MaridUser::getUsername).toArray(String[]::new);
  }

  @ModelAttribute(name = "dao")
  public UserDao usersDao() {
    return userDao;
  }
}
