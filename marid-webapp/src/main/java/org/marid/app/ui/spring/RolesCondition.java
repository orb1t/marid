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

package org.marid.app.ui.spring;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class RolesCondition implements Condition {
  @Override
  public boolean matches(@NotNull ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    if (securityContext == null) {
      return false;
    }

    final Authentication authentication = securityContext.getAuthentication();
    if (authentication == null) {
      return false;
    }

    final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    if (authorities == null) {
      return false;
    }

    final Map<String, Object> attrs = metadata.getAnnotationAttributes(Roles.class.getName());
    if (attrs == null) {
      return false;
    }

    final Object value = attrs.get("value");
    if (!(value instanceof String[])) {
      return false;
    }

    final String[] roles = (String[]) value;
    if (roles.length == 0) {
      return false;
    }

    return Stream.of(roles).allMatch(role -> authorities.stream().anyMatch(a -> role.equals(a.getAuthority())));
  }
}
