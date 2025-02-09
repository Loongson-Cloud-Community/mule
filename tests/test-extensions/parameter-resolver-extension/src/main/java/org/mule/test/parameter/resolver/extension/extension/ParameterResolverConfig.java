/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.parameter.resolver.extension.extension;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.mule.test.heisenberg.extension.model.KnockeableDoor;

import java.io.InputStream;

@Configuration(name = "config")
@Operations(ParameterResolverParameterOperations.class)
@Sources(SomeSource.class)
public class ParameterResolverConfig extends ParameterResolverExtension {

  @Parameter
  @Optional
  private ParameterResolver<String> stringResolver;

  @Parameter
  @Optional
  private org.mule.sdk.api.runtime.parameter.ParameterResolver<KnockeableDoor> doorResolver;

  @Parameter
  @Optional
  private Literal<KnockeableDoor> literalDoor;

  @Parameter
  @Optional
  private ParameterResolver<TypedValue<InputStream>> lazyParameter;

  @Parameter
  @Optional
  private org.mule.sdk.api.runtime.parameter.ParameterResolver<ParameterResolver<ParameterResolver<org.mule.sdk.api.runtime.parameter.ParameterResolver<TypedValue<InputStream>>>>> nestedParameter;

  @Parameter
  @Optional
  private ParameterResolver<Literal<String>> resolverOfLiteral;

  public ParameterResolver<String> getStringResolver() {
    return stringResolver;
  }

  public void setStringResolver(ParameterResolver<String> stringResolver) {
    this.stringResolver = stringResolver;
  }

  public org.mule.sdk.api.runtime.parameter.ParameterResolver<KnockeableDoor> getDoorResolver() {
    return doorResolver;
  }

  public void setDoorResolver(ParameterResolver<KnockeableDoor> doorResolver) {
    this.doorResolver = doorResolver;
  }

  public Literal<KnockeableDoor> getLiteralDoor() {
    return literalDoor;
  }

  public void setLiteralDoor(Literal<KnockeableDoor> literalDoor) {
    this.literalDoor = literalDoor;
  }
}
