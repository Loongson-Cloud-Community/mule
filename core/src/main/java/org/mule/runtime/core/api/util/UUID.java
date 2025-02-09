/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.util;

/**
 * <code>UUID</code> Generates a UUID using the <a href="http://johannburkard.de/software/uuid/">Johann Burkard UUID Library</a>.
 * In our performance tests we found this to be the implementation of type 1 UUID that was most performant in high concurrency
 * scenarios.
 */
// @ThreadSafe
public final class UUID {

  private UUID() {
    // no go
  }

  /**
   * @return time-based UUID.
   */
  public static String getUUID() {
    return new com.eaio.uuid.UUID().toString();
  }

  /**
   * @param clusterId cluster id
   * @return time-based UUID prefixed with the cluster id so as to ensure uniqueness within cluster.
   */
  public static String getClusterUUID(int clusterId) {
    return new com.eaio.uuid.UUID()
        .toAppendable(new StringBuilder(38).append(clusterId).append('-')).toString();
  }

  /**
   * @param clusterIdPrefix cluster id prefix, ending in a `-` separator
   * @return time-based UUID prefixed with the cluster id so as to ensure uniqueness within cluster.
   */
  public static String getClusterUUID(String clusterIdPrefix) {
    return new com.eaio.uuid.UUID()
        .toAppendable(new StringBuilder(38).append(clusterIdPrefix)).toString();
  }


}
