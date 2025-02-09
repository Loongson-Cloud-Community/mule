/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.validation;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static org.mule.runtime.api.meta.ExpressionSupport.REQUIRED;
import static org.mule.runtime.api.meta.ExpressionSupport.SUPPORTED;
import static org.mule.runtime.api.meta.model.parameter.ParameterRole.BEHAVIOUR;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.mockParameters;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.mockSubTypes;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.toMetadataType;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.validate;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.ParameterDslConfiguration;
import org.mule.runtime.api.meta.model.XmlDslModel;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.extension.api.connectivity.oauth.OAuthParameterModelProperty;
import org.mule.runtime.extension.api.exception.IllegalModelDefinitionException;
import org.mule.runtime.extension.api.property.InfrastructureParameterModelProperty;
import org.mule.runtime.extension.api.property.QNameModelProperty;
import org.mule.runtime.module.extension.internal.loader.validator.ParameterPluralNameModelValidator;
import org.mule.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ParameterPluralNameModelValidatorTestCase extends AbstractMuleTestCase {

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  private ExtensionModel extensionModel;

  @Mock(lenient = true)
  private OperationModel operationModel;

  @Mock(lenient = true)
  private ParameterModel validParameterModel;

  @Mock(lenient = true)
  private ParameterModel invalidParameterModel;

  private ParameterPluralNameModelValidator validator = new ParameterPluralNameModelValidator();

  @Before
  public void before() {
    when(extensionModel.getOperationModels()).thenReturn(asList(operationModel));
    mockSubTypes(extensionModel);
    when(extensionModel.getName()).thenReturn("extensionModel");
    when(extensionModel.getImportedTypes()).thenReturn(emptySet());
    when(extensionModel.getXmlDslModel())
        .thenReturn(XmlDslModel.builder().setPrefix("ns").setNamespace("http://www.mulesoft.org/schema/mule/ns")
            .setSchemaLocation("http://www.mulesoft.org/schema/mule/heisenberg/current/mule-ns.xsd").build());

    when(operationModel.getName()).thenReturn("dummyOperation");

    when(validParameterModel.getModelProperty(ParameterGroupModelProperty.class)).thenReturn(Optional.empty());
    when(validParameterModel.getModelProperty(QNameModelProperty.class)).thenReturn(Optional.empty());
    when(validParameterModel.getModelProperty(InfrastructureParameterModelProperty.class)).thenReturn(Optional.empty());
    when(validParameterModel.getModelProperty(OAuthParameterModelProperty.class)).thenReturn(Optional.empty());
    when(validParameterModel.getDslConfiguration()).thenReturn(ParameterDslConfiguration.getDefaultInstance());
    when(validParameterModel.getRole()).thenReturn(BEHAVIOUR);
    when(validParameterModel.getLayoutModel()).thenReturn(Optional.empty());

    when(invalidParameterModel.getModelProperty(ParameterGroupModelProperty.class)).thenReturn(Optional.empty());
    when(invalidParameterModel.getModelProperty(QNameModelProperty.class)).thenReturn(Optional.empty());
    when(invalidParameterModel.getModelProperty(InfrastructureParameterModelProperty.class)).thenReturn(Optional.empty());
    when(invalidParameterModel.getModelProperty(OAuthParameterModelProperty.class)).thenReturn(Optional.empty());
    when(invalidParameterModel.getDslConfiguration()).thenReturn(ParameterDslConfiguration.getDefaultInstance());
    when(invalidParameterModel.getRole()).thenReturn(BEHAVIOUR);
    when(invalidParameterModel.getLayoutModel()).thenReturn(Optional.empty());
    when(invalidParameterModel.getExpressionSupport()).thenReturn(SUPPORTED);
  }

  @Test(expected = IllegalModelDefinitionException.class)
  public void invalidModelDueToListWithoutPluralName() {
    when(invalidParameterModel.getType()).thenReturn(toMetadataType(List.class));
    when(invalidParameterModel.getName()).thenReturn("thing");
    mockParameters(operationModel, invalidParameterModel);
    validate(extensionModel, validator);
  }

  public void isValidNameIfDoesntSupportChildElement() {
    when(invalidParameterModel.getType()).thenReturn(toMetadataType(List.class));
    when(invalidParameterModel.getName()).thenReturn("thing");
    when(invalidParameterModel.getDslConfiguration()).thenReturn(ParameterDslConfiguration.builder()
        .allowsInlineDefinition(false)
        .allowTopLevelDefinition(false)
        .build());
    mockParameters(operationModel, invalidParameterModel);
    validate(extensionModel, validator);
  }

  public void isValidNameIfRequiresExpression() {
    when(invalidParameterModel.getType()).thenReturn(toMetadataType(List.class));
    when(invalidParameterModel.getName()).thenReturn("thing");
    when(invalidParameterModel.getExpressionSupport()).thenReturn(REQUIRED);
    mockParameters(operationModel, invalidParameterModel);
    validate(extensionModel, validator);
  }

}
