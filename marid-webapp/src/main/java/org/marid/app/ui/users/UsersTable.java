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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.marid.app.dao.UserDao;
import org.marid.app.model.MaridUser;
import org.marid.common.app.control.Controls;
import org.marid.common.app.l10n.LCommon;
import org.marid.common.app.l10n.LUsers;
import org.marid.rwt.spring.UIContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.FILL_BOTH;
import static org.marid.common.app.control.Controls.FILLED_CIRCLE;
import static org.marid.common.app.control.Controls.column;

@Component
@DependsOn("usersToolbar")
public class UsersTable extends Table {

  public UsersTable(UIContext context, UserDao dao) {
    super(context.getShell(), V_SCROLL | BORDER | SINGLE);
    setHeaderVisible(true);
    setLinesVisible(true);
    setTouchEnabled(true);
    setLayoutData(new GridData(FILL_BOTH));

    column(this, LCommon.get().name, c -> c.setAlignment(LEFT));
    column(this, LCommon.get().date, c -> c.setAlignment(CENTER));
    column(this, LUsers.get().user, c -> c.setAlignment(CENTER));
    column(this, LUsers.get().admin, c -> c.setAlignment(CENTER));

    dao.getUsers().forEach(this::userItem);

    Controls.autoFitColumns(this);

    setFocus();
  }

  TableItem userItem(MaridUser user) {
    final TableItem item = new TableItem(this, NONE);

    item.setData(user);

    item.setText(0, user.getUsername());
    item.setText(1, user.getExpirationDate().toString());
    item.setText(2, user.isUser() ? FILLED_CIRCLE : "");
    item.setText(3, user.isAdmin() ? FILLED_CIRCLE : "");

    return item;
  }
}
