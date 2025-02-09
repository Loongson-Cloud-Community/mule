/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.vegan.extension;

import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.runtime.operation.FlowListener;
import org.mule.tck.testmodels.fruit.Banana;
import org.mule.tck.testmodels.fruit.Fruit;

public class FlowListenerBananaOperations {

  @OutputResolver(output = FruitMetadataResolver.class)
  public Fruit getLunch(@Config BananaConfig config, FlowListener listener) {
    final Banana banana = new Banana();
    listener.onSuccess(message -> {
      if (message.getPayload().getValue() instanceof Banana) {
        config.onBanana();
      } else {
        config.onNotBanana();
      }
    });

    listener.onError(exception -> config.onException());
    listener.onComplete(() -> banana.peel());

    return banana;
  }

  @OutputResolver(output = FruitMetadataResolver.class)
  public Fruit SdkGetLunch(@Config BananaConfig config, org.mule.sdk.api.runtime.operation.FlowListener listener) {
    final Banana banana = new Banana();
    listener.onSuccess(message -> {
      if (message.getPayload().getValue() instanceof Banana) {
        config.onBanana();
      } else {
        config.onNotBanana();
      }
    });

    listener.onError(exception -> config.onException());
    listener.onComplete(() -> banana.peel());

    return banana;
  }

}
