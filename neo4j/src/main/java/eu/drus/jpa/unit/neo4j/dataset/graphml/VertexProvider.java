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

import java.util.Map;

/**
 * Creates a Vertex of type V
 *
 * @param <V>
 *            the vertex type
 */
public interface VertexProvider<V> {

    /**
     * Create a vertex
     *
     * @param label
     *            the label of the vertex
     * @param attributes
     *            any other attributes of the vertex
     *
     * @return the vertex
     */
    V buildVertex(String label, Map<String, Attribute> attributes);
}
