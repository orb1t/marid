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

package org.marid.app.ivy;

import org.slf4j.Logger;

import static org.apache.ivy.util.Message.*;

public class IvySlfLoggerAdapter implements BaseMessageLogger {

  private final Logger logger;

  public IvySlfLoggerAdapter(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void log(String msg, int level) {
    switch (level) {
      case MSG_INFO:
        logger.info(msg);
        break;
      case MSG_DEBUG:
        logger.debug(msg);
        break;
      case MSG_ERR:
        logger.error(msg);
        break;
      case MSG_WARN:
        logger.warn(msg);
        break;
      case MSG_VERBOSE:
        logger.trace(msg);
        break;
    }
  }
}
