/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.heisenberg.extension;

import static org.mule.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static org.mule.runtime.extension.api.annotation.source.SourceClusterSupport.DEFAULT_PRIMARY_NODE_ONLY;
import static org.mule.runtime.extension.api.runtime.source.BackPressureMode.DROP;
import static org.mule.runtime.extension.api.runtime.source.BackPressureMode.FAIL;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Streaming;
import org.mule.runtime.extension.api.annotation.notification.Fires;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.source.BackPressure;
import org.mule.runtime.extension.api.annotation.source.ClusterSupport;
import org.mule.runtime.extension.api.annotation.source.EmitsResponse;

@Alias("listen-payments-cluster")
@EmitsResponse
@Fires(SourceNotificationProvider.class)
@Streaming
@MediaType(TEXT_PLAIN)
@BackPressure(defaultMode = FAIL, supportedModes = {FAIL, DROP})
@ClusterSupport(DEFAULT_PRIMARY_NODE_ONLY)
public class HeisenbergClusterSource extends SdkHeisenbergSource {
}
