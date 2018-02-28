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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.marid.app.dao.UserDao;
import org.marid.app.model.MaridUser;
import org.marid.common.app.l10n.LUsers;
import org.marid.common.app.util.UIImages;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.FILL_HORIZONTAL;

@Component
public class UsersToolbar extends ToolBar {

  public UsersToolbar(Shell mainShellBean) {
    super(mainShellBean, BORDER);
    setLayoutData(new GridData(FILL_HORIZONTAL));
  }

  @Bean
  @Order(1)
  public ToolItem addItem(ObjectProvider<AddUserDialog> dialog) {
    final ToolItem it = new ToolItem(this, PUSH);
    it.setImage(new Image(getDisplay(), UIImages.image("images/add.png")));
    it.setToolTipText(LUsers.get().addUser);
    it.addListener(Selection, event -> dialog.getObject().open());
    return it;
  }

  @Bean
  @Order(2)
  public ToolItem modifyItem(UsersTable table, ObjectProvider<ModifyUserDialog> dialog, Authentication authentication) {
    final ToolItem it = new ToolItem(this, PUSH);
    it.setImage(new Image(getDisplay(), UIImages.image("images/modify.png")));
    it.setToolTipText(LUsers.get().modifyUser);
    it.addListener(Selection, event -> {
      final int index = table.getSelectionIndex();
      if (index < 0) {
        return;
      }
      final TableItem item = table.getItem(index);
      final MaridUser user = (MaridUser) item.getData();
      if (!authentication.getName().equals(user.getUsername())) {
        dialog.getObject().open();
      }
    });
    return it;
  }

  @Bean
  @Order(3)
  public ToolItem removeItem(UsersTable table, Authentication authentication, UserDao dao) {
    final ToolItem it = new ToolItem(this, PUSH);
    it.setImage(new Image(getDisplay(), UIImages.image("images/remove.png")));
    it.setToolTipText(LUsers.get().removeUser);
    it.addListener(Selection, event -> {
      final int index = table.getSelectionIndex();
      if (index < 0) {
        return;
      }
      final TableItem item = table.getItem(index);
      final MaridUser user = (MaridUser) item.getData();
      if (!authentication.getName().equals(user.getUsername())) {
        dao.removeUser(user.getUsername());
      }
      table.remove(index);
    });
    return it;
  }
}
