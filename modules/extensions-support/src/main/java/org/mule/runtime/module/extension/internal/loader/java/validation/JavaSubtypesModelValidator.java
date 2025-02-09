/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.java.validation;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.mule.runtime.extension.api.util.ExtensionModelUtils.toSubTypesMap;
import static org.mule.runtime.module.extension.internal.util.IntrospectionUtils.isInstantiable;

import org.mule.metadata.api.model.ObjectType;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.extension.api.loader.ExtensionModelValidator;
import org.mule.runtime.extension.api.loader.Problem;
import org.mule.runtime.extension.api.loader.ProblemsReporter;
import org.mule.runtime.extension.api.util.ExtensionMetadataTypeUtils;
import org.mule.runtime.module.extension.internal.util.ReflectionCache;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ExtensionModelValidator} which applies to {@link ExtensionModel}s.
 * <p>
 * This validator checks that all of the extension's {@link ExtensionModel#getSubTypes()} are instantiable
 *
 * @since 4.0
 */
public final class JavaSubtypesModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel model, ProblemsReporter problemsReporter) {
    final Map<ObjectType, Set<ObjectType>> typesMapping = toSubTypesMap(model.getSubTypes());
    validateNonAbstractSubtypes(model, typesMapping, problemsReporter, new ReflectionCache());
  }

  private void validateNonAbstractSubtypes(ExtensionModel model, Map<ObjectType, Set<ObjectType>> typesMapping,
                                           ProblemsReporter problemsReporter, ReflectionCache reflectionCache) {
    List<String> abstractSubtypes = new LinkedList<>();
    for (Set<ObjectType> subtypes : typesMapping.values()) {
      abstractSubtypes.addAll(subtypes.stream()
          .filter(s -> !isInstantiable(s, reflectionCache))
          .map(ExtensionMetadataTypeUtils::getId)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(toList()));
    }

    if (!abstractSubtypes.isEmpty()) {
      problemsReporter.addError(new Problem(model, format(
                                                          "All the declared Subtypes in extension %s should be of concrete types, but [%s] are non instantiable",
                                                          model.getName(), Arrays.toString(abstractSubtypes.toArray()))));
    }
  }
}
