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

package org.marid.app.config;

import j2html.TagCreator;
import j2html.tags.Tag;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.marid.app.ui.main.MainConfiguration;
import org.marid.app.ui.users.UsersConfiguration;
import org.marid.rwt.spring.ApplicationConfigurer;
import org.marid.rwt.spring.EndPoint;
import org.marid.rwt.spring.EndPointConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import static j2html.TagCreator.join;
import static org.eclipse.rap.rwt.client.WebClient.HEAD_HTML;

@Configuration
public class EndPointConfiguration {

  @Bean
  public EndPoint mainEndPoint() {
    return new EndPoint("/menu.marid", MainConfiguration.class);
  }

  @Bean
  public EndPoint usersEndPoint() {
    return new EndPoint("/users.marid", UsersConfiguration.class);
  }

  @Qualifier("head")
  @Bean
  public Tag<?> favIconTag() {
    return TagCreator.link().withRel("icon").withType("image/png").withHref("/dyn/marid-icon.png?size=24");
  }

  @Qualifier("head")
  @Bean
  public Tag<?> robotoFontTag() {
    return TagCreator.link().withRel("stylesheet").withHref("https://fonts.googleapis.com/css?family=Roboto");
  }

  @Bean
  public EndPointConfigurer headEndPointConfigurer(@Qualifier("head") Tag<?>[] tags) {
    return (beanName, endPoint) -> endPoint.put(HEAD_HTML, () -> join((Object[]) tags).render());
  }

  @Bean
  public ApplicationConfigurer maridApplicationConfigurer(GenericApplicationContext context) {
    final Logger logger = LoggerFactory.getLogger(context.getId());
    return application -> {
      application.setExceptionHandler(throwable -> {
        logger.error("Unknown exception", throwable);
        final Display display = Display.getCurrent();
        if (display != null) {
          final Shell shell = display.getActiveShell();
          if (shell != null) {
            final MessageDialog messageDialog = new MessageDialog(
                shell,
                "Error",
                null,
                throwable.getMessage(),
                MessageDialog.ERROR,
                new String[] {IDialogConstants.get().OK_LABEL},
                0
            );
            messageDialog.setBlockOnOpen(false);
            messageDialog.open();
          }
        }
      });
      application.setAttribute("MARID_NAME", context.getId());
    };
  }
}
