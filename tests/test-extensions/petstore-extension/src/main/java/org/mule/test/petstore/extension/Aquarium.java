/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.petstore.extension;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import java.util.Objects;

public class Aquarium {

  @org.mule.sdk.api.annotation.param.ParameterGroup(name = "pond")
  public Pond pond;

  @Parameter
  @Optional(defaultValue = "50")
  public Integer ticketPrice;

  public Integer getTicketPrice() {
    return ticketPrice;
  }

  public Pond getPond() {
    return pond;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Aquarium aquarium = (Aquarium) o;
    return Objects.equals(pond, aquarium.pond) &&
        Objects.equals(ticketPrice, aquarium.ticketPrice);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pond, ticketPrice);
  }
}
