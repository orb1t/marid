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

package org.marid.app.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

  @Bean
  public JsonComponentModule module() {
    return new JsonComponentModule();
  }

  @Bean
  public JsonFactory jsonFactory() {
    return new MappingJsonFactory()
        .configure(JsonParser.Feature.ALLOW_MISSING_VALUES, true)
        .configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
  }

  @Bean
  public ObjectMapper mapper(JsonFactory jsonFactory, JsonComponentModule module) {
    return new ObjectMapper(jsonFactory)
        .findAndRegisterModules()
        .registerModule(module)
        .enable(SerializationFeature.INDENT_OUTPUT);
  }
}
