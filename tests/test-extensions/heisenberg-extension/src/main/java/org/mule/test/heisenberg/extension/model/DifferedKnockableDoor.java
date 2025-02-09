/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.heisenberg.extension.model;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.parameter.Literal;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

import java.util.Objects;

@TypeDsl(allowTopLevelDefinition = true)
public class DifferedKnockableDoor {

  @Parameter
  @Optional
  private ParameterResolver<String> victim;

  @Parameter
  @Optional
  private TypedValue<String> address;

  @Parameter
  @Optional
  private org.mule.sdk.api.runtime.parameter.Literal<String> encodedPasssword;

  public ParameterResolver<String> getVictim() {
    return victim;
  }

  public TypedValue<String> getAddress() {
    return address;
  }

  public org.mule.sdk.api.runtime.parameter.Literal<String> getEncodedPasssword() {
    return encodedPasssword;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DifferedKnockableDoor that = (DifferedKnockableDoor) o;
    return Objects.equals(victim, that.victim) &&
        Objects.equals(address, that.address) &&
        Objects.equals(encodedPasssword, that.encodedPasssword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(victim, address, encodedPasssword);
  }
}
