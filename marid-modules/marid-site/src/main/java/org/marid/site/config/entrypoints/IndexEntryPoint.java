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

package org.marid.site.config.entrypoints;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.marid.image.MaridIcon;
import org.marid.site.config.tabs.Tab;
import org.marid.site.images.ImageUtil;
import org.marid.site.spring.EntryPointType;
import org.marid.site.util.NlsSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import java.util.*;

import static java.awt.Color.GREEN;
import static org.marid.site.util.FontUtil.overrideFont;

/**
 * @author Dmitry Ovchinnikov
 */
@EntryPointType("/index")
public class IndexEntryPoint extends ScrollableEntryPoint implements NlsSupport {

    private final Set<Tab> tabs;
    private StackLayout stackLayout;
    private Composite stackComposite;
    private final Map<String, Composite> tabMap = new HashMap<>();
    private final List<Button> buttons = new ArrayList<>();

    @Autowired
    public IndexEntryPoint(Set<Tab> tabs) {
        this.tabs = tabs;
    }

    @Override
    protected void fillContents(Composite composite) {
        tabMap.clear();
        fill(composite);
        final Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        stackComposite = new Composite(composite, SWT.NONE);
        stackComposite.setLayout(stackLayout = new StackLayout());
        stackComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        tabs.stream().sorted(Comparator.comparingInt(Ordered::getOrder)).forEach(tab -> {
            final Composite tabComposite = new Composite(stackComposite, SWT.NONE);
            tabComposite.setLayout(new GridLayout(1, false));
            tab.fill(tabComposite);
            tabMap.put(tab.getName(), tabComposite);
            if (stackLayout.topControl == null) {
                stackLayout.topControl = tabComposite;
            }
        });
    }

    private void fill(Composite composite) {
        final Composite group = new Composite(composite, SWT.FILL);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setLayout(new GridLayout(3, false));

        addIconLabel(group);
        addLinks(group);
        addTextComposite(group);
    }

    private void addIconLabel(Composite composite) {
        final Label iconLabel = new Label(composite, SWT.NONE);
        iconLabel.setImage(ImageUtil.image(composite.getDisplay(), MaridIcon.getImage(64, GREEN)));
    }

    private void addLinks(Composite composite) {
        buttons.clear();
        final Composite linksComposite = new Composite(composite, SWT.NONE);
        final RowLayout rowLayout = new RowLayout();
        rowLayout.fill = true;
        rowLayout.justify = true;
        rowLayout.spacing = 10;
        linksComposite.setLayout(rowLayout);
        tabs.stream().sorted(Comparator.comparingInt(Ordered::getOrder)).forEach(tab -> addLink(linksComposite, tab));
    }

    private void addLink(Composite composite, Tab tab) {
        final Button button = new Button(composite, SWT.TOGGLE);
        buttons.add(button);
        button.setImage(img(composite.getDisplay(), tab.getIcon()));
        button.setToolTipText(s(tab.getTitle()));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final Composite selected = tabMap.get(tab.getName());
                if (selected != null) {
                    stackLayout.topControl = selected;
                    stackComposite.layout();
                }
                buttons.stream().filter(b -> b != button).forEach(b -> b.setSelection(false));
                if (!button.getSelection()) {
                    button.setSelection(true);
                }
            }
        });
    }

    private void addTextComposite(Composite composite) {
        final Composite textComposite = new Composite(composite, SWT.FILL);
        textComposite.setLayout(new RowLayout(SWT.VERTICAL));
        textComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true));
        final Label text = new Label(textComposite, SWT.NONE);
        text.setText(s("title"));
        text.setFont(overrideFont(text.getFont(), d -> new FontData(d.getName(), d.getHeight() + 4, SWT.BOLD)));
    }
}
