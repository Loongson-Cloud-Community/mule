/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.java.validation;

import static java.lang.String.format;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.parameter.ParameterGroupModel;
import org.mule.runtime.api.meta.model.parameter.ParameterizedModel;
import org.mule.runtime.api.meta.model.util.ExtensionWalker;
import org.mule.runtime.extension.api.loader.ExtensionModelValidator;
import org.mule.runtime.extension.api.loader.Problem;
import org.mule.runtime.extension.api.loader.ProblemsReporter;
import org.mule.runtime.module.extension.internal.loader.java.property.CompileTimeModelProperty;
import org.mule.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;

/**
 * Validates that the classes through which parameter groups are implemented are valid
 * </ul>
 *
 * @since 4.0
 */
public final class ParameterGroupModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {
    boolean isCompileTime = extensionModel.getModelProperty(CompileTimeModelProperty.class).isPresent();
    new ExtensionWalker() {

      @Override
      protected void onParameterGroup(ParameterizedModel owner, ParameterGroupModel model) {
        validateIsInstantiable(model, problemsReporter);
        if (isCompileTime) {
          validateNonEmptyGroup(model, problemsReporter);
        }
      }
    }.walk(extensionModel);
  }

  private void validateIsInstantiable(ParameterGroupModel groupModel, ProblemsReporter problemsReporter) {
    groupModel.getModelProperty(ParameterGroupModelProperty.class).map(ParameterGroupModelProperty::getDescriptor)
        .ifPresent(group -> {
          if (!group.getType().isInstantiable()) {
            problemsReporter
                .addError(new Problem(groupModel,
                                      format("The parameter group of type '%s' should be non abstract with a default constructor.",
                                             group.getType().getTypeName())));
          }
        });
  }

  private void validateNonEmptyGroup(ParameterGroupModel groupModel, ProblemsReporter problemsReporter) {
    if (groupModel.getParameterModels().isEmpty()) {
      problemsReporter.addError(new Problem(groupModel, "ParameterGroups cannot be empty. "
          + "At least one user-facing parameter should be declared, but none was found."));
    }
  }
}
