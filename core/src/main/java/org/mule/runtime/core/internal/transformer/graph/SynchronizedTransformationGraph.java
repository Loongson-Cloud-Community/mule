/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.transformer.graph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.api.transformer.Converter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SynchronizedTransformationGraph {

  private ReentrantReadWriteLock readWriteLock;
  private TransformationGraph transformationGraph;

  public SynchronizedTransformationGraph() {
    this.transformationGraph = new TransformationGraph();
    this.readWriteLock = new ReentrantReadWriteLock();
  }

  public void addConverter(Converter converter) {
    readWriteLock.writeLock().lock();
    try {
      transformationGraph.addConverter(converter);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  public void removeConverter(Converter converter) {
    readWriteLock.writeLock().lock();
    try {
      transformationGraph.removeConverter(converter);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  public Set<TransformationEdge> outgoingEdgesOf(DataType vertex) {
    readWriteLock.readLock().lock();
    try {
      return ImmutableSet.copyOf(transformationGraph.outgoingEdgesOf(vertex));
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public DataType getEdgeTarget(TransformationEdge transformationEdge) {
    readWriteLock.readLock().lock();
    try {
      return transformationGraph.getEdgeTarget(transformationEdge);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public boolean containsVertex(DataType dataType) {
    readWriteLock.readLock().lock();
    try {
      return transformationGraph.containsVertex(dataType);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }


  List<DataType> getSuperVertexes(DataType vertex) {
    readWriteLock.readLock().lock();
    try {
      return ImmutableList.copyOf(transformationGraph.getSuperVertexes(vertex));
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  List<DataType> getSubVertexes(DataType vertex) {
    readWriteLock.readLock().lock();
    try {
      return ImmutableList.copyOf(transformationGraph.getSubVertexes(vertex));
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  boolean containsVertexOrSuper(DataType vertex) {
    readWriteLock.readLock().lock();
    try {
      return transformationGraph.containsVertexOrSuper(vertex);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  boolean containsVertexOrSub(DataType vertex) {
    readWriteLock.readLock().lock();
    try {
      return transformationGraph.containsVertexOrSub(vertex);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

}
