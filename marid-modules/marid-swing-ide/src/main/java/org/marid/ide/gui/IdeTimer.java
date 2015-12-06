package org.marid.ide.gui;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeTimer {

    private final List<ActionListener> listeners = new CopyOnWriteArrayList<>();
    private final Timer timer = new Timer(1_000, ev -> listeners.forEach(e -> e.actionPerformed(ev)));

    @PostConstruct
    public void start() {
        timer.start();
    }

    @PreDestroy
    public void stop() {
        timer.stop();
        listeners.clear();
    }

    public void addListener(ActionListener actionListener) {
        listeners.add(actionListener);
    }

    public void removeListener(ActionListener actionListener) {
        listeners.remove(actionListener);
    }
}
