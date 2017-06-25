package org.marid.ide.event;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.context.ApplicationEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class PropagatedEvent extends ApplicationEvent {

    public PropagatedEvent(Object source) {
        super(source);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE, true);
    }
}
