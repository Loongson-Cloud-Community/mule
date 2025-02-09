/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.policy;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mule.runtime.api.util.Preconditions.checkArgument;

/**
 * Identifier for a policy state.
 * <p>
 * A policy state identifier is the composition of the execution identifier which is unique accross every execution and the policy
 * id which is unique across all available policies.
 *
 * @since 4.0
 */
public final class PolicyStateId {

  private final String executionIdentifier;
  private final String policyId;
  public static final String POLICY_ID = "policyID";

  /**
   * Creates a new policy state id.
   *
   * @param executionIdentifier identifier of the execution of the policy
   * @param policyId            identifier of the policy
   */
  public PolicyStateId(String executionIdentifier, String policyId) {
    checkArgument(!isEmpty(executionIdentifier), "executionIdentifier cannot be null or empty");
    checkArgument(!isEmpty(executionIdentifier), "policyId cannot be null or empty");
    this.executionIdentifier = executionIdentifier;
    this.policyId = policyId;
  }

  /**
   * @return the identifier of the execution
   */
  public String getExecutionIdentifier() {
    return executionIdentifier;
  }

  /**
   * @return the identifier of the policy
   */
  public String getPolicyId() {
    return policyId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PolicyStateId that = (PolicyStateId) o;

    if (executionIdentifier != null ? !executionIdentifier.equals(that.executionIdentifier)
        : that.executionIdentifier != null) {
      return false;
    }
    return policyId != null ? policyId.equals(that.policyId) : that.policyId == null;

  }

  @Override
  public int hashCode() {
    int result = executionIdentifier != null ? executionIdentifier.hashCode() : 0;
    result = 31 * result + (policyId != null ? policyId.hashCode() : 0);
    return result;
  }
}
