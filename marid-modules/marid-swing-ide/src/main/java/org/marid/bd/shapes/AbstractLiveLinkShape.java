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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import static org.marid.bd.shapes.LinkShapeType.LiveLinkConfigurationEditor.incubatorSize;
import static org.marid.bd.shapes.LinkShapeType.LiveLinkConfigurationEditor.species;
import static org.marid.concurrent.ThreadPools.getPoolSize;
import static org.marid.concurrent.ThreadPools.newArrayThreadPool;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractLiveLinkShape<T> extends LinkShape implements Cloneable, LogSupport {

    private static final ThreadPoolExecutor EXECUTOR = newArrayThreadPool(getPoolSize(4), 8192, new AbortPolicy());

    protected final List<T> specieList = new ArrayList<>();
    protected volatile T bestSpecie;
    protected volatile Point out;
    protected volatile Point in;
    protected final CopyOnWriteArrayList<Rectangle> rectangles = new CopyOnWriteArrayList<>();

    public AbstractLiveLinkShape(BlockComponent.Output output, BlockComponent.Input input) {
        super(output, input);
        for (int i = 0; i < species; i++) {
            specieList.add(defaultSpecie());
        }
        bestSpecie = specieList.get(0);
    }

    @Override
    public void update() {
        out = output.getConnectionPoint();
        in = input.getConnectionPoint();
        rectangles.clear();
        output.getBlockComponent().getSchemaEditor().visitBlockComponents(bc -> rectangles.add(bc.getBounds()));
        for (int i = 0; i < 32; i++) {
            try {
                EXECUTOR.execute(this::doGA);
            } catch (RejectedExecutionException x) {
                break;
            }
        }
        bestSpecie = specieList.get(0);
    }

    protected abstract T defaultSpecie();

    protected abstract T crossover(T male, T female, ThreadLocalRandom random);

    protected abstract void mutate(T specie, ThreadLocalRandom random);

    protected abstract double fitness(T specie);

    protected void doGA() {
        final TreeMap<Double, T> incubator = new TreeMap<>();
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            for (int i = 0; incubator.size() < species * incubatorSize; i++) {
                if (i >= 65536) {
                    throw new IllegalStateException("Singularity detected");
                }
                final int n = random.nextInt(1, specieList.size());
                final int mi = random.nextInt(n);
                final int fi = random.nextInt(n);
                final T male = specieList.get(mi);
                final T female = specieList.get(mi == fi ? fi + 1 : fi);
                final T child = crossover(male, female, random);
                mutate(child, random);
                incubator.put(fitness(child), child);
            }
            int i = 0;
            for (final Iterator<T> it = incubator.values().iterator(); i < species && it.hasNext(); i++) {
                if (i < specieList.size()) {
                    specieList.set(i, it.next());
                } else {
                    synchronized (specieList) {
                        specieList.add(it.next());
                    }
                }
            }
            synchronized (specieList) {
                while (specieList.size() > species) {
                    specieList.remove(specieList.size() - 1);
                }
            }
        } catch (Exception x) {
            warning("GA error", x);
        }
    }
}
