/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.source.legacy;

import org.mule.runtime.extension.api.runtime.source.Source;

/**
 * Interface that is meant to signal that the given instances delegate its responsabilities into a legacy {@link Source}
 *
 * @since 4.4.0
 */
public interface LegacySourceWrapper {

  /**
   * This method provide the instance of the legacy {@link Source} in with this implementation is delegating behavior.
   *
   * @return the delegate {@link Source} instance.
   */
  Source getDelegate();

}
