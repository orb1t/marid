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
import org.marid.logging.LogSupport;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import static org.marid.concurrent.ThreadPools.getPoolSize;
import static org.marid.concurrent.ThreadPools.newArrayThreadPool;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractLiveLinkShape<T> extends LinkShape implements Cloneable, LogSupport {

    private static final ThreadPoolExecutor EXECUTOR = newArrayThreadPool(getPoolSize(4), 8192, new DiscardPolicy());

    protected final List<T> list = new ArrayList<>();
    protected volatile Point out;
    protected volatile Point in;
    protected volatile Rectangle[] rectangles = new Rectangle[0];
    protected static final AtomicLong COUNTER = new AtomicLong();

    public AbstractLiveLinkShape(BlockComponent.Output output, BlockComponent.Input input) {
        super(output, input);
        init(LinkShapeType.LiveLinkConfigurationEditor.species);
    }

    protected void init(int species) {
        out = output.getConnectionPoint();
        in = input.getConnectionPoint();
        rectangles = Arrays.stream(output.getBlockComponent().getSchemaEditor().getComponents())
                .map(Component::getBounds)
                .toArray(Rectangle[]::new);
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
        final int species = LinkShapeType.LiveLinkConfigurationEditor.species;
        final int incubatorSize = LinkShapeType.LiveLinkConfigurationEditor.incubatorSize * species;
        init(species);
        for (int i = 0; i < 64; i++) {
            EXECUTOR.execute(() -> doGA(incubatorSize));
        }
    }

    protected abstract T defaultSpecie();

    protected abstract T crossover(T male, T female, ThreadLocalRandom random);

    protected abstract void mutate(T specie, ThreadLocalRandom random);

    protected abstract double fitness(T specie);

    protected void doGA(int incubatorSize) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final TreeMap<Double, T> incubator = new TreeMap<>();
        try {
            list.forEach(s -> incubator.put(fitness(s), s));
            for (int i = 0; i < incubatorSize; i++) {
                final int mi = random.nextInt(list.size() - 1);
                final int fi = random.nextInt(list.size());
                final T male = list.get(mi);
                final T female = list.get(fi == mi ? mi + 1 : fi);
                final T child = crossover(male, female, random);
                mutate(child, random);
                incubator.put(fitness(child), child);
            }
            final Iterator<T> it = incubator.values().iterator();
            for (int i = 0; i < list.size() && it.hasNext(); i++) {
                list.set(i, it.next());
            }
        } catch (Exception x) {
            warning("GA error", x);
        }
    }
}
