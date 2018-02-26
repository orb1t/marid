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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.marid.app.dao.UserDao;
import org.marid.app.model.MaridUser;
import org.marid.app.model.MaridUserInfo;
import org.marid.common.app.l10n.LCommon;
import org.marid.common.app.l10n.LUsers;
import org.marid.rwt.spring.UIContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.FILL_BOTH;
import static org.eclipse.swt.layout.GridData.FILL_HORIZONTAL;
import static org.marid.misc.Builder.build;

@Configuration
public class UsersConfiguration {

  @Bean
  public SashForm splitter(UIContext context) {
    final SashForm form = new SashForm(context.getShell(), VERTICAL);
    form.setLayoutData(new GridData(FILL_BOTH));
    return form;
  }

  @Bean
  @Order(1)
  public Table userTable(SashForm form, UserDao dao) {
    final Table table = new Table(form, V_SCROLL | H_SCROLL | BORDER | MULTI) {
    };

    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    final TableColumn nameColumn = new TableColumn(table, NONE);
    nameColumn.setText(LUsers.get().name);
    nameColumn.setResizable(true);
    nameColumn.setAlignment(LEFT);

    final TableColumn expirationDateColumn = new TableColumn(table, NONE);
    expirationDateColumn.setText(LCommon.get().date);
    expirationDateColumn.setResizable(true);
    expirationDateColumn.setAlignment(CENTER);
    expirationDateColumn.setWidth(DEFAULT);

    final TableColumn adminColumn = new TableColumn(table, NONE);
    adminColumn.setText(LUsers.get().admin);
    adminColumn.setResizable(true);
    adminColumn.setAlignment(CENTER);

    final TableColumn userColumn = new TableColumn(table, NONE);
    userColumn.setText(LUsers.get().user);
    userColumn.setResizable(true);
    userColumn.setAlignment(CENTER);

    for (final MaridUser user: dao.getUsers()) {
      final TableItem item = new TableItem(table, NONE);
      item.setData(user);
      item.setText(0, user.getUsername());
      item.setText(1, user.getExpirationDate().toString());
      item.setText(2, user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ? "\u25CF" : "");
      item.setText(3, user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")) ? "\u25CF" : "");
    }

    for (final TableColumn column : table.getColumns()) {
      column.pack();
      column.setWidth(column.getWidth() + 16);
    }

    return table;
  }

  @Bean
  @Order(2)
  public Composite buttons(SashForm form, Table table, UserDao dao, Authentication authentication) {
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
      final Dialog dialog = new Dialog(table.getShell(), APPLICATION_MODAL) {

        @Override
        protected void prepareOpen() {
          shell = new Shell(getParent(), TITLE | CLOSE | BORDER | APPLICATION_MODAL | V_SCROLL);
          shell.setText("Add User");
          shell.setLayout(build(new GridLayout(2, false), l -> {
            l.verticalSpacing = 10;
            l.horizontalSpacing = 10;
          }));

          final Label nameLabel = new Label(shell, NONE);
          nameLabel.setText(LCommon.get().name);

          final Text nameField = new Text(shell, BORDER);
          nameField.setLayoutData(new GridData(FILL_HORIZONTAL));

          final Label expirationDateLabel = new Label(shell, NONE);
          expirationDateLabel.setText(LCommon.get().expirationDate);

          final DateTime expirationDateField = new DateTime(shell, BORDER | DATE);
          final ZonedDateTime date = Instant.now().atZone(ZoneId.systemDefault()).plus(1L, ChronoUnit.YEARS);
          expirationDateField.setDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());

          final Label passwordLabel = new Label(shell, NONE);
          passwordLabel.setText(LCommon.get().password);

          final Text passwordField = new Text(shell, BORDER | PASSWORD);
          passwordField.setLayoutData(new GridData(FILL_HORIZONTAL));

          final Label adminLabel = new Label(shell, NONE);
          adminLabel.setText(LUsers.get().admin);

          final Button adminButton = new Button(shell, CHECK);
          adminButton.setSelection(false);

          final Label userLabel = new Label(shell, NONE);
          userLabel.setText(LUsers.get().user);

          final Button userButton = new Button(shell, CHECK);
          userButton.setSelection(true);

          final Composite buttonsArea = new Composite(shell, NONE);
          buttonsArea.setLayoutData(build(new GridData(FILL_HORIZONTAL), l -> l.horizontalSpan = 2));
          buttonsArea.setLayout(build(new RowLayout(), l -> l.center = true));

          final Button add = new Button(buttonsArea, PUSH);
          add.setText("Add");
          add.addListener(Selection, e -> {
            final Set<String> authorities = new HashSet<>();
            if (adminButton.getSelection()) {
              authorities.add("ROLE_ADMIN");
            }
            if (userButton.getSelection()) {
              authorities.add("ROLE_USER");
            }
            final MaridUser user = new MaridUser(nameField.getText(), new MaridUserInfo(
                passwordField.getText(),
                true,
                String.format("%04d-%02d-%02d", expirationDateField.getYear(), expirationDateField.getMonth(), expirationDateField.getDay()),
                authorities.toArray(new String[authorities.size()])
            ));
            dao.saveUser(user);
            shell.close();

            final TableItem item = new TableItem(table, NONE);
            item.setData(user);
            item.setText(0, user.getUsername());
            item.setText(1, user.getExpirationDate().toString());
            item.setText(2, user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ? "\u25CF" : "");
            item.setText(3, user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")) ? "\u25CF" : "");

            for (final TableColumn column : table.getColumns()) {
              column.pack();
              column.setWidth(column.getWidth() + 16);
            }
          });

          shell.setBounds(computeShellBounds());
          shell.layout();
        }

        private Rectangle computeShellBounds() {
          final Rectangle d = getParent().getDisplay().getBounds();
          final Point preferredSize = shell.computeSize((d.width * 5) / 6, DEFAULT);
          final int w = Math.min(preferredSize.x, 800);
          final int h = preferredSize.y;
          return new Rectangle((d.width - w) / 2 + d.x, (d.height - h ) / 2 + d.y, w, h);
        }
      };
      dialog.open(returnCode -> {

      });
    });

    return buttons;
  }
}
