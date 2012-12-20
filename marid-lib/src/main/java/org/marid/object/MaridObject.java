/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.object;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Marid object interface.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface MaridObject extends Cloneable {
	/**
	 * Clone this object.
	 * @return A clone.
	 * @since 1.0
	 */
	public MaridObject clone();
	
	/**
	 * Get the source map.
	 * @return Source map.
	 */
	public Map getSource();
	
	/**
	 * Clone object with a new parent.
	 * @param newParent New parent.
	 * @return Cloned object.
	 * @since 1.0
	 */
	public MaridObject clone(MaridObject newParent);
		
	/**
	 * Get children map.
	 * @return Children map.
	 * @since 1.0
	 */
	public Map<String, ? extends List<? extends MaridObject>> getChildrenMap();
	
	/**
	 * Get parent object.
	 * @return Parent object.
	 * @since 1.0
	 */
	public MaridObject getParent();
	
	/**
	 * Get the root.
	 * @return Root object.
	 * @since 1.0
	 */
	public MaridObject getRoot();
	
	/**
	 * Get a root by class.
	 * @param <T> Root type.
	 * @param c Root class.
	 * @return Typed root.
	 */
	public <T extends MaridObject> T getRoot(Class<T> c);
	
	/**
	 * Get parameter value.
	 * @param key Parameter key.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public Object get(String key);
	
	/**
	 * Get parameter value.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public Object get(String key, Object def);
	
	/**
	 * Puts a parameter into the parameter map.
	 * @param key Parameter key.
	 * @param val Parameter value.
	 * @return Old parameter value.
	 * @since 1.0
	 */
	public Object put(String key, Object val);
	
	/**
	 * Get value as byte.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public byte getByte(String key, byte def);
	
	/**
	 * Get value as byte array.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public byte[] getBytes(String key, byte[] def);
	
	/**
	 * Get value as char.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public char getChar(String key, char def);
	
	/**
	 * Get value as character array.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public char[] getChars(String key, char[] def);
	
	/**
	 * Get value as int.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public int getInt(String key, int def);
	
	/**
	 * Get value as long.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public long getLong(String key, long def);
	
	/**
	 * Get value as float.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public float getFloat(String key, float def);
	
	/**
	 * Get value as double.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public double getDouble(String key, double def);
	
	/**
	 * Get value as short.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public short getShort(String key, short def);
	
	/**
	 * Get value as boolean.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public boolean getBoolean(String key, boolean def);
	
	/**
	 * Get value as string.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public String getString(String key, String def);
	
	/**
	 * Get value as string.
	 * @param key Parameter key.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public String getString(String key);
	
	/**
	 * Get value as date.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Parameter value.
	 * @since 1.0
	 */
	public Date getDate(String key, Date def);
	
	/**
	 * Get the in-memory hexadecimal id (identity hash code).
	 * @return In-memory hexadecimal id.
	 * @since 1.0
	 */
	public String getMemId();
	
	/**
	 * Get the ID (last name of the path).
	 * @return ID string.
	 * @since 1.0
	 */
	public String getId();
	
	/**
	 * Get the current object path.
	 * @return Object path.
	 * @since 1.0
	 */
	public String getPath();
	
	/**
	 * Get the relative path.
	 * @param o Base object.
	 * @return Relative path.
	 * @since 1.0
	 */
	public String getPath(MaridObject o);
	
	/**
	 * Get the relative path.
	 * @param c Base object class.
	 * @return Relative path.
	 * @since 1.0
	 */
	public String getPath(Class<? extends MaridObject> c);
	
	/**
	 * Get an object by path.
	 * @param path Object path (UNIX path rules)
	 * @return An object.
	 * @since 1.0
	 */
	public MaridObject getObject(String path);
	
	/**
	 * Checks if this object contains another object with the given ID.
	 * @param id Object ID.
	 * @return Check status.
	 * @since 1.0
	 */
	public boolean containsObject(String id);
	
	/**
	 * Checks if this object contains another object with the given ID
	 * within the given group.
	 * @param group A group name.
	 * @param id Object ID.
	 * @return Check status.
	 * @since 1.0
	 */
	public boolean containsObject(String group, String id);
	
	/**
	 * Checks if this object contains a parameter with the given key.
	 * @param key Parameter key.
	 * @return Check status.
	 * @since 1.0
	 */
	public boolean containsKey(String key);
	
	/**
	 * Get the parameter map.
	 * @return Parameter map.
	 * @since 1.0
	 */
	public ConcurrentMap<String, Object> getMap();
}
