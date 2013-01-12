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
package org.marid.db.storage;

import java.io.IOException;

/**
 * Data archive storage.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface ArchiveStorage extends HistoricalStorage {

    /**
     * Inserts a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long ts, double v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Values array.
     * @param q Tag array.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, double[] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long ts, float v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Values array.
     * @param q Tag array.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, float[] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long ts, int v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tag array.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, int[] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long ts, long v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, long[] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long ts, byte[] v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, byte[][] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long ts, boolean v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, boolean[] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long t, double[] v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, double[][] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion value.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long t, float[] v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param tag Tag array.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, float[][] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long t, int[] v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param tag Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, int[][] v, String[]... tag) throws IOException;

    /**
     * Inserts a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long t, long[] v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, long[][] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long t, String[] v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, String[][] v, String[]... q) throws IOException;

    /**
     * Inserts a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean insert(long t, String v, String... tag) throws IOException;

    /**
     * Inserts values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int insert(long[] t, String[] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long ts, double v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Values array.
     * @param q Tag array.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, double[] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long ts, float v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Values array.
     * @param q Tag array.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, float[] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long ts, int v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tag array.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, int[] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long ts, long v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, long[] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long ts, byte[] v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, byte[][] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long ts, boolean v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, boolean[] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long t, double[] v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, double[][] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion value.
     * @throws IOException An I/O exception.
     */
    public boolean append(long t, float[] v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param tag Tag array.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, float[][] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long t, int[] v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param tag Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, int[][] v, String[]... tag) throws IOException;

    /**
     * Appends a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long t, long[] v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, long[][] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long t, String[] v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, String[][] v, String[]... q) throws IOException;

    /**
     * Appends a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean append(long t, String v, String... tag) throws IOException;

    /**
     * Appends values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Insertions count.
     * @throws IOException An I/O exception.
     */
    public int append(long[] t, String[] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long ts, double v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Values array.
     * @param q Tag array.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, double[] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long ts, float v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Values array.
     * @param q Tag array.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, float[] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long ts, int v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tag array.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, int[] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long ts, long v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, long[] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long ts, byte[] v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, byte[][] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param ts Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long ts, boolean v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, boolean[] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long t, double[] v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, double[][] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion value.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long t, float[] v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param tag Tag array.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, float[][] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long t, int[] v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param tag Tags.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, int[][] v, String[]... tag) throws IOException;

    /**
     * Merges a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long t, long[] v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, long[][] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long t, String[] v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, String[][] v, String[]... q) throws IOException;

    /**
     * Merges a value.
     * @param t Timestamp.
     * @param v Value.
     * @param tag Tag.
     * @return Insertion result.
     * @throws IOException An I/O exception.
     */
    public boolean merge(long t, String v, String... tag) throws IOException;

    /**
     * Merges values.
     * @param t Timestamp array.
     * @param v Value array.
     * @param q Tags.
     * @return Updates count.
     * @throws IOException An I/O exception.
     */
    public int merge(long[] t, String[] v, String[]... q) throws IOException;
}
