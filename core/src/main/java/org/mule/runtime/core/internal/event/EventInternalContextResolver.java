/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.event;

import static org.mule.runtime.api.util.collection.SmallMap.of;
import static org.mule.runtime.core.internal.event.EventQuickCopy.quickCopy;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.internal.message.InternalEvent;

import java.util.function.Supplier;

/**
 * Resolves initial and mutated event-wide internal Contexts.
 *
 * For example, this is used for {@link org.mule.runtime.core.internal.policy.PolicyEventMapper} handling nested policies, or
 * {@link org.mule.runtime.core.internal.routing.UntilSuccessfulRouter} for handling nested retrial contexts.
 *
 * @param <CTX> The context type
 * @since 4.2.3, 4.3.0
 */
public class EventInternalContextResolver<CTX> {

  String internalParametersKey;
  Supplier<CTX> initialContextSupplier;

  /**
   * @param key                    with which this contexts are stores inside the event
   * @param initialContextSupplier supplies the initial context state
   */
  public EventInternalContextResolver(String key, Supplier<CTX> initialContextSupplier) {
    this.internalParametersKey = key;
    this.initialContextSupplier = initialContextSupplier;
  }

  /**
   * @param event gets the current context stored in the event
   * @return a {@link CTX}
   */
  public CTX getCurrentContextFromEvent(CoreEvent event) {
    CTX currentContext = ((InternalEvent) event).getInternalParameter(internalParametersKey);
    if (currentContext == null) {
      currentContext = initialContextSupplier.get();
    }
    return currentContext;
  }

  public CoreEvent eventWithContext(CoreEvent event, CTX context) {
    return quickCopy(event, of(internalParametersKey, context));
  }
}
