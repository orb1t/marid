/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.app;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class DeployToMaridOrg {

  public static void main(String... args) throws Throwable {
    {
      final Process process = new ProcessBuilder("mvn", "-DskipTests", "-pl", "org.marid:marid-webapp", "-am", "clean", "install")
          .inheritIO()
          .start();
      final int result = process.waitFor();
      if (result != 0) {
        throw new IllegalStateException("Result: " + result);
      }
    }

    {
      final ProtectionDomain protectionDomain = DeployToMaridOrg.class.getProtectionDomain();
      final CodeSource codeSource = protectionDomain.getCodeSource();
      final URL location = codeSource.getLocation();
      final Path path = Paths.get(location.toURI()).getParent().getParent();

      final Process process = new ProcessBuilder("mvn", "wagon:sshexec@stop", "wagon:upload@deploy", "wagon:sshexec@start")
          .inheritIO()
          .directory(path.toFile())
          .start();
      final int result = process.waitFor();
      if (result != 0) {
        throw new IllegalStateException("Result: " + result);
      }
    }
  }
}
