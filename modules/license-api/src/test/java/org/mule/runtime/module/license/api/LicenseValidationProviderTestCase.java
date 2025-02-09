/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.license.api;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mule.runtime.module.license.api.LicenseValidatorProvider.discoverLicenseValidator;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LicenseValidationProviderTestCase extends AbstractMuleTestCase {

  @Rule
  public ExpectedException expectedException = none();

  @Test
  public void anyPluginValidationFails() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    LicenseValidator licenseValidator = discoverLicenseValidator(classLoader);
    assertThat(licenseValidator, notNullValue());
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("Mule Runtime CE cannot run a licensed connector");
    licenseValidator.validatePluginLicense(PluginLicenseValidationRequest.builder()
        .withArtifactClassLoader(classLoader)
        .withPluginClassLoader(classLoader)
        .withEntitlement("entitlement")
        .withPluginName("plugin")
        .withPluginProvider("provider")
        .withPluginVersion("1.0.0")
        .build());
  }

}
