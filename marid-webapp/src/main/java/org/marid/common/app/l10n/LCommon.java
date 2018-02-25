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

package org.marid.common.app.l10n;

import org.eclipse.rap.rwt.RWT;

public class LCommon {

  public String remove;
  public String add;
  public String name;
  public String password;
  public String close;

  public static LCommon get() {
    return RWT.NLS.getUTF8Encoded("res.common", LCommon.class);
  }
}
