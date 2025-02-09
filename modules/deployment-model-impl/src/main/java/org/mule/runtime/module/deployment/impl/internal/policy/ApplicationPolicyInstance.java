/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.impl.internal.policy;

import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.policy.Policy;
import org.mule.runtime.deployment.model.api.policy.PolicyTemplate;
import org.mule.runtime.policy.api.PolicyPointcut;

import java.util.Optional;

/**
 * Defines a policy provider for a given parametrized policy
 */
public interface ApplicationPolicyInstance extends Initialisable, Disposable {

  /**
   * @return the policy's pointcut used to determine whether to apply or ignore the policy when a request arrives. No null.
   */
  PolicyPointcut getPointcut();

  /**
   * @return order that must be used to apply the policy
   */
  int getOrder();

  /**
   * @return policy template from which the instance will be created
   */
  PolicyTemplate getPolicyTemplate();

  /**
   * @return policy to intercept the source execution
   */
  Optional<Policy> getSourcePolicy();

  /**
   * @return policy to intercept the operation execution
   */
  Optional<Policy> getOperationPolicy();
}
