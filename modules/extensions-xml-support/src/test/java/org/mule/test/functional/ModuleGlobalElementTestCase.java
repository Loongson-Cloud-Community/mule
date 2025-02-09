/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.functional;


import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mule.runtime.core.api.error.Errors.CORE_NAMESPACE_NAME;
import static org.mule.runtime.core.api.error.Errors.Identifiers.UNKNOWN_ERROR_IDENTIFIER;
import static org.mule.tck.junit4.matcher.ErrorTypeMatcher.errorType;

import org.mule.test.petstore.extension.PetStoreClient;
import org.mule.test.runner.RunnerDelegateTo;

import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class ModuleGlobalElementTestCase extends AbstractCeXmlExtensionMuleArtifactFunctionalTestCase {

  @Parameterized.Parameter
  public String configFile;

  @Parameterized.Parameter(1)
  public String[] paths;

  @Parameterized.Parameters(name = "{index}: Running tests for {0} ")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        // simple scenario consuming a global element
        {"flows/flows-using-module-global-element.xml", new String[] {"modules/module-global-element.xml"}},
        // default macro expansion global elements without an explicit 'config' element (module doesn't export properties)
        {"flows/flows-using-module-global-element-default.xml", new String[] {"modules/module-global-element-default.xml"}},
        // config with all default elements is still required
        {"flows/flows-using-module-global-element-default-params.xml",
            new String[] {"modules/module-global-element-default-params.xml"}}
    });
  }

  @Override
  protected String[] getModulePaths() {
    return paths;
  }

  @Override
  protected String getConfigFile() {
    return configFile;
  }

  @Test
  public void testDoGetClient() throws java.lang.Exception {
    assertGetClient("testDoGetClient");
  }

  @Test
  public void testDoGetClientWithPrivateOperation() throws java.lang.Exception {
    assertGetClient("testDoGetClientWithPrivateOperation");
  }

  @Test
  public void testDoGetPets() throws Exception {
    Collection<String> pets = (Collection<String>) flowRunner("testDoGetPets")
        .withVariable("ownerTest", "john")
        .run().getMessage().getPayload().getValue();
    assertThat(pets, containsInAnyOrder("la tota", "la porota"));
  }

  @Test
  public void testDoGetPetsFailWrongOwnerThrowsException() throws Exception {
    flowRunner("testDoGetPets")
        .withVariable("ownerTest", "notJohn")
        .runExpectingException(errorType(CORE_NAMESPACE_NAME, UNKNOWN_ERROR_IDENTIFIER));
  }

  private void assertGetClient(String flow) throws Exception {
    PetStoreClient client = (PetStoreClient) flowRunner(flow)
        .run().getMessage().getPayload().getValue();
    assertThat(client.getUsername(), is("john"));
    assertThat(client.getPassword(), is("notDoe"));
  }

  @Override
  public boolean mustRegenerateComponentBuildingDefinitionRegistryFactory() {
    // returns true because not same extensions are loaded by all tests.
    // returning false will fails while creating application context on some tests.
    return true;
  }

}
