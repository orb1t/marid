/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.ide.status;

import javafx.application.Platform;
import org.jboss.logmanager.formatters.PatternFormatter;
import org.marid.Ide;
import org.marid.spring.postprocessors.LogBeansPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
@Service
@Lazy(false)
public class IdeStatusLogHandler extends Handler {

    private final IdeStatusBar bar;
    private final AtomicReference<String> lastMessage = new AtomicReference<>();

    @Autowired
    public IdeStatusLogHandler(IdeStatusBar bar) {
        this.bar = bar;
        setFilter(record -> !LogBeansPostProcessor.class.getName().equals(record.getLoggerName()));
        setFormatter(new PatternFormatter("%s"));
    }

    @PostConstruct
    private void init() {
        Ide.rootLogger.addHandler(this);
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            lastMessage.set(getFormatter().formatMessage(record));
        }
    }

    @Override
    @Scheduled(fixedDelay = 100L)
    public void flush() {
        final String prevMessage = lastMessage.getAndSet(null);
        if (prevMessage != null) {
            Platform.runLater(() -> bar.setText(prevMessage));
        }
    }

    @Override
    @PreDestroy
    public void close() throws SecurityException {
        Ide.rootLogger.removeHandler(this);
    }
}
