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

package org.marid.common.app.util;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public interface UIImages {

  @NotNull
  static ImageData image(@NotNull String path) {
    try (final InputStream inputStream = UIImages.class.getResourceAsStream("/" + path)) {
      if (inputStream != null) {
        return new ImageData(inputStream);
      } else {
        return new ImageData(24, 24, 8, new PaletteData(0, 0, 0));
      }
    } catch (IOException x) {
      throw new UncheckedIOException(x);
    }
  }
}
