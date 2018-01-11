package org.marid.site;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("runo")
@CDIUI
public class SiteUI extends UI {

  @Override
  protected void init(VaadinRequest vaadinRequest) {
    final VerticalLayout layout = new VerticalLayout();
    layout.addComponent(new Button("Add"));

    setContent(layout);
  }
}
