package org.marid.site;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import javax.inject.Inject;

@Theme("runo")
@CDIUI("")
public class LoginUI extends UI {

  @Inject
  private CDIViewProvider viewProvider;

  @Override
  protected void init(VaadinRequest vaadinRequest) {
    final VerticalLayout layout = new VerticalLayout();
    layout.addComponent(new Button("Add"));

    System.out.println(viewProvider);

    setContent(layout);
  }
}
