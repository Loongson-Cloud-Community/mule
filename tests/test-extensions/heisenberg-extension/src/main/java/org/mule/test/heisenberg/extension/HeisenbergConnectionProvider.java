/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.heisenberg.extension;

import static java.lang.String.format;
import static org.mule.runtime.api.meta.ExternalLibraryType.NATIVE;
import static org.mule.test.heisenberg.extension.HeisenbergExtension.HEISENBERG_LIB_CLASS_NAME;
import static org.mule.test.heisenberg.extension.HeisenbergExtension.HEISENBERG_LIB_DESCRIPTION;
import static org.mule.test.heisenberg.extension.HeisenbergExtension.HEISENBERG_LIB_FILE_NAME;
import static org.mule.test.heisenberg.extension.HeisenbergExtension.HEISENBERG_LIB_NAME;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.deprecated.Deprecated;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.sdk.api.annotation.semantics.SemanticTerms;
import org.mule.sdk.api.annotation.semantics.connectivity.Port;
import org.mule.test.heisenberg.extension.model.BarberPreferences;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExternalLib(name = HEISENBERG_LIB_NAME,
    description = HEISENBERG_LIB_DESCRIPTION,
    nameRegexpMatcher = HEISENBERG_LIB_FILE_NAME,
    requiredClassName = HEISENBERG_LIB_CLASS_NAME,
    type = NATIVE)
@Deprecated(
    message = "Usages of this connection provider must be change to the secure option, this will lower the chances of getting caught by the DEA",
    since = "1.3.0", toRemoveIn = "2.0.0")
public class HeisenbergConnectionProvider implements ConnectionProvider<HeisenbergConnection> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeisenbergConnectionProvider.class);

  public static final String SAUL_OFFICE_NUMBER = "505-503-4455";

  private static Set<HeisenbergConnection> activeConnections = new HashSet<>();
  private static final AtomicInteger connects = new AtomicInteger();
  private static final AtomicInteger disconnects = new AtomicInteger();

  @Parameter
  @Optional(defaultValue = SAUL_OFFICE_NUMBER)
  @Port
  @SemanticTerms("telephone")
  private String saulPhoneNumber;

  @ParameterGroup(name = "look", showInDsl = true)
  private BarberPreferences look;

  @Override
  public HeisenbergConnection connect() throws ConnectionException {
    LOGGER.info("'{}' connect()", toString());
    HeisenbergConnection connection = new HeisenbergConnection(saulPhoneNumber);
    synchronized (activeConnections) {
      connects.incrementAndGet();
      activeConnections.add(connection);
    }
    return connection;
  }

  @Override
  public void disconnect(HeisenbergConnection heisenbergConnection) {
    LOGGER.info("'{}' disconnect()", toString());
    synchronized (activeConnections) {
      if (!activeConnections.remove(heisenbergConnection)) {
        throw new IllegalStateException(format("Connection '%s' was already disconnected", heisenbergConnection.toString()));
      }
      disconnects.incrementAndGet();
    }
  }

  @Override
  public ConnectionValidationResult validate(HeisenbergConnection heisenbergConnection) {
    return ConnectionValidationResult.success();
  }

  public static int getConnects() {
    return connects.get();
  }

  public static int getDisconnects() {
    return disconnects.get();
  }

  public static Set<HeisenbergConnection> getActiveConnections() {
    return activeConnections;
  }
}
