/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension.metadata;

import static org.mule.runtime.api.component.location.Location.builder;
import static org.mule.runtime.api.metadata.resolving.FailureCode.INVALID_CONFIGURATION;
import static org.mule.runtime.api.metadata.resolving.MetadataComponent.COMPONENT;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.descriptor.ComponentMetadataDescriptor;
import org.mule.runtime.api.metadata.resolving.MetadataResult;

import org.junit.Test;

public class MetadataImplicitDynamicConfigurationTestCase extends AbstractMetadataOperationTestCase {

  public MetadataImplicitDynamicConfigurationTestCase(ResolutionType resolutionType) {
    super(resolutionType);
  }

  @Override
  protected String getConfigFile() {
    return METADATA_TEST_DYNAMIC_IMPLICIT_CONFIGURATION;
  }

  @Test
  public void resolveMetadataWithImplicitDynamicConfig() throws Exception {
    location = builder().globalName(CONTENT_METADATA_WITH_KEY_ID).addProcessorsPart().addIndexPart(0).build();
    final MetadataResult<ComponentMetadataDescriptor<OperationModel>> result = getComponentDynamicMetadata(PERSON_METADATA_KEY);
    assertFailureResult(result, 1);
    assertMetadataFailure(result.getFailures().get(0),
                          "Configuration used for Metadata fetch cannot be dynamic",
                          INVALID_CONFIGURATION,
                          MetadataResolvingException.class.getName(),
                          COMPONENT,
                          "");
  }
}
