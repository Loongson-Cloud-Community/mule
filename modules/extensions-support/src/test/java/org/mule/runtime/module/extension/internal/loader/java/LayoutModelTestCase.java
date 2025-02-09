/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.java;

import static java.util.Optional.ofNullable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mule.test.heisenberg.extension.HeisenbergExtension.PERSONAL_INFORMATION_GROUP_NAME;
import static org.mule.test.heisenberg.extension.HeisenbergExtension.RICIN_GROUP_NAME;
import static org.mule.test.heisenberg.extension.HeisenbergOperations.KILL_WITH_GROUP;

import org.mule.runtime.api.meta.model.declaration.fluent.ConfigurationDeclaration;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import org.mule.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclaration;
import org.mule.runtime.api.meta.model.display.LayoutModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Text;
import org.mule.runtime.extension.api.exception.IllegalParameterModelDefinitionException;
import org.mule.tck.size.SmallTest;
import org.mule.test.heisenberg.extension.HeisenbergExtension;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public class LayoutModelTestCase extends AbstractJavaExtensionDeclarationTestCase {

  private static final String KILL_CUSTOM_OPERATION = "killWithCustomMessage";

  @Before
  public void setUp() {
    setDeclarer(declarerFor(HeisenbergExtension.class));
  }

  @Test
  public void parseLayoutAnnotationsOnParameter() {
    ExtensionDeclarer declarer = declareExtension();
    ExtensionDeclaration extensionDeclaration = declarer.getDeclaration();
    final ConfigurationDeclaration configurationDeclaration = extensionDeclaration.getConfigurations().get(0);

    assertParameterPlacement(findParameterInGroup(configurationDeclaration, "labeledRicin"), RICIN_GROUP_NAME, 1);
    assertParameterPlacement(findParameterInGroup(configurationDeclaration, "ricinPacks"), RICIN_GROUP_NAME, 2);
  }

  @Test
  public void parseLayoutAnnotationsOnParameterGroup() {
    ExtensionDeclarer declarer = declareExtension();
    ExtensionDeclaration extensionDeclaration = declarer.getDeclaration();
    ConfigurationDeclaration config = extensionDeclaration.getConfigurations().get(0);

    assertParameterPlacement(findParameterInGroup(config, "myName"), PERSONAL_INFORMATION_GROUP_NAME, 1);
    assertParameterPlacement(findParameterInGroup(config, "age"), PERSONAL_INFORMATION_GROUP_NAME, 2);
    assertParameterPlacement(findParameterInGroup(config, "dateOfConception"), PERSONAL_INFORMATION_GROUP_NAME, 3);
    assertParameterPlacement(findParameterInGroup(config, "dateOfBirth"), PERSONAL_INFORMATION_GROUP_NAME, 4);
    assertParameterPlacement(findParameterInGroup(config, "dateOfDeath"), PERSONAL_INFORMATION_GROUP_NAME, 5);
  }

  @Test
  public void parseLayoutAnnotationsOnOperationParameter() {
    ExtensionDeclarer declarer = declareExtension();
    ExtensionDeclaration extensionDeclaration = declarer.getDeclaration();
    OperationDeclaration operation = getOperation(extensionDeclaration, KILL_CUSTOM_OPERATION);

    assertThat(operation, is(notNullValue()));

    assertParameterPlacement(findParameterInGroup(operation, "victim"), KILL_WITH_GROUP, 1);
    assertParameterPlacement(findParameterInGroup(operation, "goodbyeMessage"), KILL_WITH_GROUP, 2);
  }

  @Test(expected = IllegalParameterModelDefinitionException.class)
  public void parseLegacyAndSdkPlacementAnnotationsOnParameter() {
    declarerFor(ExtensionWithInvalidUseOfPlacementAnnotation.class, "1.0.0-dev");
  }

  @Test(expected = IllegalParameterModelDefinitionException.class)
  public void parseLegacyAndSdkTextAnnotationsOnParameter() {
    declarerFor(ExtensionWithInvalidUseOfPlacementAnnotation.class, "1.0.0-dev");
  }

  private void assertParameterPlacement(Pair<ParameterGroupDeclaration, ParameterDeclaration> pair, String groupName,
                                        Integer order) {
    ParameterGroupDeclaration group = pair.getFirst();
    assertThat(group.getName(), equalTo(groupName));

    assertParameterPlacement(pair.getSecond(), order);
  }

  private void assertParameterPlacement(ParameterDeclaration param, Integer order) {
    LayoutModel layout = param.getLayoutModel();
    assertThat(layout, is(notNullValue()));

    assertThat(layout.getOrder(), equalTo(ofNullable(order)));
  }

  @Extension(name = "extensionWithInvalidUseOfPlacementAnnotation")
  public static class ExtensionWithInvalidUseOfPlacementAnnotation {

    @Parameter
    @Placement(order = 1, tab = "Advance")
    @org.mule.sdk.api.annotation.param.display.Placement(order = 1, tab = "Advance")
    public String firstParameter;
  }

  @Extension(name = "extensionWithInvalidUseOfTextAnnotation")
  public static class ExtensionWithInvalidUseOfTextAnnotation {

    @Parameter
    @Text
    @org.mule.sdk.api.annotation.param.display.Text()
    public String firstParameter;
  }

}
