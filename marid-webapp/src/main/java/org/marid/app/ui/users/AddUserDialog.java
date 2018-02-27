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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.marid.app.dao.UserDao;
import org.marid.app.model.MaridUser;
import org.marid.app.model.MaridUserInfo;
import org.marid.common.app.l10n.LCommon;
import org.marid.common.app.l10n.LUsers;
import org.marid.rwt.spring.PrototypeScoped;
import org.marid.rwt.spring.UIContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.TreeSet;

import static org.eclipse.jface.dialogs.IDialogConstants.CLOSE_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.PROCEED_ID;
import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.FILL_HORIZONTAL;
import static org.marid.common.app.control.Controls.label;

@Component
@PrototypeScoped
public class AddUserDialog extends Dialog {

  private final UserDao dao;
  private final UsersTable table;

  private Text userField;
  private Text passwordField;
  private DateTime dateField;
  private Button userButton;
  private Button adminButton;

  public AddUserDialog(UIContext context, UserDao dao, UsersTable table) {
    super(context.getShell());
    this.dao = dao;
    this.table = table;
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
    newShell.setText(LUsers.get().addUser);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    final Composite composite = (Composite) super.createDialogArea(parent);

    final GridLayout layout = (GridLayout) composite.getLayout();

    layout.numColumns = 2;

    label(composite, LCommon.get().name);
    userField = new Text(composite, BORDER);
    userField.setLayoutData(new GridData(FILL_HORIZONTAL));

    label(composite, LCommon.get().password);
    passwordField = new Text(composite, BORDER | PASSWORD);
    passwordField.setLayoutData(new GridData(FILL_HORIZONTAL));

    label(composite, LCommon.get().expirationDate);
    dateField = new DateTime(composite, BORDER | CALENDAR);
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
          final MaridUser user = new MaridUser(userField.getText(), new MaridUserInfo(
              passwordField.getText(),
              true,
              String.format("%04d-%02d-%02d", dateField.getYear(), dateField.getMonth(), dateField.getDay()),
              authorities.toArray(new String[authorities.size()])
          ));
          dao.saveUser(user);
          table.userItem(user);
          table.layout();
          break;
      }
    } finally {
      close();
    }
  }
}
