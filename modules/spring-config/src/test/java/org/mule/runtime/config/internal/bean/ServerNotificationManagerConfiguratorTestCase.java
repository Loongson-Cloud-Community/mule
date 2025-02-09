/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.bean;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.config.internal.bean.ServerNotificationManagerConfigurator;
import org.mule.runtime.config.internal.bean.NotificationConfig.DisabledNotificationConfig;
import org.mule.runtime.config.internal.bean.NotificationConfig.EnabledNotificationConfig;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.api.notification.AbstractServerNotification;
import org.mule.runtime.api.notification.NotificationListener;
import org.mule.runtime.core.api.context.notification.NotificationsProvider;
import org.mule.runtime.core.api.context.notification.ServerNotificationManager;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationContext;

public class ServerNotificationManagerConfiguratorTestCase extends AbstractMuleTestCase {

  private Registry registry;
  private ServerNotificationManager notificationManager;
  private MuleContext context;
  private ServerNotificationManagerConfigurator configurator;

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Before
  public void before() {
    registry = mock(Registry.class);

    notificationManager = mock(ServerNotificationManager.class);
    context = mock(MuleContext.class);
    doReturn(notificationManager).when(context).getNotificationManager();

    configurator = new ServerNotificationManagerConfigurator();
    configurator.setMuleContext(context);
    configurator.setRegistry(registry);
    final ApplicationContext springContext = mock(ApplicationContext.class);
    doReturn(new String[0]).when(springContext).getBeanNamesForType(NotificationListener.class, false, true);
    configurator.setApplicationContext(springContext);
  }

  @Test
  public void compliantEnabledNotification() throws InitialisationException {
    doReturn(singletonList((NotificationsProvider) () -> singletonMap("test:COMPLIANT",
                                                                      new Pair(CompliantNotification.class,
                                                                               CompliantNotificationListener.class))))
                                                                                   .when(registry)
                                                                                   .lookupAllByType(NotificationsProvider.class);

    configurator.setEnabledNotifications(singletonList(new EnabledNotificationConfig(CompliantNotificationListener.class,
                                                                                     CompliantNotification.class)));
    configurator.initialise();

    verify(notificationManager).addInterfaceToType(CompliantNotificationListener.class, CompliantNotification.class);
  }

  @Test
  public void compliantEnabledDuplicateNotification() throws InitialisationException {
    doReturn(asList((NotificationsProvider) () -> singletonMap("test:COMPLIANT",
                                                               new Pair(CompliantNotification.class,
                                                                        CompliantNotificationListener.class)),
                    (NotificationsProvider) () -> singletonMap("test:COMPLIANT",
                                                               new Pair(CompliantNotification.class,
                                                                        CompliantNotificationListener.class))))
                                                                            .when(registry)
                                                                            .lookupAllByType(NotificationsProvider.class);

    configurator.setEnabledNotifications(singletonList(new EnabledNotificationConfig(CompliantNotificationListener.class,
                                                                                     CompliantNotification.class)));

    expected.expect(InitialisationException.class);
    expected.expectMessage(containsString("'test:COMPLIANT'"));
    configurator.initialise();
  }

  @Test
  public void compliantDisabledNotificationByEventClass() throws InitialisationException {
    doReturn(singletonList((NotificationsProvider) () -> singletonMap("test:COMPLIANT",
                                                                      new Pair(CompliantNotification.class,
                                                                               CompliantNotificationListener.class))))
                                                                                   .when(registry)
                                                                                   .lookupAllByType(NotificationsProvider.class);

    final DisabledNotificationConfig disableNotificationConfig = new DisabledNotificationConfig();
    disableNotificationConfig.setEventClass(CompliantNotification.class);
    configurator.setDisabledNotifications(singletonList(disableNotificationConfig));
    configurator.initialise();

    verify(notificationManager).disableType(CompliantNotification.class);
  }

  @Test
  public void compliantDisabledNotificationByEventName() throws InitialisationException {
    doReturn(singletonList((NotificationsProvider) () -> singletonMap("test:COMPLIANT",
                                                                      new Pair(CompliantNotification.class,
                                                                               CompliantNotificationListener.class))))
                                                                                   .when(registry)
                                                                                   .lookupAllByType(NotificationsProvider.class);

    final DisabledNotificationConfig disableNotificationConfig = new DisabledNotificationConfig();
    disableNotificationConfig.setEventName("test:COMPLIANT");
    configurator.setDisabledNotifications(singletonList(disableNotificationConfig));
    configurator.initialise();

    verify(notificationManager).disableType(CompliantNotification.class);
  }

  @Test
  public void compliantDisabledNotificationByInterface() throws InitialisationException {
    doReturn(singletonList((NotificationsProvider) () -> singletonMap("test:COMPLIANT",
                                                                      new Pair(CompliantNotification.class,
                                                                               CompliantNotificationListener.class))))
                                                                                   .when(registry)
                                                                                   .lookupAllByType(NotificationsProvider.class);

    final DisabledNotificationConfig disableNotificationConfig = new DisabledNotificationConfig();
    disableNotificationConfig.setInterfaceClass(CompliantNotificationListener.class);
    configurator.setDisabledNotifications(singletonList(disableNotificationConfig));
    configurator.initialise();

    verify(notificationManager).disableInterface(CompliantNotificationListener.class);
  }

  @Test
  public void nonCompliantNotification() throws InitialisationException {
    doReturn(singletonList((NotificationsProvider) () -> singletonMap("nonCompliant", new Pair(null, null)))).when(registry)
        .lookupAllByType(NotificationsProvider.class);

    expected.expect(InitialisationException.class);
    expected.expectMessage(containsString("'nonCompliant'"));

    configurator.initialise();
  }

  public static class CompliantNotification extends AbstractServerNotification {

    public CompliantNotification(Object message, int action) {
      super(message, action);
    }

    @Override
    public String getEventName() {
      return "CompliantNotification";
    }
  }

  public static class CompliantNotificationListener implements NotificationListener<CompliantNotification> {

    @Override
    public void onNotification(CompliantNotification notification) {
      // Nothing to do
    }

  }
}
