/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package eu.drus.jpa.unit.neo4j.dataset.graphml;

public class DefaultAttribute<T> implements Attribute {

    private AttributeType type;
    private T value;

    public DefaultAttribute(final T value, final AttributeType type) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String getValue() {
        return value.toString();
    }

    @Override
    public AttributeType getType() {
        return type;
    }
}