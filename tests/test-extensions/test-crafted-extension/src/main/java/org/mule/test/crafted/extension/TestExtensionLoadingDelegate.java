/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.crafted.extension;

import static org.mule.runtime.api.meta.Category.COMMUNITY;

import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.loader.ExtensionLoadingDelegate;

public class TestExtensionLoadingDelegate implements ExtensionLoadingDelegate {

  private static final String EXTENSION_NAME = "crafted-extension";

  @Override
  public void accept(ExtensionDeclarer extensionDeclarer, ExtensionLoadingContext context) {
    extensionDeclarer.named(EXTENSION_NAME)
        .describedAs("Crafted Extension")
        .onVersion("1.0.0")
        .withCategory(COMMUNITY)
        .fromVendor("Mulesoft");
  }
}
