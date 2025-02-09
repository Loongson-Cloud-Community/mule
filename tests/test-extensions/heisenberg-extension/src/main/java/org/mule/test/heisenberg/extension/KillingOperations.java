/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.heisenberg.extension;

import static java.lang.String.format;
import static org.mule.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static org.mule.runtime.extension.api.annotation.param.Optional.PAYLOAD;
import static org.mule.test.heisenberg.extension.HeisenbergOperations.KILL_WITH_GROUP;

import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.stereotype.Stereotype;
import org.mule.test.heisenberg.extension.model.KillParameters;
import org.mule.test.heisenberg.extension.model.Ricin;
import org.mule.test.heisenberg.extension.model.Weapon;
import org.mule.test.heisenberg.extension.model.types.WeaponType;
import org.mule.test.heisenberg.extension.stereotypes.DrugKillingStereotype;
import org.mule.test.heisenberg.extension.stereotypes.KillingStereotype;

import java.util.List;
import java.util.stream.Collectors;

@Stereotype(KillingStereotype.class)
public class KillingOperations {

  @MediaType(TEXT_PLAIN)
  public String killWithCustomMessage(@ParameterGroup(name = KILL_WITH_GROUP) KillParameters killParameters) {
    return format("%s, %s", killParameters.getGoodbyeMessage(), killParameters.getVictim());
  }

  @MediaType(TEXT_PLAIN)
  public String killWithWeapon(Weapon weapon, WeaponType type, Weapon.WeaponAttributes attributesOfWeapon) {
    return format("Killed with: %s , Type %s and attribute %s", weapon.kill(), type.name(), attributesOfWeapon.getBrand());
  }

  public List<String> killWithMultiplesWeapons(@Optional(defaultValue = PAYLOAD) List<Weapon> weapons) {
    return weapons.stream().map(Weapon::kill).collect(Collectors.toList());
  }

  public List<String> killWithMultipleWildCardWeapons(List<? extends Weapon> wildCardWeapons) {
    return wildCardWeapons.stream().map(Weapon::kill).collect(Collectors.toList());
  }

  public int killWithId(int id) {
    return id;
  }

  @Stereotype(DrugKillingStereotype.class)
  public List<Ricin> drugKilling(@Optional(defaultValue = PAYLOAD) List<Ricin> ricins) {
    return ricins;
  }

  public List<Ricin> killWithRicins(@Optional(defaultValue = PAYLOAD) List<Ricin> ricins) {
    return ricins;
  }
}
