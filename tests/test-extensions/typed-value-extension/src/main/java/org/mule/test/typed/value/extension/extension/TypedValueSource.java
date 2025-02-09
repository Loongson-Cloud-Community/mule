/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.typed.value.extension.extension;

import static org.mule.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.execution.OnTerminate;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;

@Alias("source")
@MediaType(TEXT_PLAIN)
public class TypedValueSource extends Source<String, Object> {

  public static TypedValue<String> onSuccessValue;

  @Override
  public void onStart(SourceCallback<String, Object> sourceCallback) throws MuleException {
    sourceCallback.handle(Result.<String, Object>builder().output("This is a string").build());
  }

  @Override
  public void onStop() {

  }

  @OnSuccess
  public void onSuccess(TypedValue<String> stringValue) {
    onSuccessValue = stringValue;
  }

  @OnTerminate
  public void onTerminate() {

  }
}
