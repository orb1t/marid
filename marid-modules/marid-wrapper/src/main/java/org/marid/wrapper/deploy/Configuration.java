/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.wrapper.deploy;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "configuration")
public class Configuration {

    @XmlElementWrapper(name = "vm-arguments")
    @XmlElement(name = "vm-argument")
    private final List<String> vmArguments = new LinkedList<>();

    @XmlElementWrapper(name = "marid-arguments")
    @XmlElement(name = "marid-argument")
    private final List<String> maridArguments = new LinkedList<>();

    @XmlElement
    private String version = "1.0";

    @XmlElement
    private Date timestamp = new Date();

    public List<String> getVmArguments() {
        return Collections.unmodifiableList(vmArguments);
    }

    public Configuration addVmArgument(String arg) {
        vmArguments.add(arg);
        return this;
    }

    public Configuration addVmArguments(Collection<String> args) {
        vmArguments.addAll(args);
        return this;
    }

    public Configuration addVmArguments(String... args) {
        return addVmArguments(Arrays.asList(args));
    }

    public List<String> getMaridArguments() {
        return Collections.unmodifiableList(maridArguments);
    }

    public Configuration addMaridArgument(String arg) {
        maridArguments.add(arg);
        return this;
    }

    public Configuration addMaridArguments(Collection<String> args) {
        maridArguments.addAll(args);
        return this;
    }

    public Configuration addMaridArguments(String... args) {
        return addMaridArguments(Arrays.asList(args));
    }

    @XmlTransient
    public String getVersion() {
        return version;
    }

    public Configuration setVersion(String version) {
        this.version = version;
        return this;
    }

    @XmlTransient
    public Date getTimestamp() {
        return timestamp;
    }

    public Configuration setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
