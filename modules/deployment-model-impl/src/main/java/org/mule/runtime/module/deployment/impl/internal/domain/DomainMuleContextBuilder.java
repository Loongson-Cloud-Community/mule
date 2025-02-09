/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.impl.internal.domain;

import static org.mule.runtime.core.api.config.bootstrap.ArtifactType.DOMAIN;

import org.mule.runtime.api.notification.ClusterNodeNotification;
import org.mule.runtime.api.notification.ClusterNodeNotificationListener;
import org.mule.runtime.api.notification.ConnectionNotification;
import org.mule.runtime.api.notification.ConnectionNotificationListener;
import org.mule.runtime.api.notification.CustomNotification;
import org.mule.runtime.api.notification.CustomNotificationListener;
import org.mule.runtime.api.notification.ExceptionNotification;
import org.mule.runtime.api.notification.ExceptionNotificationListener;
import org.mule.runtime.api.notification.ExtensionNotification;
import org.mule.runtime.api.notification.ManagementNotification;
import org.mule.runtime.api.notification.ManagementNotificationListener;
import org.mule.runtime.api.notification.SecurityNotification;
import org.mule.runtime.api.notification.SecurityNotificationListener;
import org.mule.runtime.core.api.config.DefaultMuleConfiguration;
import org.mule.runtime.core.api.config.MuleConfiguration;
import org.mule.runtime.core.api.context.notification.MuleContextNotification;
import org.mule.runtime.core.api.context.notification.MuleContextNotificationListener;
import org.mule.runtime.core.api.context.notification.ServerNotificationManager;
import org.mule.runtime.core.internal.context.DefaultMuleContextBuilder;
import org.mule.runtime.module.artifact.api.classloader.MuleArtifactClassLoader;

/**
 * Builder for domain MuleContext instance.
 */
public class DomainMuleContextBuilder extends DefaultMuleContextBuilder {

  private final String domainId;

  public DomainMuleContextBuilder(String domainId) {
    super(DOMAIN);
    this.domainId = domainId;
  }

  @Override
  protected MuleConfiguration getMuleConfiguration() {
    DefaultMuleConfiguration defaultMuleConfiguration = new DefaultMuleConfiguration(true);
    defaultMuleConfiguration.setDomainId(domainId);
    defaultMuleConfiguration.setId(domainId);
    if (executionClassLoader instanceof MuleArtifactClassLoader) {
      defaultMuleConfiguration
          .setMinMuleVersion(((MuleArtifactClassLoader) executionClassLoader).getArtifactDescriptor().getMinMuleVersion());
    }
    return defaultMuleConfiguration;
  }

  @Override
  protected ServerNotificationManager createNotificationManager() {
    ServerNotificationManager manager = new ServerNotificationManager();
    manager.addInterfaceToType(MuleContextNotificationListener.class, MuleContextNotification.class);
    manager.addInterfaceToType(SecurityNotificationListener.class, SecurityNotification.class);
    manager.addInterfaceToType(ManagementNotificationListener.class, ManagementNotification.class);
    manager.addInterfaceToType(CustomNotificationListener.class, CustomNotification.class);
    manager.addInterfaceToType(ConnectionNotificationListener.class, ConnectionNotification.class);
    manager.addInterfaceToType(ExceptionNotificationListener.class, ExceptionNotification.class);
    manager.addInterfaceToType(ClusterNodeNotificationListener.class, ClusterNodeNotification.class);
    manager.addInterfaceToType(ExceptionNotificationListener.class, ExtensionNotification.class);
    return manager;
  }
}
