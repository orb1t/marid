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

package org.marid.app.ui.users;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.marid.app.dao.UserDao;
import org.marid.app.model.MaridUser;
import org.marid.common.app.l10n.LCommon;
import org.marid.rwt.spring.UIContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;

import java.util.stream.IntStream;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.FILL_BOTH;
import static org.marid.misc.Builder.build;

@Configuration
@ComponentScan
public class UsersConfiguration {

  @Bean
  public SashForm splitter(UIContext context) {
    final SashForm form = new SashForm(context.getShell(), VERTICAL);
    form.setLayoutData(new GridData(FILL_BOTH));
    return form;
  }

  @Bean
  @Order(2)
  public Composite buttons(SashForm form,
                           UsersTable table,
                           UserDao dao,
                           ObjectProvider<AddUserDialog> dialogProvider,
                           Authentication authentication) {
    final Composite buttons = new Composite(form, BORDER);

    buttons.setLayout(build(new RowLayout(), l -> {
      l.spacing = 10;
      l.justify = true;
    }));

    final Button remove = new Button(buttons, SWT.PUSH);
    remove.setText("[-] " + LCommon.get().remove);
    remove.addListener(Selection, event -> {
      final int[] toDelete = IntStream.of(table.getSelectionIndices())
          .filter(i -> {
            final TableItem item = table.getItem(i);
            final MaridUser user = (MaridUser) item.getData();
            return !authentication.getName().equals(user.getUsername());
          })
          .peek(i -> {
            final TableItem item = table.getItem(i);
            final MaridUser user = (MaridUser) item.getData();
            dao.removeUser(user.getUsername());
          })
          .toArray();
      table.remove(toDelete);
    });

    final Button add = new Button(buttons, SWT.PUSH);
    add.setText("[+]" + LCommon.get().add);
    add.addListener(Selection, event -> {
      final AddUserDialog dialog = dialogProvider.getObject();
      dialog.open();
    });

    return buttons;
  }
}
