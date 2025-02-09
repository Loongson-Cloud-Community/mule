/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension.config;

import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.exception.IllegalModelDefinitionException;
import org.mule.runtime.extension.api.loader.ExtensionModelValidator;
import org.mule.runtime.module.extension.internal.loader.validator.JavaConfigurationModelValidator;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.test.module.extension.internal.util.ExtensionsTestUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NamelessConfigTestCase extends AbstractMuleTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ExtensionModelValidator validator = new JavaConfigurationModelValidator();

  @Test
  public void useDefaultConfigurationNameOnceAnnotated() throws Exception {
    validate(OneConfigAnnotatedExtension.class);
  }

  @Test
  public void useDefaultConfigurationNameTwiceAnnotated() throws Exception {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage("contains 2 components 'config' which it's transformed DSL name is 'config'");
    validate(TwoConfigAnnotatedExtension.class);
  }

  @Test
  public void useDefaultConfigurationNameOnceNonAnnotated() throws Exception {
    validate(OneConfigNonAnnotatedExtension.class);
  }

  @Test
  public void useDefaultConfigurationNameTwiceNonAnnotated() throws Exception {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage("contains 2 components 'config' which it's transformed DSL name is 'config'");
    validate(TwoConfigNonAnnotatedExtension.class);
  }

  @Test
  public void useDefaultConfigurationNameMixedAnnotation() throws Exception {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage("contains 2 components 'config' which it's transformed DSL name is 'config'");
    validate(MixedConfigExtension.class);
  }

  private void validate(Class<?> connectorClass) {
    ExtensionsTestUtils.validate(connectorClass, validator);
  }

  @Extension(name = "once-annotated")
  @Configurations({AnnotatedNamelessConfig.class})
  public static class OneConfigAnnotatedExtension {
  }

  @Extension(name = "twice-annotated")
  @Configurations({AnnotatedNamelessConfig.class, AnotherAnnotatedNamelessConfig.class})
  public static class TwoConfigAnnotatedExtension {
  }

  @Extension(name = "once-non-annotated")
  @Configurations({NonAnnotatedNamelessConfig.class})
  public static class OneConfigNonAnnotatedExtension {
  }

  @Extension(name = "twice-non-annotated")
  @Configurations({NonAnnotatedNamelessConfig.class, AnotherNonAnnotatedNamelessConfig.class})
  public static class TwoConfigNonAnnotatedExtension {
  }

  @Extension(name = "twice-mixed-annotated")
  @Configurations({AnnotatedNamelessConfig.class, NonAnnotatedNamelessConfig.class})
  public static class MixedConfigExtension {
  }

  @Configuration
  public static class AnnotatedNamelessConfig {
  }

  @Configuration
  public static class AnotherAnnotatedNamelessConfig {
  }

  public static class NonAnnotatedNamelessConfig {
  }

  public static class AnotherNonAnnotatedNamelessConfig {
  }

}
