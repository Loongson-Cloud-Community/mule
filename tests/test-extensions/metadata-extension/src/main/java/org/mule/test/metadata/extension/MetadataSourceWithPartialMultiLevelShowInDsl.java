/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.metadata.extension;

import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.AMERICA;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.ARGENTINA;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.BUENOS_AIRES;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.tck.message.StringAttributes;
import org.mule.test.metadata.extension.resolver.TestOutputAnyTypeResolver;
import org.mule.test.metadata.extension.resolver.TestPartialMultiLevelKeyResolver;

import java.util.Map;

@MetadataScope(keysResolver = TestPartialMultiLevelKeyResolver.class,
    outputResolver = TestOutputAnyTypeResolver.class)
public class MetadataSourceWithPartialMultiLevelShowInDsl extends Source<Map<String, Object>, StringAttributes> {

  private static final String ERROR_MESSAGE = "LocationKey field was not injected properly";

  @MetadataKeyId
  @ParameterGroup(name = "Location", showInDsl = true)
  public LocationKey key;

  @Override
  public void onStart(SourceCallback<Map<String, Object>, StringAttributes> sourceCallback) throws MuleException {
    boolean injectedProperly = key != null && key.getCity().equals(BUENOS_AIRES) && key.getCountry().equals(ARGENTINA)
        && key.getContinent().equals(AMERICA);

    if (!injectedProperly) {
      throw new RuntimeException(ERROR_MESSAGE);
    }
  }

  @Override
  public void onStop() {

  }
}
