/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.module.extension.config;

import static java.util.Calendar.YEAR;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mule.test.heisenberg.extension.HeisenbergExtension.AGE;
import static org.mule.test.heisenberg.extension.HeisenbergExtension.HEISENBERG;

import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.privileged.event.BaseEventContext;
import org.mule.test.heisenberg.extension.HeisenbergExtension;
import org.mule.test.heisenberg.extension.model.HealthStatus;
import org.mule.test.heisenberg.extension.model.KnockeableDoor;
import org.mule.test.heisenberg.extension.model.Ricin;
import org.mule.test.runner.RunnerDelegateTo;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunnerDelegateTo(Parameterized.class)
public class ParameterizedConfigParserTestCase extends AbstractConfigParserTestCase {

  private static final String HEISENBERG_BYNAME = "heisenberg";
  private static final String HEISENBERG_PLACEHOLDERS = "heisenbergWithPlaceHolders";
  private static final String HEISENBERG_BYREF = "heisenbergByRef";
  private static final String HEISENBERG_EXPRESSION = "expressionHeisenberg";
  private static final String HEISENBERG_EXPRESSION_BYREF = "expressionHeisenbergByRef";

  private static final Long MICROGRAMS_PER_KILO = 22L;
  private static final String LIDIA = "Lidia";
  private static final String STEVIA_COFFE_SHOP = "Stevia coffe shop";
  private static final String POLLOS_HERMANOS = "pollos hermanos";
  private static final String GUSTAVO_FRING = "Gustavo Fring";
  private static final String KRAZY_8 = "Krazy-8";
  private static final String JESSE_S = "Jesse's";
  private static final String METHYLAMINE = "methylamine";
  private static final int METHYLAMINE_QUANTITY = 75;
  private static final String PSEUDOEPHEDRINE = "pseudoephedrine";
  private static final int PSEUDOEPHEDRINE_QUANTITY = 0;
  private static final String P2P = "P2P";
  private static final int P2P_QUANTITY = 25;
  private static final String HANK = "Hank";
  private static final String MONEY = "1000000";
  private static final String LAB_ADDRESS = "Pollos Hermanos";
  private static final String FIRST_ENDEVOUR = "Gray Matter Technologies";
  private static final int DEATH_YEAR = 2011;
  private static final HealthStatus INITIAL_HEALTH = HealthStatus.CANCER;
  private static final HealthStatus FINAL_HEALTH = HealthStatus.DEAD;

  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{HEISENBERG_BYNAME}, {HEISENBERG_PLACEHOLDERS}, {HEISENBERG_BYREF},
        {HEISENBERG_EXPRESSION}, {HEISENBERG_EXPRESSION_BYREF}});
  }

  @Parameter(0)
  public String testConfig;

  @Test
  public void config() throws Exception {
    HeisenbergExtension heisenberg = lookupHeisenberg(testConfig);
    assertHeisenbergConfig(heisenberg);
  }

  @Test
  public void injectedConfigName() throws Exception {
    HeisenbergExtension heisenberg = lookupHeisenberg(testConfig);
    assertThat(heisenberg.getConfigName(), equalTo(testConfig));
  }

  @Test
  public void sameInstanceForEquivalentEvent() throws Exception {
    CoreEvent event = null;
    try {
      event = getHeisenbergEvent();
      HeisenbergExtension heisenberg = lookupHeisenberg(testConfig, event);
      assertThat(heisenberg, is(sameInstance(lookupHeisenberg(testConfig, event))));
    } finally {
      if (event != null) {
        ((BaseEventContext) event.getContext()).success();
      }
    }
  }

  @Test
  public void configWithExpressionFunctionIsSameInstanceForDifferentEvents() throws Exception {
    CoreEvent event = null;
    try {
      event = getHeisenbergEvent();
      CoreEvent anotherEvent = testEvent();
      HeisenbergExtension config = lookupHeisenberg(HEISENBERG_BYNAME, event);
      HeisenbergExtension anotherConfig = lookupHeisenberg(HEISENBERG_BYNAME, anotherEvent);
      assertThat(config, is(sameInstance(anotherConfig)));
    } finally {
      if (event != null) {
        ((BaseEventContext) event.getContext()).success();
      }
    }
  }

  @Test
  public void configWithExpressionFunctionStillDynamic() throws Exception {
    CoreEvent event = null;
    CoreEvent anotherEvent = null;
    try {
      event = getHeisenbergEvent();
      anotherEvent = CoreEvent.builder(getHeisenbergEvent()).addVariable("age", 40).build();
      HeisenbergExtension config = lookupHeisenberg(HEISENBERG_EXPRESSION, event);
      HeisenbergExtension anotherConfig = lookupHeisenberg(HEISENBERG_EXPRESSION, anotherEvent);
      assertThat(config, is(not(sameInstance(anotherConfig))));
    } finally {
      if (event != null) {
        ((BaseEventContext) event.getContext()).success();
      }
      if (anotherEvent != null) {
        ((BaseEventContext) anotherEvent.getContext()).success();
      }
    }
  }

  @Test
  public void lifecycle() throws Exception {
    HeisenbergExtension heisenberg = lookupHeisenberg(testConfig);
    assertThat(heisenberg.getInitialise(), is(1));
    assertThat(heisenberg.getStart(), is(1));

    muleContext.stop();
    muleContext.dispose();

    assertThat(heisenberg.getStop(), is(1));
    assertThat(heisenberg.getDispose(), is(1));
  }

  @Test
  public void dependenciesInjected() throws Exception {
    HeisenbergExtension heisenberg = lookupHeisenberg(testConfig);
    assertThat(heisenberg.getExtensionManager(), is(sameInstance(muleContext.getExtensionManager())));
  }

  private void assertHeisenbergConfig(HeisenbergExtension heisenberg) {
    assertNotNull(heisenberg);

    assertSimpleProperties(heisenberg);
    assertRecipe(heisenberg);
    assertDoors(heisenberg);
    assertRicinPacks(heisenberg);
    assertCandidateDoors(heisenberg);
    assertDeathsBySeason(heisenberg);
    assertMonthlyIncomes(heisenberg);
    assertLabeledRicin(heisenberg);
  }


  private void assertRicinPacks(HeisenbergExtension heisenberg) {
    Set<Ricin> ricinPacks = heisenberg.getRicinGroup().getRicinPacks();

    assertNotNull(ricinPacks);
    assertThat(ricinPacks.size(), equalTo(1));

    Ricin ricin = ricinPacks.iterator().next();
    assertThat(ricin.getMicrogramsPerKilo(), equalTo(MICROGRAMS_PER_KILO));
    assertDoor(ricin.getDestination(), LIDIA, STEVIA_COFFE_SHOP);
  }

  private void assertDoors(HeisenbergExtension heisenberg) {
    KnockeableDoor door = heisenberg.getRicinGroup().getNextDoor();
    assertDoor(door, GUSTAVO_FRING, POLLOS_HERMANOS);

    KnockeableDoor previous = door.getPrevious();
    assertDoor(door.getPrevious(), KRAZY_8, JESSE_S);
    assertNull(previous.getPrevious());
  }

  private void assertRecipe(HeisenbergExtension heisenberg) {
    Map<String, Long> recipe = heisenberg.getRecipe();
    assertNotNull(recipe);
    assertThat(recipe.size(), equalTo(3));
    assertThat(recipe.get(METHYLAMINE), equalTo(Long.valueOf(METHYLAMINE_QUANTITY)));
    assertThat(recipe.get(PSEUDOEPHEDRINE), equalTo(Long.valueOf(PSEUDOEPHEDRINE_QUANTITY)));
    assertThat(recipe.get(P2P), equalTo(Long.valueOf(P2P_QUANTITY)));
  }

  private void assertSimpleProperties(HeisenbergExtension heisenberg) {
    assertThat(heisenberg.getPersonalInfo().getName(), equalTo(HEISENBERG));
    assertThat(heisenberg.getPersonalInfo().getAge(), equalTo(Integer.valueOf(AGE)));

    List<String> enemies = heisenberg.getEnemies();
    assertThat(enemies, notNullValue());
    assertThat(enemies.size(), equalTo(2));
    assertThat(enemies.get(0), equalTo(GUSTAVO_FRING));
    assertThat(enemies.get(1), equalTo(HANK));

    assertTrue(heisenberg.isCancer());
    assertThat(heisenberg.getInitialHealth(), is(INITIAL_HEALTH));
    assertThat(heisenberg.getEndingHealth(), is(FINAL_HEALTH));
    assertThat(heisenberg.getFirstEndevour(), containsString(FIRST_ENDEVOUR));
    assertThat(heisenberg.getLabAddress(), is(LAB_ADDRESS));

    Calendar dayOfBirth = Calendar.getInstance();
    dayOfBirth.setTime(heisenberg.getPersonalInfo().getDateOfBirth());

    // only compare year to avoid timezone related flakyness
    assertThat(dayOfBirth.get(YEAR), equalTo(getDateOfBirth().get(YEAR)));
    assertThat(heisenberg.getPersonalInfo().getDateOfDeath().get(YEAR), equalTo(getDateOfDeath().get(YEAR)));

    assertThat(heisenberg.getPersonalInfo().getDateOfConception().getYear(),
               is(getDateOfConception().getYear()));
    assertThat(heisenberg.getPersonalInfo().getDateOfConception().getMonth(),
               is(getDateOfConception().getMonth()));

    assertThat(heisenberg.getMoney(), equalTo(new BigDecimal(MONEY)));
  }


  private void assertCandidateDoors(HeisenbergExtension heisenberg) {
    Map<String, KnockeableDoor> candidates = heisenberg.getCandidateDoors();
    assertNotNull(candidates);
    assertThat(candidates.size(), equalTo(2));

    assertDoor(candidates.get(SKYLER.toLowerCase()), SKYLER, WHITE_ADDRESS);
    assertDoor(candidates.get(SAUL.toLowerCase()), SAUL, SHOPPING_MALL);
  }

  private void assertDoor(KnockeableDoor door, String victim, String address) {
    assertNotNull(door);
    assertThat(door.getVictim(), equalTo(victim));
    assertThat(door.getAddress(), equalTo(address));
  }

  public static Calendar getDateOfBirth() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(YEAR, 1959);
    calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 7);
    calendar.set(Calendar.HOUR, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar;
  }

  public static LocalDateTime getDateOfConception() {
    return LocalDateTime.of(1959, Month.JANUARY, 7, 0, 0);
  }

  public static Calendar getDateOfDeath() {
    Calendar calendar = getDateOfBirth();
    calendar.set(YEAR, DEATH_YEAR);

    return calendar;
  }

  private void assertLabeledRicin(HeisenbergExtension heisenberg) {

    Map<String, Ricin> labeledRicin = heisenberg.getRicinGroup().getLabeledRicin();

    assertNotNull(labeledRicin);
    assertThat(labeledRicin.size(), equalTo(1));
    assertThat(labeledRicin.get("pojo"), instanceOf(Ricin.class));

    Ricin ricin = labeledRicin.get("pojo");
    assertNotNull(ricin);
    assertThat(ricin.getMicrogramsPerKilo(), equalTo(MICROGRAMS_PER_KILO));
    assertDoor(ricin.getDestination(), LIDIA, STEVIA_COFFE_SHOP);
  }

  private void assertMonthlyIncomes(HeisenbergExtension heisenberg) {
    List<Long> incomes = heisenberg.getMonthlyIncomes();

    assertNotNull(incomes);
    assertThat(incomes.size(), equalTo(MONTHLY_INCOMES.size()));
    assertThat(incomes, containsInAnyOrder(MONTHLY_INCOMES.toArray()));
  }

  private void assertDeathsBySeason(HeisenbergExtension heisenberg) {

    Map<String, List<String>> deaths = heisenberg.getDeathsBySeasons();

    assertNotNull(deaths);
    assertThat(deaths.size(), equalTo(2));

    List<String> s01 = deaths.get(SEASON_1_KEY);
    assertNotNull(s01);
    assertThat(s01.size(), equalTo(DEATHS_BY_SEASON.get(SEASON_1_KEY).size()));
    assertThat(s01, containsInAnyOrder(DEATHS_BY_SEASON.get(SEASON_1_KEY).toArray()));

    List<String> s02 = deaths.get(SEASON_2_KEY);
    assertNotNull(s02);
    assertThat(s02.size(), equalTo(DEATHS_BY_SEASON.get(SEASON_2_KEY).size()));
    assertThat(s02, containsInAnyOrder(DEATHS_BY_SEASON.get(SEASON_2_KEY).toArray()));
  }
}
