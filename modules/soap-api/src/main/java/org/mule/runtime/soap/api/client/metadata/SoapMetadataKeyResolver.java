/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.soap.api.client.metadata;

import java.util.Set;

import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataResolvingException;

/**
 * An object that is in charge of resolving {@link SoapOperationMetadata} for different operations.
 *
 * @since 4.0
 */
public interface SoapMetadataKeyResolver {

  /**
   * @param operation the name of the operation that the metadata is going to fetched for
   * @return a new {@link SoapOperationMetadata} with the INPUT body type, headers type and attachments type.
   * @throws MetadataResolvingException in any error case.
   */
  Set<MetadataKey> getMetadataKeys() throws MetadataResolvingException;
}
