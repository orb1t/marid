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

import com.google.gson.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

import java.util.List;

@Configuration
public class GsonConfiguration {

  @Bean
  public Gson gson(List<TypeAdapter<?>> typeAdapters) {
    final GsonBuilder builder = new GsonBuilder()
        .setPrettyPrinting()
        .setVersion(1.0)
        .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
        .setLongSerializationPolicy(LongSerializationPolicy.DEFAULT);

    typeAdapters.forEach(adapter -> {
      final ResolvableType type = ResolvableType.forClass(TypeAdapter.class, adapter.getClass());
      builder.registerTypeAdapter(type.getGeneric(0).getType(), adapter);
    });

    return builder.create();
  }
}
