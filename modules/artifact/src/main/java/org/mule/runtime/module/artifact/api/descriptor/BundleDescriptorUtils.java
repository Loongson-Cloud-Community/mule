/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.api.descriptor;

import static com.vdurmont.semver4j.Semver.SemverType.LOOSE;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mule.runtime.api.util.Preconditions.checkArgument;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;

/**
 * Utilities to work with {@link BundleDescriptor}
 */
public class BundleDescriptorUtils {

  private BundleDescriptorUtils() {}

  /**
   * Determines if a version is compatible with another one
   *
   * @param availableVersion version that is available to use. Non empty
   * @param expectedVersion  version that is expected. Non empty
   * @return true if versions are compatible, false otherwise
   */
  public static boolean isCompatibleVersion(String availableVersion, String expectedVersion) {
    checkArgument(!isEmpty(availableVersion), "availableVersion cannot be empty");
    checkArgument(!isEmpty(expectedVersion), "expectedVersion cannot be empty");

    if (availableVersion.equals(expectedVersion)) {
      return true;
    }

    Semver available = getBundleVersion(availableVersion);
    Semver expected = getBundleVersion(expectedVersion);

    if (available.isGreaterThan(expected)) {
      return available.getMajor().equals(expected.getMajor());
    }

    return false;
  }

  private static Semver getBundleVersion(String version) {
    try {
      return new Semver(version, LOOSE);
    } catch (SemverException e) {
      throw new InvalidDependencyVersionException(
                                                  format("Unable to parse bundle version: %s, version is not following semantic versioning",
                                                         version),
                                                  e);
    }
  }
}
