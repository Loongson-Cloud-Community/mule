/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.util;

import static java.util.Collections.unmodifiableMap;

import org.mule.api.annotation.NoExtend;
import org.mule.runtime.api.util.CaseInsensitiveMapWrapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A case-insensitive <code>Map</code>.
 * <p/>
 * As entries are added to the map, keys hash values are lowercase hash codes of the key. the Real key case is preserved.
 * <p/>
 * <p/>
 * The <code>keySet()</code> method returns all keys in their original case
 * <p/>
 * <strong>Note that CaseInsensitiveMap is not synchronized and is not thread-safe.</strong> If you wish to use this map from
 * multiple threads concurrently, you must use appropriate synchronization. The simplest approach is to wrap this map using
 * {@link java.util.Collections#synchronizedMap(Map)}. This class may throw exceptions when accessed by concurrent threads without
 * synchronization.
 *
 * @since 3.0.0
 */
@NoExtend
public class CaseInsensitiveHashMap<K, V> implements Map<K, V>, Serializable {

  /**
   * Serialisation version
   */
  private static final long serialVersionUID = -7074633917369299456L;

  @SuppressWarnings("rawtypes")
  private static final CaseInsensitiveHashMap EMPTY_MAP = new CaseInsensitiveHashMap<>().toImmutableCaseInsensitiveMap();

  /**
   * Returns an empty CaseInsensitiveHashMap (immutable). This map is serializable.
   *
   * <p>
   * This example illustrates the type-safe way to obtain an empty map:
   *
   * <pre>
   *
   * CaseInsensitiveHashMap&lt;String, String&gt; s = CaseInsensitiveHashMap.emptyCaseInsensitiveMap();
   * </pre>
   *
   * @param <K> the class of the map keys
   * @param <V> the class of the map values
   * @return an empty multi-map
   * @since 1.1.1
   */
  @SuppressWarnings("unchecked")
  public static <K, V> CaseInsensitiveHashMap<K, V> emptyCaseInsensitiveMap() {
    return EMPTY_MAP;
  }

  /**
   * Creates a new instance which is backed by the given {@code map}. Said map is required to be empty
   *
   * @param map the backing map
   * @param <K> the generic type of the key
   * @param <V> the generic type of the value
   * @return a new instance
   * @throws IllegalArgumentException if {@code map} is not empty
   * @since 4.3.0
   */
  public static <K, V> CaseInsensitiveHashMap<K, V> basedOn(Map<K, V> map) {
    return new CaseInsensitiveHashMap<>(new CaseInsensitiveMapWrapper(map));
  }

  protected Map<K, V> delegate;

  /**
   * Constructs a new empty map with default size and load factor.
   */
  public CaseInsensitiveHashMap() {
    delegate = new CaseInsensitiveMapWrapper();
  }

  /**
   * Constructor copying elements from another map.
   * <p/>
   * Keys will be converted to lower case strings, which may cause some entries to be removed (if string representation of keys
   * differ only by character case).
   *
   * @param map the map to copy
   * @throws NullPointerException if the map is null
   */
  public CaseInsensitiveHashMap(Map map) {
    delegate = new CaseInsensitiveMapWrapper();
    delegate.putAll(map);
  }

  private CaseInsensitiveHashMap(CaseInsensitiveMapWrapper delegate) {
    this.delegate = delegate;
  }

  // -----------------------------------------------------------------------

  /**
   * Clones the map without cloning the keys or values.
   *
   * @return a shallow clone
   */
  @Override
  public Object clone() {
    return new CaseInsensitiveHashMap<K, V>(delegate);
  }

  /**
   * Creates a shallow copy of this instance. This is the recommended way of creating copy instances as this is optimized and
   * usually much faster than using the {@link CaseInsensitiveHashMap#CaseInsensitiveHashMap(Map)} constructor.
   *
   * @return a shallow copy of {@code this} instance
   * @since 4.3.0
   */
  public CaseInsensitiveHashMap<K, V> copy() {
    return new CaseInsensitiveHashMap<>(((CaseInsensitiveMapWrapper) delegate).copy());
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return delegate.get(key);
  }

  @Override
  public V put(K key, V value) {
    return delegate.put(key, value);
  }

  @Override
  public V remove(Object key) {
    return delegate.remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> values) {
    delegate.putAll(values);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public Set<K> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<V> values() {
    return delegate.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return delegate.entrySet();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  /**
   * @return an immutable version of this map.
   * @since 4.1.5
   */
  public CaseInsensitiveHashMap<K, V> toImmutableCaseInsensitiveMap() {
    if (isEmpty() && EMPTY_MAP != null) {
      return EMPTY_MAP;
    }
    return new ImmutableCaseInsensitiveHashMap<>(this);
  }

  private static class ImmutableCaseInsensitiveHashMap<K, V> extends CaseInsensitiveHashMap<K, V> {

    private transient final CaseInsensitiveHashMap<K, V> originalMap;

    private ImmutableCaseInsensitiveHashMap(CaseInsensitiveHashMap<K, V> caseInsensitiveHashMap) {
      this.delegate = unmodifiableMap(caseInsensitiveHashMap);
      originalMap = caseInsensitiveHashMap;
    }

    @Override
    public CaseInsensitiveHashMap<K, V> toImmutableCaseInsensitiveMap() {
      return this;
    }

    @Override
    public CaseInsensitiveHashMap<K, V> copy() {
      return originalMap.copy();
    }
  }
}
