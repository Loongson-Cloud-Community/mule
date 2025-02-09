/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.dsl.model.internal.config;

import static java.lang.Thread.currentThread;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mule.test.allure.AllureConstants.ConfigurationProperties.CONFIGURATION_PROPERTIES;
import static org.mule.test.allure.AllureConstants.ConfigurationProperties.ComponentConfigurationAttributesStory.COMPONENT_CONFIGURATION_YAML_STORY;

import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.properties.api.ResourceProvider;
import org.mule.runtime.properties.api.DefaultConfigurationPropertiesProvider;
import org.mule.runtime.config.internal.model.dsl.ClassLoaderResourceProvider;
import org.mule.runtime.properties.internal.ConfigurationPropertiesException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.yaml.snakeyaml.parser.ParserException;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(CONFIGURATION_PROPERTIES)
@Story(COMPONENT_CONFIGURATION_YAML_STORY)
public class YamlConfigurationPropertiesComponentTestCase {

  @Rule
  public ExpectedException expectedException = none();

  private ResourceProvider externalResourceProvider;
  private DefaultConfigurationPropertiesProvider configurationComponent;

  @Before
  public void setUp() {
    externalResourceProvider = new ClassLoaderResourceProvider(currentThread().getContextClassLoader());
  }

  @Description("Validates the values obtained for the different types in the properties")
  @Test
  public void validConfig() throws InitialisationException {
    configurationComponent = new DefaultConfigurationPropertiesProvider("properties.yaml", externalResourceProvider);
    configurationComponent.initialise();
    assertThat(configurationComponent.provide("number").get().getValue(), is("34843"));
    assertThat(configurationComponent.provide("float").get().getValue(), is("2392.00"));
    assertThat(configurationComponent.provide("date").get().getValue(), is("2001-01-23"));
    assertThat(configurationComponent.provide("lines").get().getValue(), is("458 Walkman Dr.\nSuite #292\n"));
    assertThat(configurationComponent.provide("list").get().getValue(), is("one,two,three"));
    assertThat(configurationComponent.provide("complex.prop0").get().getValue(), is("value0"));
    assertThat(configurationComponent.provide("complex.complex2.prop1").get().getValue(), is("value1"));
    assertThat(configurationComponent.provide("complex.complex2.prop2").get().getValue(), is("value2"));
  }

  @Description("Validates that a list of complex objects is not valid")
  @Test
  public void complexListOfObjectsNotSuported() throws InitialisationException {
    configurationComponent = new DefaultConfigurationPropertiesProvider("complex-list-object.yaml", externalResourceProvider);
    expectedException
        .expectMessage("Configuration properties does not support type a list of complex types. Complex type keys are: complex");
    expectedException.expect(ConfigurationPropertiesException.class);
    configurationComponent.initialise();
  }


  @Description("Validates that a only string types are supported")
  @Test
  public void unsupportedType() throws InitialisationException {
    configurationComponent = new DefaultConfigurationPropertiesProvider("unsupported-type.yaml", externalResourceProvider);
    expectedException
        .expectMessage("YAML configuration properties only supports string values, make sure to wrap the value with \" so you force the value to be an string. Offending property is integer with value 1235");
    expectedException.expect(ConfigurationPropertiesException.class);
    configurationComponent.initialise();
  }

  @Description("Validates encoding")
  @Test
  public void validConfigEncoding() throws InitialisationException {
    configurationComponent =
        new DefaultConfigurationPropertiesProvider("properties-utf16.yaml", "UTF-16", externalResourceProvider);
    configurationComponent.initialise();
    assertThat(configurationComponent.provide("number").get().getValue(), is("34843"));
  }


  @Description("YAML config files need spaces after key definition")
  @Test
  public void spacesRequired() throws InitialisationException {
    configurationComponent = new DefaultConfigurationPropertiesProvider("spaces.yaml", externalResourceProvider);
    expectedException
        .expectMessage("YAML configuration properties must have space after ':' character. Offending line is: prop1:\"v1\" prop2:\"v2\"");
    expectedException.expect(ConfigurationPropertiesException.class);
    configurationComponent.initialise();
  }

  @Description("YAML config files need to have correct number of quotes")
  @Test
  public void quotesRequired() throws InitialisationException {
    configurationComponent = new DefaultConfigurationPropertiesProvider("quotes.yaml", externalResourceProvider);
    expectedException
        .expectMessage("Error while parsing YAML configuration file. Check that all quotes are correctly closed.");
    expectedException.expect(ConfigurationPropertiesException.class);
    expectedException.expectCause(instanceOf(ParserException.class));
    configurationComponent.initialise();
  }

}
