/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.retry.policy;


/**
 * Indicates the current state of a RetryPolicy
 * <ul>
 * <li>ok: The policy is active</li>
 * <li>exhausted: The policy has run through the actions for the policy</li>
 * </ul>
 *
 * For example, a RetryPolicy may have a RetryCount - how many times the policy can be invoked. Once the retryCount has been
 * reached, the policy is exhausted and cannot be used again.
 */
public final class PolicyStatus {

  private boolean exhausted = false;
  private boolean ok = false;
  private Throwable throwable;

  public static PolicyStatus policyExhausted(Throwable t) {
    return new PolicyStatus(true, t);
  }

  public static PolicyStatus policyOk() {
    return new PolicyStatus();
  }

  protected PolicyStatus() {
    this.ok = true;
  }

  protected PolicyStatus(boolean exhausted, Throwable throwable) {
    this.exhausted = exhausted;
    this.throwable = throwable;
  }

  public boolean isExhausted() {
    return exhausted;
  }

  public boolean isOk() {
    return ok;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  @Override
  public String toString() {
    return "PolicyStatus{ ok: " + ok + "; exhausted: " + exhausted + "; throwable: " + throwable + "}";
  }
}
