/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.troubleshooting.api;

import org.mule.api.annotation.Experimental;

/**
 * Interface that has the {@link TroubleshootingOperationDefinition} and the {@link TroubleshootingOperationCallback} for a given
 * operation.
 *
 * @since 4.5
 */
@Experimental
public interface TroubleshootingOperation {

  /**
   * Returns the {@link TroubleshootingOperationDefinition} for this operation.
   *
   * @return the {@link TroubleshootingOperationDefinition} for this operation.
   */
  TroubleshootingOperationDefinition getDefinition();

  /**
   * Returns the {@link TroubleshootingOperationCallback} for this operation.
   *
   * @return the {@link TroubleshootingOperationCallback} for this operation.
   */

  TroubleshootingOperationCallback getCallback();
}
