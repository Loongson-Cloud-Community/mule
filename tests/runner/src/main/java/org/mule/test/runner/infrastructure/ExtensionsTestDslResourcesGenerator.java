/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.runner.infrastructure;

import static java.util.Collections.emptyList;
import static org.mule.runtime.api.util.collection.Collectors.toImmutableList;
import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.extension.api.dsl.syntax.resources.spi.DslResourceFactory;
import org.mule.runtime.extension.api.resources.GeneratedResource;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * This class extends {@link ExtensionsTestLoaderResourcesGenerator} that writes the DSL generated resources to the specified
 * target directory but also exposes the content to be shared for testing purposes.
 *
 * @since 4.0
 */
class ExtensionsTestDslResourcesGenerator extends ExtensionsTestLoaderResourcesGenerator {

  private final List<DslResourceFactory> resourceFactories;
  private final DslResolvingContext context;

  ExtensionsTestDslResourcesGenerator(List<DslResourceFactory> resourceFactories, File generatedResourcesDirectory,
                                      DslResolvingContext context) {
    super(emptyList(), generatedResourcesDirectory);
    this.resourceFactories = ImmutableList.copyOf(resourceFactories);
    this.context = context;
  }

  /**
   * {@inheritDoc}
   */
  public List<GeneratedResource> generateFor(ExtensionModel extensionModel) {
    List<GeneratedResource> resources =
        resourceFactories.stream().map(factory -> factory.generateResource(extensionModel, context))
            .filter(Optional::isPresent).map(Optional::get).collect(toImmutableList());

    resources.forEach(this::write);
    return resources;
  }
}
