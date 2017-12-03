/*-
 * #%L
 * marid-test
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
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

package org.marid.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Parameter;

import static org.mockito.Mockito.mock;

public class MockitoExtension implements TestInstancePostProcessor, ParameterResolver {

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
    MockitoAnnotations.initMocks(testInstance);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.getParameter().isAnnotationPresent(Mock.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return getMock(parameterContext.getParameter(), extensionContext);
  }

  private Object getMock(Parameter parameter, ExtensionContext extensionContext) {
    final Class<?> mockType = parameter.getType();
    final Store mocks = extensionContext.getStore(Namespace.create(MockitoExtension.class, mockType));
    final String mockName = getMockName(parameter);

    return mockName != null
        ? mocks.getOrComputeIfAbsent(mockName, key -> mock(mockType, mockName))
        : mocks.getOrComputeIfAbsent(mockType.getCanonicalName(), key -> mock(mockType));
  }

  private String getMockName(Parameter parameter) {
    final String explicitMockName = parameter.getAnnotation(Mock.class).name().trim();
    return !explicitMockName.isEmpty() ? explicitMockName : parameter.isNamePresent() ? parameter.getName() : null;
  }
}