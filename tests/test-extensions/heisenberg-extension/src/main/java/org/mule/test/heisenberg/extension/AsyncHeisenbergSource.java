/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.heisenberg.extension;

import static org.mule.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static org.mule.runtime.extension.api.annotation.param.Optional.PAYLOAD;
import static org.mule.test.heisenberg.extension.HeisenbergExtension.RICIN_GROUP_NAME;

import org.mule.runtime.api.message.Error;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Streaming;
import org.mule.runtime.extension.api.annotation.execution.OnError;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.notification.Fires;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.stereotype.Stereotype;
import org.mule.sdk.api.annotation.source.EmitsResponse;
import org.mule.runtime.extension.api.notification.NotificationEmitter;
import org.mule.runtime.extension.api.runtime.source.SourceCompletionCallback;
import org.mule.test.heisenberg.extension.model.Methylamine;
import org.mule.test.heisenberg.extension.model.PersonalInfo;
import org.mule.test.heisenberg.extension.stereotypes.AsyncSourceStereotype;

@Alias("AsyncListenPayments")
@EmitsResponse
@Fires(SourceNotificationProvider.class)
@Streaming
@Stereotype(AsyncSourceStereotype.class)
@MediaType(TEXT_PLAIN)
public class AsyncHeisenbergSource extends HeisenbergSource {

  public static SourceCompletionCallback completionCallback;
  public static org.mule.sdk.api.runtime.source.SourceCompletionCallback sdkCompletionCallback;

  @OnSuccess
  public void onSuccess(@Optional(defaultValue = PAYLOAD) Long payment, @Optional String sameNameParameter,
                        @org.mule.sdk.api.annotation.param.ParameterGroup(name = RICIN_GROUP_NAME) RicinGroup ricin,
                        @ParameterGroup(name = "Success Info", showInDsl = true) PersonalInfo successInfo,
                        @Optional boolean fail,
                        org.mule.sdk.api.runtime.source.SourceCompletionCallback sdkCompletionCallback,
                        NotificationEmitter notificationEmitter) {

    AsyncHeisenbergSource.sdkCompletionCallback = sdkCompletionCallback;

    try {
      super.onSuccess(payment, sameNameParameter, ricin, successInfo, fail, notificationEmitter);
      sdkCompletionCallback.success();
    } catch (Throwable t) {
      sdkCompletionCallback.error(t);
    }
  }

  @OnError
  public void onError(Error error, @Optional String sameNameParameter, @Optional Methylamine methylamine,
                      @ParameterGroup(name = RICIN_GROUP_NAME) RicinGroup ricin,
                      @org.mule.sdk.api.annotation.param.ParameterGroup(name = "Error Info",
                          showInDsl = true) PersonalInfo infoError,
                      @Optional boolean propagateError,
                      SourceCompletionCallback completionCallback,
                      NotificationEmitter notificationEmitter) {

    AsyncHeisenbergSource.completionCallback = completionCallback;

    try {
      super.onError(error, sameNameParameter, methylamine, ricin, infoError, propagateError, notificationEmitter);
      completionCallback.success();
    } catch (Throwable t) {
      completionCallback.error(t);
    }
  }
}
