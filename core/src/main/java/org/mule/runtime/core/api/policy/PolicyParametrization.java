/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.policy;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mule.runtime.api.util.Preconditions.checkArgument;

import org.mule.runtime.api.notification.NotificationListener;
import org.mule.runtime.policy.api.PolicyPointcut;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Parametrizes a policy template
 * <p/>
 * A policy template is a Mule artifact consistent of a context with dependencies deployed inside a Mule application.
 *
 * @since 4.0
 */
public final class PolicyParametrization {

  private final String id;
  private final PolicyPointcut pointcut;
  private final Map<String, String> parameters;
  private final int order;
  private final File config;
  private final List<NotificationListener> notificationListeners;

  /**
   * Creates a new parametrization
   *
   * @param id                    parametrization identifier. Non empty.
   * @param pointcut              used to determine if the policy must be applied on a given request. Non null.
   * @param order                 indicates how this policy must be ordered related to other applied policies. A policy with a
   *                              given order has to be applied before polices with smaller order and after policies with bigger
   *                              order. Must be positive
   * @param parameters            parameters for the policy template on which the parametrization is based on. Non null.
   * @param config                Mule XML configuration file for creating the policy. Non null.
   * @param notificationListeners notifications listener to be added to policy's context. Non null.
   */
  public PolicyParametrization(String id, PolicyPointcut pointcut, int order, Map<String, String> parameters, File config,
                               List<NotificationListener> notificationListeners) {
    checkArgument(!isEmpty(id), "id cannot be null");
    checkArgument(pointcut != null, "pointcut cannot be null");
    checkArgument(parameters != null, "parameters cannot be null");
    checkArgument(order > 0, "order must be positive");
    checkArgument(config != null, "config file cannot be null");
    checkArgument(notificationListeners != null, "notification listeners cannot be null");

    this.id = id;
    this.pointcut = pointcut;
    this.parameters = unmodifiableMap(parameters);
    this.order = order;
    this.config = config;
    this.notificationListeners = unmodifiableList(notificationListeners);
  }

  /**
   * Creates a new parametrization
   *
   * @param id                    parametrization identifier. Non empty.
   * @param pointcut              used to determine if the policy must be applied on a given request. Non null.
   * @param order                 indicates how this policy must be ordered related to other applied policies. A policy with a
   *                              given order has to be applied before polices with smaller order and after policies with bigger
   *                              order. Must be positive
   * @param parameters            parameters for the policy template on which the parametrization is based on. Non null.
   * @param config                Mule XML configuration file for creating the policy. Non null.
   * @param notificationListeners notifications listener to be added to policy's context. Non null.
   *
   * @deprecated Use {@link #PolicyParametrization(String, PolicyPointcut, int, Map, File, List)} instead.
   */
  @Deprecated
  public PolicyParametrization(String id, org.mule.runtime.core.api.policy.PolicyPointcut pointcut, int order,
                               Map<String, String> parameters, File config,
                               List<NotificationListener> notificationListeners) {
    this(id, (PolicyPointcut) pointcut, order, parameters, config, notificationListeners);
  }

  /**
   * @return parametrization identifier
   */
  public String getId() {
    return id;
  }

  /**
   * @return pointcut to evaluate whether the policy must be applied or not.
   *
   * @deprecated Use {@link #getPolicyPointcut()} instead.
   */
  @Deprecated
  public org.mule.runtime.core.api.policy.PolicyPointcut getPointcut() {
    return (org.mule.runtime.core.api.policy.PolicyPointcut) pointcut;
  }

  /**
   * @return pointcut to evaluate whether the policy must be applied or not.
   */
  public PolicyPointcut getPolicyPointcut() {
    return pointcut;
  }

  /**
   * @return order of the policy parametrization
   */
  public int getOrder() {
    return order;
  }

  /**
   * @return parameters to configure the policy template
   */
  public Map<String, String> getParameters() {
    return parameters;
  }

  /**
   * @return configuration file used to generate the policy
   */
  public File getConfig() {
    return config;
  }

  /**
   * @return notifications listener to be added to policy's context
   */
  public List<NotificationListener> getNotificationListeners() {
    return notificationListeners;
  }
}
