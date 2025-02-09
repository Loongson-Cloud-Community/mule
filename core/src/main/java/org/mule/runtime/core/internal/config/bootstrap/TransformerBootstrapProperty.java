/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.config.bootstrap;

import static org.mule.runtime.api.util.Preconditions.checkArgument;

import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.core.api.config.bootstrap.BootstrapService;
import org.mule.runtime.core.api.util.StringUtils;

import java.util.Set;

/**
 * Defines a bootstrap property for a transformer
 */
public class TransformerBootstrapProperty extends AbstractBootstrapProperty {

  private final String name;
  private final String className;
  private final String mimeType;
  private final String returnClassName;

  /**
   * Creates a bootstrap property
   *
   * @param service         service that provides the property. Not null.
   * @param artifactTypes   defines what is the artifact this bootstrap object applies to
   * @param optional        indicates whether or not the bootstrapped transformer is optional. When a bootstrap object is
   *                        optional, any error creating it will be ignored.
   * @param name            name assigned to the transformer. Can be null.
   * @param className       className of the bootstrapped transformer. Not empty.
   * @param returnClassName name of the transformer return class. Can be null.
   * @param mimeType        transformer returned mimeType. Can be null
   */
  public TransformerBootstrapProperty(BootstrapService service, Set<ArtifactType> artifactTypes, boolean optional, String name,
                                      String className, String returnClassName, String mimeType) {
    super(service, artifactTypes, optional);
    checkArgument(!StringUtils.isEmpty(className), "className cannot be empty");

    this.name = name;
    this.className = className;
    this.mimeType = mimeType;
    this.returnClassName = returnClassName;
  }

  public String getName() {
    return name;
  }

  public String getClassName() {
    return className;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getReturnClassName() {
    return returnClassName;
  }

  @Override
  public String toString() {
    return String.format("Transformer{ %s}", className);
  }
}
