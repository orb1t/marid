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

import org.marid.bd.BlockComponent;
import org.marid.bd.schema.SchemaEditor;
import org.marid.bd.shapes.LinkShapeType.LiveLinkConfigurationEditor;
import org.marid.concurrent.MaridThreadFactory;
import org.marid.logging.LogSupport;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;

import static java.util.Arrays.stream;
import static org.marid.concurrent.ThreadPools.getPoolSize;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractLiveLinkShape<T> extends LinkShape implements Cloneable, LogSupport {

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            getPoolSize(4),
            getPoolSize(4),
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(1024),
            new MaridThreadFactory("lls", true, 96L * 1024L),
            new DiscardPolicy());

    protected final SchemaEditor editor;
    protected final List<T> list;

    public AbstractLiveLinkShape(BlockComponent.Output output, BlockComponent.Input input) {
        super(output, input);
        editor = output.getBlockComponent().getSchemaEditor();
        final T specie = defaultSpecie();
        final int species = LiveLinkConfigurationEditor.species;
        list = new ArrayList<>(species);
        for (int i = 0; i < species; i++) {
            list.add(specie);
        }
    }

    protected void init(int species) {
        while (list.size() < species) {
            list.add(defaultSpecie());
        }
        while (list.size() > species) {
            list.remove(list.size() - 1);
        }
    }

    protected T bestSpecie() {
        return list.get(0);
    }

    @Override
    public void update() {
        final int species = LiveLinkConfigurationEditor.species;
        final int incubatorSize = LiveLinkConfigurationEditor.incubatorSize * species;
        init(species);
        final ConcurrentSkipListMap<Double, T> incubator = new ConcurrentSkipListMap<>();
        final LiveData liveData = new LiveData();
        list.forEach(s -> incubator.put(fitness(s, liveData), s));
        final Runnable gaTask = () -> {
            try {
                doGA(incubatorSize, incubator, liveData);
            } catch (IndexOutOfBoundsException x) {
                log(WARNING, "Shrink IOOBE");
            } catch (Exception x) {
                log(WARNING, "GA error", x);
            }
        };
        for (int i = 0; i < species; i++) {
            EXECUTOR.execute(gaTask);
        }
    }

    protected abstract T defaultSpecie();

    protected abstract T crossoverAndMutate(T male, T female, ThreadLocalRandom random);

    protected abstract double fitness(T specie, LiveData liveData);

    protected void doGA(int incubatorSize, NavigableMap<Double, T> incubator, LiveData liveData) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int n = list.size();
        for (int i = 0; i < incubatorSize; i++) {
            final T male = list.get(random.nextInt(n));
            final T female = list.get(random.nextInt(n));
            final T child = crossoverAndMutate(male, female, random);
            incubator.put(fitness(child, liveData), child);
        }
        final Iterator<T> it = incubator.values().iterator();
        for (int i = 0; i < n && it.hasNext(); i++) {
            list.set(i, it.next());
        }
    }

    public class LiveData {

        final Point out;
        final Point in;
        final Rectangle[] rectangles;

        public LiveData() {
            out = output.getConnectionPoint();
            in = input.getConnectionPoint();
            rectangles = stream(editor.getComponents()).map(Component::getBounds).toArray(Rectangle[]::new);
        }
    }
}
