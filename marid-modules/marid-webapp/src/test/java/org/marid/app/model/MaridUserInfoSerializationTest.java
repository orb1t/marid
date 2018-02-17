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

package org.marid.app.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.json.BasicJsonParser;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.marid.test.TestGroups.NORMAL;
import static org.testng.Assert.assertEquals;

public class MaridUserInfoSerializationTest {

  @Test(groups = {NORMAL})
  public void testSerialization() throws Exception {
    final MaridUserInfo user = new MaridUserInfo("y", true, "2011-01-01", "USER");

    final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    final String jackson = mapper.writeValueAsString(user);

    final BasicJsonParser basicJsonParser = new BasicJsonParser();
    final Map<String, Object> map = basicJsonParser.parseMap(jackson);

    assertEquals(map.get("password"), "y");
    assertEquals(map.get("expirationDate"), "2011-01-01");
    assertEquals(map.get("authorities"), List.of("USER"));
    assertEquals(map.get("enabled"), "true");
  }

  @Test(groups = {NORMAL})
  void testDeserialization() throws Exception {
    final MaridUserInfo user = new MaridUserInfo("y", true, "2011-01-01", "USER");

    final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    final String jackson = mapper.writeValueAsString(user);

    final MaridUserInfo cloned = mapper.readValue(jackson, MaridUserInfo.class);

    assertEquals(cloned.password, "y");
    assertEquals(cloned.expirationDate, "2011-01-01");
    assertEquals(cloned.authorities, Set.of("USER"));
    assertEquals(cloned.enabled, true);
  }
}
