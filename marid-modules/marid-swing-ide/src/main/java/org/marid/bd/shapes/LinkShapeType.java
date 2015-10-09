/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.bd.shapes;

import org.marid.bd.schema.SchemaEditor;
import org.marid.l10n.L10nSupport;
import org.marid.pref.PrefSupport;
import org.marid.pref.PrefUtils;
import org.marid.swing.AbstractDialog;
import org.marid.swing.pref.Configurable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.prefs.Preferences;

import static java.awt.EventQueue.invokeLater;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static org.marid.bd.BlockComponent.Input;
import static org.marid.bd.BlockComponent.Output;

/**
 * @author Dmitry Ovchinnikov
 */
public enum LinkShapeType implements Configurable, PrefSupport, L10nSupport {

    LINE("Simple line link", LineLinkShape::new, w -> null),
    ORTHO("Othogonal link", OrthoLinkShape::new, OrthoLinkConfigurationEditor::new),
    LIVE("Live link", LiveLinkShape::new, LiveLinkConfigurationEditor::new);

    private final String name;
    private final BiFunction<Output, Input, LinkShape> linkShapeFunction;
    private final Function<Window, JDialog> configurationWindowFunction;
    private final Preferences preferences = PrefUtils.preferences("bd", "links");
    private final Map<SchemaEditor, Void> registry = new WeakHashMap<>();

    LinkShapeType(String name, BiFunction<Output, Input, LinkShape> lsf, Function<Window, JDialog> cwf) {
        this.name = name;
        this.linkShapeFunction = lsf;
        this.configurationWindowFunction = cwf;
    }

    @Override
    public String toString() {
        return s(name);
    }

    public LinkShape linkShapeFor(Output output, Input input) {
        preferences().addPreferenceChangeListener(ev -> invokeLater(() -> registry.forEach((k, v) -> k.repaint())));
        registry.put(output.getBlockComponent().getSchemaEditor(), null);
        return linkShapeFunction.apply(output, input);
    }

    @Override
    public JDialog createConfigurationDialog(Window window) {
        return configurationWindowFunction.apply(window);
    }

    @Override
    public Preferences preferences() {
        return preferences.node(name());
    }

    public static class LiveLinkConfigurationEditor extends AbstractDialog {

        private static final String MUTATION_PROBABILITY_KEY = "mutationProbability";
        private static final String INCUBATOR_SIZE_KEY = "incubatorSize";
        private static final String SPECIES_KEY = "species";

        public static volatile int mutationProbability = LIVE.getPref(MUTATION_PROBABILITY_KEY, 30);
        public static volatile int incubatorSize = LIVE.getPref(INCUBATOR_SIZE_KEY, 10);
        public static volatile int species = LIVE.getPref(SPECIES_KEY, 10);

        static {
            LIVE.preferences().addPreferenceChangeListener(ev -> {
                if (ev.getNewValue() == null) {
                    return;
                }
                switch (ev.getKey()) {
                    case MUTATION_PROBABILITY_KEY:
                        mutationProbability = Integer.parseInt(ev.getNewValue());
                        break;
                    case INCUBATOR_SIZE_KEY:
                        incubatorSize = Integer.parseInt(ev.getNewValue());
                        break;
                    case SPECIES_KEY:
                        species = Integer.parseInt(ev.getNewValue());
                        break;
                }
            });
        }

        private final JSpinner mpSpinner = new JSpinner(new SpinnerNumberModel(mutationProbability, 10, 100, 1));
        private final JSpinner isSpinner = new JSpinner(new SpinnerNumberModel(incubatorSize, 5, 100, 1));
        private final JSpinner scSpinner = new JSpinner(new SpinnerNumberModel(species, 5, 100, 1));

        public LiveLinkConfigurationEditor(Window window) {
            super(window, LS.s("Live link configuration"), ModalityType.MODELESS);
            pack();
        }

        @Override
        protected void fill(GroupLayout g, GroupLayout.SequentialGroup v, GroupLayout.SequentialGroup h) {
            final JLabel mpLabel = new JLabel(s("Mutation probability") + ":");
            final JLabel isLabel = new JLabel(s("Incubator size") + ":");
            final JLabel scLabel = new JLabel(s("Species count") + ":");
            v.addGroup(g.createParallelGroup(BASELINE).addComponent(mpLabel).addComponent(mpSpinner));
            v.addGroup(g.createParallelGroup(BASELINE).addComponent(isLabel).addComponent(isSpinner));
            v.addGroup(g.createParallelGroup(BASELINE).addComponent(scLabel).addComponent(scSpinner));
            h.addGroup(g.createParallelGroup().addComponent(mpLabel).addComponent(isLabel).addComponent(scLabel));
            h.addGroup(g.createParallelGroup().addComponent(mpSpinner).addComponent(isSpinner).addComponent(scSpinner));
            addDefaultButtons();
        }

        @Override
        protected void accept() {
            LIVE.preferences().putInt(MUTATION_PROBABILITY_KEY, ((Number) mpSpinner.getValue()).intValue());
            LIVE.preferences().putInt(INCUBATOR_SIZE_KEY, ((Number) isSpinner.getValue()).intValue());
            LIVE.preferences().putInt(SPECIES_KEY, ((Number) scSpinner.getValue()).intValue());
        }
    }

    public static class OrthoLinkConfigurationEditor extends AbstractDialog {

        private static final String JOIN_KEY = "join";

        public static volatile int join = ORTHO.getPref(JOIN_KEY, 10);

        static {
            ORTHO.preferences().addPreferenceChangeListener(ev -> {
                if (ev.getNewValue() == null) {
                    return;
                }
                join = Integer.parseInt(ev.getNewValue());
            });
        }

        private final JSpinner jlSpinner = new JSpinner(new SpinnerNumberModel(join, 0, 20, 1));

        public OrthoLinkConfigurationEditor(Window window) {
            super(window, LS.s("Ortho link configuration"), ModalityType.MODELESS);
            pack();
        }

        @Override
        protected void fill(GroupLayout gl, GroupLayout.SequentialGroup vg, GroupLayout.SequentialGroup hg) {
            final JLabel jlLabel = new JLabel(LS.s("Join size") + ":");
            vg.addGroup(gl.createParallelGroup(BASELINE).addComponent(jlLabel).addComponent(jlSpinner));
            hg.addGroup(gl.createParallelGroup().addComponent(jlLabel));
            hg.addGroup(gl.createParallelGroup().addComponent(jlSpinner));
            addDefaultButtons();
        }

        @Override
        protected void accept() {
            ORTHO.putPref(JOIN_KEY, ((Number) jlSpinner.getValue()).intValue());
        }
    }
}
