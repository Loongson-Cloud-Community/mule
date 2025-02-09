/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.junit4.rule;

import static java.lang.String.format;

/**
 * Defines a socket port number that will be dynamically assigned as an external resource. The instance will check that the port
 * has been released on test shutdown. To use an instance dynamic socket port:
 *
 * <pre>
 *
 * &#64;Rule
 * public DynamicPort serverPort = new DynamicPort("server_port");
 * </pre>
 * <p/>
 * In order to use static dynamic ports:
 * <p/>
 *
 * <pre>
 *
 * &#64;ClassRule
 * public static DynamicPort dynamicPort = new DynamicPort("server_port");
 * </pre>
 */
public class DynamicPort extends SystemProperty {

  public static final String MIN_PORT_SYSTEM_PROPERTY = "mule.test.minPort";
  public static final String MAX_PORT_SYSTEM_PROPERTY = "mule.test.maxPort";

  private static final int DEFAULT_MIN_PORT = 10000;
  private static final int DEFAULT_MAX_PORT = 40000;

  protected static FreePortFinder freePortFinder;

  static {
    int minPort = DEFAULT_MIN_PORT;
    int maxPort = DEFAULT_MAX_PORT;

    String propertyValue = System.getProperty(MIN_PORT_SYSTEM_PROPERTY);
    if (propertyValue != null) {
      minPort = Integer.parseInt(propertyValue);
    }

    propertyValue = System.getProperty(MAX_PORT_SYSTEM_PROPERTY);
    if (propertyValue != null) {
      maxPort = Integer.parseInt(propertyValue);
    }

    if (minPort > maxPort) {
      throw new IllegalArgumentException(format("Min port '%s' must be less than max port '%s'", minPort, maxPort));
    }

    freePortFinder = new FreePortFinder(minPort, maxPort);
  }


  private int number;

  /**
   * Creates a dynamic port resource for a given port name.
   *
   * @param name the name assigned to the port number. On resource creation a new system property will be created with that name
   *             and the value will be the port number.
   */
  public DynamicPort(String name) {
    super(name);
  }

  @Override
  public String getValue() {
    return Integer.toString(getNumber());
  }

  @Override
  protected void doCleanUp() {
    freePortFinder.releasePort(number);
  }

  public int getNumber() {
    if (number == 0) {
      number = freePortFinder.find();
    }

    return number;
  }
}
