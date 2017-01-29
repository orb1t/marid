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

package org.marid.ide.project;

import javafx.beans.Observable;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableList;
import static org.junit.Assert.assertSame;
import static org.marid.ide.project.ProfileReflections.observableStream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Category({NormalTests.class})
public class ProjectGarbageCollectorTest {

    @Test
    public void testLists() {
        final ObservableValue<?> value1 = new SimpleFloatProperty(1f);
        final ObservableList<?> list1 = observableArrayList("a", "b");
        final ObservableList<?> list2 = observableArrayList("x", list1, value1);
        final ObservableValue<?> value2 = new SimpleObjectProperty<>(list2);
        final ObservableList<?> list = observableList(asList(value1, list1, list2, value2));
        final Observable[] observables = observableStream(Stream.of(list)).toArray(Observable[]::new);

        assertSame(observables[0], list);
        assertSame(observables[1], value1);
        assertSame(observables[2], list1);
        assertSame(observables[3], list2);
        assertSame(observables[4], list1);
        assertSame(observables[5], value1);
        assertSame(observables[6], value2);
        assertSame(observables[7], list2);
        assertSame(observables[8], list1);
        assertSame(observables[9], value1);
    }
}
