/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
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

package org.marid.ide;

import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.stage.Window;
import org.marid.ide.event.PropagatedEvent;
import org.marid.ide.tabs.IdeTab;
import org.marid.misc.Casts;
import org.marid.idelib.spring.postprocessors.MaridCommonPostProcessor;
import org.marid.idelib.spring.postprocessors.WindowAndDialogPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("dependants")
public class IdeDependants {

  private static final Collection<DependantContext> CONTEXTS = new ConcurrentLinkedQueue<>();

  private final GenericApplicationContext parent;

  @Autowired
  public IdeDependants(GenericApplicationContext parent) {
    this.parent = parent;
  }

  public GenericApplicationContext run(Consumer<AnnotationConfigApplicationContext> consumer) {
    final AnnotationConfigApplicationContext context = new DependantContext(parent);
    consumer.accept(context);
    context.refresh();
    context.start();
    return context;
  }

  @SafeVarargs
  public final GenericApplicationContext start(Object conf, Consumer<AnnotationConfigApplicationContext>... consumers) {
    return CONTEXTS.stream()
        .filter(c -> c.containsBean("$conf") && c.getBean("$conf").equals(conf))
        .findFirst()
        .map(IdeDependants::activate)
        .orElseGet(() -> run(context -> {
          final Supplier<Object> supplier = () -> conf;
          final Class<Object> type = Casts.cast(conf.getClass());
          context.registerBean("$conf", type, supplier);
          for (final Consumer<AnnotationConfigApplicationContext> c : consumers) {
            c.accept(context);
          }
        }));
  }

  public GenericApplicationContext start(Object conf, String displayName) {
    return start(conf, c -> c.setDisplayName(displayName));
  }

  private static GenericApplicationContext activate(GenericApplicationContext context) {
    context.getBeansOfType(IdeTab.class, false, false).forEach((name, tab) -> {
      final SelectionModel<Tab> selectionModel = tab.getTabPane().getSelectionModel();
      selectionModel.select(tab);
    });
    context.getBeansOfType(Window.class, false, false).forEach((name, win) -> win.requestFocus());
    context.getBeansOfType(Dialog.class, false, false).forEach((name, win) -> win.show());
    return context;
  }

  @Override
  public String toString() {
    return parent.toString();
  }

  private static class DependantContext extends AnnotationConfigApplicationContext {

    private final Listener parentListener;

    private DependantContext(GenericApplicationContext parent) {
      parent.addApplicationListener(parentListener = new Listener(this, parent));
      setAllowBeanDefinitionOverriding(false);
      setAllowCircularReferences(false);
      getBeanFactory().addBeanPostProcessor(new WindowAndDialogPostProcessor(this));
      getBeanFactory().addBeanPostProcessor(new MaridCommonPostProcessor());
      getBeanFactory().setParentBeanFactory(parent.getDefaultListableBeanFactory());
      register(IdeDependants.class);
    }

    @Override
    protected void onRefresh() throws BeansException {
      parentListener.close();
      CONTEXTS.add(this);
    }

    @Override
    protected void onClose() {
      CONTEXTS.removeIf(c -> c == this);
      CONTEXTS.stream()
          .filter(c -> c.getBeanFactory().getParentBeanFactory() == getBeanFactory())
          .findFirst()
          .ifPresent(AbstractApplicationContext::close);
    }
  }
}

class Listener extends WeakReference<GenericApplicationContext> implements ApplicationListener<ApplicationEvent> {

  private final GenericApplicationContext parent;

  Listener(GenericApplicationContext referent, GenericApplicationContext parent) {
    super(referent);
    this.parent = parent;
  }

  @Override
  public void onApplicationEvent(@Nonnull ApplicationEvent event) {
    final GenericApplicationContext context = get();
    if (context == null || !context.isActive()) {
      close();
    } else {
      if (event instanceof ContextClosedEvent) {
        context.close();
      } else if (event instanceof PropagatedEvent) {
        context.publishEvent(event);
      }
    }
  }

  void close() {
    parent.getApplicationListeners().remove(this);
  }
}
