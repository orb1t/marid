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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.marid.app.dao.UserDao;
import org.marid.app.model.MaridUser;
import org.marid.app.model.MaridUserInfo;
import org.marid.common.app.l10n.LCommon;
import org.marid.common.app.l10n.LUsers;
import org.marid.rwt.spring.PrototypeScoped;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.String.format;
import static org.eclipse.jface.dialogs.IDialogConstants.CLOSE_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.PROCEED_ID;
import static org.eclipse.swt.SWT.*;
import static org.marid.common.app.control.Controls.FILLED_CIRCLE;
import static org.marid.common.app.control.Controls.label;

@Component
@PrototypeScoped
public class ModifyUserDialog extends Dialog {

  private final UserDao dao;
  private final UsersTable table;
  private final TableItem item;

  private DateTime dateField;
  private Button userButton;
  private Button adminButton;

  public ModifyUserDialog(Shell mainShellBean, UserDao dao, UsersTable table) {
    super(mainShellBean);
    this.dao = dao;
    this.table = table;
    this.item = table.getSelection()[0];
    setBlockOnOpen(false);
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, CLOSE_ID, LCommon.get().close, false);
    createButton(parent, PROCEED_ID, LCommon.get().proceed, true);
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(LUsers.get().modifyUser);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    final Composite composite = (Composite) super.createDialogArea(parent);

    final GridLayout layout = (GridLayout) composite.getLayout();

    layout.numColumns = 2;

    label(composite, LCommon.get().expirationDate);
    dateField = new DateTime(composite, BORDER | DATE | DROP_DOWN);
    final ZonedDateTime date = Instant.now().atZone(ZoneId.systemDefault()).plus(1L, ChronoUnit.YEARS);
    dateField.setDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());

    label(composite, LUsers.get().user);
    userButton = new Button(composite, CHECK);
    userButton.setSelection(true);

    label(composite, LUsers.get().admin);
    adminButton = new Button(composite, CHECK);

    return composite;
  }

  @Override
  protected void buttonPressed(int buttonId) {
    try {
      switch (buttonId) {
        case PROCEED_ID:
          final Set<String> authorities = new TreeSet<>();
          if (adminButton.getSelection()) {
            authorities.add("ROLE_ADMIN");
          }
          if (userButton.getSelection()) {
            authorities.add("ROLE_USER");
          }
          final String date = format("%04d-%02d-%02d", dateField.getYear(), dateField.getMonth(), dateField.getDay());
          final MaridUser user = new MaridUser(item.getText(0), new MaridUserInfo(
              null,
              true,
              date,
              authorities.toArray(new String[authorities.size()])
          ));
          dao.saveUser(user);
          item.setText(1, date);
          item.setText(2, user.isUser() ? FILLED_CIRCLE : "");
          item.setText(3, user.isAdmin() ? FILLED_CIRCLE : "");

          table.layout();
          break;
      }
    } finally {
      close();
    }
  }
}
