package org.marid.ide.status;

import javax.xml.soap.Node;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeTaskResult {

    public final Node graphic;

    public IdeTaskResult(Node graphic) {
        this.graphic = graphic;
    }
}
