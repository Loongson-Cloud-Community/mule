/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.config;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.config.custom.ServiceConfigurator;
import org.mule.runtime.api.service.Service;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.internal.config.builders.MinimalConfigurationBuilder;

/**
 * A <code>ConfigurationBuilder</code> is used to configure a Mule instance, represented by a MuleContext instance. Multiple
 * ConfigurationBuilder's can be used to configure a single mule instance with each ConfigurationBuilder doing one of more of the
 * following:
 * <li>Creation of mule runtime artifacts (endpoint's, connector's, service's, transformer's) which are then registered with the
 * <code>Registry</code
 * <li> Creation and registration of SecurityManager / TransactionManager / TransactionManagerFactory / QueueManager
 * and ThreadingProfile's.  Unlike the runtime artifacts mule only uses a single instance of each of these and so if
 * multiple configuration builder create and register these artifacts only one will be used.
 * <li> Configuration of existing Mule configuration related artifacts such as <code>MuleConfiguration</code> and
 * <code>ServerNotificationManager</code> <br/>
 * <br/>
 * Which of the above takes place, depends on what the configuration source contains and the ConfgurationBuilder implementation is
 * used.
 */
public interface ConfigurationBuilder {

  /**
   * Returns an instance which configures a {@link MuleContext} {@link Registry} with the bare minimum elements needed for
   * functioning. This instance will configure the elements related to a particular {@link MuleContext} only. It will not
   * configure container related elements such as {@link Service mule services}.
   *
   * @return a {@link ConfigurationBuilder}
   * @since 4.5.0
   */
  static ConfigurationBuilder getMinimalConfigurationBuilder() {
    return new MinimalConfigurationBuilder();
  }

  /**
   * Adds a service configurator to be used on the context being built.
   *
   * @param serviceConfigurator service to add. Non null.
   */
  void addServiceConfigurator(ServiceConfigurator serviceConfigurator);

  /**
   * Will configure a MuleContext based on the configuration provided. The configuration will be set on the
   * {@link org.mule.runtime.core.api.config.ConfigurationBuilder} implementation as bean properties before this method has been
   * called.
   *
   * @param muleContext The current {@link org.mule.runtime.core.api.MuleContext}
   * @throws ConfigurationException if the configuration fails i.e. an object cannot be created or initialised properly
   */
  void configure(MuleContext muleContext) throws ConfigurationException;
}
