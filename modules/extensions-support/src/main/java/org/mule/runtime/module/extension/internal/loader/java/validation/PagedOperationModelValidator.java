/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.java.validation;

import static java.lang.String.format;
import static org.mule.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserUtils.getConnectionParameter;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.meta.model.util.IdempotentExtensionWalker;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.loader.ExtensionModelValidator;
import org.mule.runtime.extension.api.loader.Problem;
import org.mule.runtime.extension.api.loader.ProblemsReporter;
import org.mule.runtime.extension.internal.property.PagedOperationModelProperty;
import org.mule.runtime.module.extension.internal.loader.java.type.property.ExtensionOperationDescriptorModelProperty;

/**
 * Validates that all the paged {@link OperationModel operations} don't receive a {@link Connection} parameter.
 *
 * @since 4.0
 */
public class PagedOperationModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {
    new IdempotentExtensionWalker() {

      @Override
      protected void onOperation(OperationModel operationModel) {
        boolean hasConnectionParameter = operationModel.getModelProperty(ExtensionOperationDescriptorModelProperty.class)
            .map(property -> getConnectionParameter(property.getOperationElement()).isPresent())
            .orElse(false);
        if (hasConnectionParameter && operationModel.getModelProperty(PagedOperationModelProperty.class).isPresent()) {
          problemsReporter.addError(new Problem(operationModel, format(
                                                                       "Operation '%s' in Extension '%s' is paged and has a parameter annotated with '%s' at the same time. Paged operation shouldn't have a connection parameter.",
                                                                       operationModel.getName(), extensionModel.getName(),
                                                                       Connection.class.getName())));
        }
      }
    }.walk(extensionModel);
  }
}
