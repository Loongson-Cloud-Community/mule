/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.parser.java.info;


/**
 * Class that models the information of the RequiresEnterpriseLicense annotations.
 *
 * @since 4.5
 */
public class RequiresEnterpriseLicenseInfo {

  private final boolean allowEvaluationLicense;

  public RequiresEnterpriseLicenseInfo(boolean allowEvaluationLicense) {
    this.allowEvaluationLicense = allowEvaluationLicense;
  }

  public boolean isAllowEvaluationLicense() {
    return allowEvaluationLicense;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    RequiresEnterpriseLicenseInfo that = (RequiresEnterpriseLicenseInfo) o;

    return allowEvaluationLicense == that.allowEvaluationLicense;
  }

  @Override
  public int hashCode() {
    return (allowEvaluationLicense ? 1 : 0);
  }
}
