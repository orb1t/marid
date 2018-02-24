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

package org.marid.common.app.endpoint;

import org.eclipse.rap.rwt.client.WebClient;
import org.intellij.lang.annotations.MagicConstant;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Supplier;

public class EndPoint {

  private final String path;
  private final Class<?> configurationClass;
  private final Map<String, String> parameters = new TreeMap<>();

  public EndPoint(String path, Class<?> configurationClass) {
    this.path = path;
    this.configurationClass = configurationClass;
  }

  public EndPoint parameter(String key, String value) {
    parameters.put(key, value);
    return this;
  }

  public EndPoint clientProperty(@MagicConstant(valuesFromClass = WebClient.class) String key, String value) {
    parameters.put(key, value);
    return this;
  }

  public EndPoint parameters(Map<String, String> map) {
    parameters.putAll(map);
    return this;
  }

  public EndPoint parameters(Properties properties) {
    properties.stringPropertyNames().forEach(k -> parameters.put(k, properties.getProperty(k)));
    return this;
  }

  public EndPoint put(@MagicConstant(valuesFromClass = WebClient.class) String key, Supplier<String> valueSupplier) {
    parameters.computeIfAbsent(key, k -> valueSupplier.get());
    return this;
  }

  public String getPath() {
    return path;
  }

  public Class<?> getConfigurationClass() {
    return configurationClass;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }
}
