/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.some.extension;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;

@MediaType(ANY)
public class AnotherEmittingSource extends Source<Object, Void> {

  @ParameterGroup(name = "Awesome Parameter Group")
  SomeParameterGroupOneRequiredConfig someParameterGroup;

  @Override
  public void onStart(SourceCallback<Object, Void> sourceCallback) {
    Object parameterValue = someParameterGroup.getSomeParameter() != null ? someParameterGroup.getSomeParameter()
        : someParameterGroup.getComplexParameter();
    sourceCallback.handle(Result.<Object, Void>builder().output(parameterValue).build());
  }

  @Override
  public void onStop() {}
}
