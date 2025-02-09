/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.api.plugin;

import static org.mule.runtime.api.util.Preconditions.checkArgument;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.mule.api.annotation.NoExtend;
import org.mule.api.annotation.NoInstantiate;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic descriptor that will be used to describe parameterization to construct {@link ExtensionModel}, {@link ClassLoader} and
 * any other descriptor that may arise in a future of {@link ArtifactPluginDescriptor}.
 * <p/>
 * Each {@link LoaderDescriber} will have an ID that will be used to discover any loader that's responsible of working with the
 * current set of attributes. It's up to each loader to validate the types, size and all that matters around the attributes.
 *
 * @since 4.5
 */
@NoInstantiate
@NoExtend
public class LoaderDescriber {

  private final String id;
  private final Map<String, Object> attributes = new HashMap<>();

  /**
   * Creates an immutable implementation of {@link LoaderDescriber}
   *
   * @param id ID of the descriptor. Not blank nor null.
   */
  public LoaderDescriber(String id) {
    checkArgument(!isEmpty(id), "id must not be null");
    this.id = id;
  }

  /**
   * @return descriptor's ID that will be used to discover any object that matches with this ID.
   */
  public String getId() {
    return id;
  }

  /**
   * @return attributes that will be feed to the loader found through {@link #getId()}, where it's up to the loader's
   *         responsibilities to determine if the current structure of the values ({@link Object}) does match with the expected
   *         types.
   *         <p/>
   *         That implies each loader must evaluate on the attributes' values to be 100% sure it were formed correctly.
   */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /**
   * Stores all the entries of {@code attributes} under in the {@code attributes} internal state.
   *
   * @param attributes mappings to be stored in this map
   */
  public void addAttributes(Map<String, Object> attributes) {
    attributes.forEach(this::addAttribute);
  }

  /**
   * Stores the {@code value} under the {@key} in the {@code attributes} internal state.
   *
   * @param key   key with which the specified value is to be associated. Non blank nor null.
   * @param value value to be associated with the specified key
   */
  private void addAttribute(String key, Object value) {
    checkArgument(!isEmpty(key), "key must not be null");
    attributes.put(key, value);
  }
}
