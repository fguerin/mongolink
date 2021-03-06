/*
 * MongoLink, Object Document Mapper for Java and MongoDB
 *
 * Copyright (c) 2012, Arpinum or third-party contributors as
 * indicated by the @author tags
 *
 * MongoLink is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MongoLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with MongoLink.  If not, see <http://www.gnu.org/licenses/>. 
 *
 */

package org.mongolink.domain;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.mongodb.DBObject;
import org.mongolink.MongoSession;

import java.util.Map;


public class UnitOfWork {

    public UnitOfWork(MongoSession session) {
        this.session = session;
    }

    public void add(Object id, Object entity, DBObject initialValue) {
        values.put(new Key(entity.getClass(), id), new Value(entity, initialValue));
    }

    public void flush() {
        for (Value value : values.values()) {
            session.update(value.entity);
        }
        clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T getEntity(Class<?> type, Object dbId) {
        return (T) getValue(type, dbId).entity;
    }

    public DBObject getDBOBject(Class<?> type, Object dbId) {
        return getValue(type, dbId).initialValue;
    }

    public boolean contains(Class<?> type, Object dbId) {
        if (type != null && dbId != null) {
            final Key craftedKey = new Key(type, dbId);
            for (Key key : values.keySet()) {
                if (key.equals(craftedKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Value getValue(Class<?> type, Object dbId) {
        Key craftedKey = new Key(type, dbId);
        for (Key key : values.keySet()) {
            if (key.equals(craftedKey)) {
                return values.get(key);
            }
        }
        return values.get(craftedKey);
    }

    public void update(Object id, Object element, DBObject update) {
        values.put(new Key(element.getClass(), id), new Value(element, update));
    }

    public void clear() {
        values.clear();
    }

    public void delete(Object id, Object element) {
        values.remove(new Key(element.getClass(), id));
    }

    private class Value {

        private Value(Object entity, DBObject initialValue) {
            this.entity = entity;
            this.initialValue = initialValue;
        }

        final Object entity;
        final DBObject initialValue;
    }

    private class Key {

        private Key(Class<?> type, Object id) {
            this.type = type;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            Key other = (Key) o;
            return other.type.isAssignableFrom(type) && Objects.equal(id, other.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(type, id);
        }

        final Class<?> type;

        final Object id;
    }

    private final MongoSession session;

    private final Map<Key, Value> values = Maps.newHashMap();

}
