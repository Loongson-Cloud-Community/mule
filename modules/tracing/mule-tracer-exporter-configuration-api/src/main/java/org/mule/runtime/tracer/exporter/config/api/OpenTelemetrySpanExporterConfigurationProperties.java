/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.tracer.exporter.config.api;

/**
 * Properties for Span Configuration Exporter.
 *
 * @since 4.5.0
 */
public class OpenTelemetrySpanExporterConfigurationProperties {

  private static final String MULE_OPEN_TELEMETRY_EXPORTER = "mule.openTelemetry.tracer.exporter";

  private OpenTelemetrySpanExporterConfigurationProperties() {}

  public static final String USE_MULE_OPEN_TELEMETRY_EXPORTER_SNIFFER = MULE_OPEN_TELEMETRY_EXPORTER + ".use.sniffer";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_ENABLED = MULE_OPEN_TELEMETRY_EXPORTER + ".enabled";

  public static final String MULE_OPEN_TELEMETRY_TRACING_CONFIGURATION_FILE_PATH =
      MULE_OPEN_TELEMETRY_EXPORTER + ".configurationFilePath";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_CONFIGURATION_WATCHER_DEFAULT_DELAY_PROPERTY =
      MULE_OPEN_TELEMETRY_EXPORTER + ".configurationFileWatcherDelay";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_TYPE = MULE_OPEN_TELEMETRY_EXPORTER + ".type";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_TLS_ENABLED = MULE_OPEN_TELEMETRY_EXPORTER + ".tls.enabled";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_CERT_FILE_LOCATION =
      MULE_OPEN_TELEMETRY_EXPORTER + "tls.certFileLocation";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_KEY_FILE_LOCATION =
      MULE_OPEN_TELEMETRY_EXPORTER + ".tls.keyFileLocation";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_CA_FILE_LOCATION = MULE_OPEN_TELEMETRY_EXPORTER + ".tls.caFileLocation";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_COMPRESSION_TYPE = MULE_OPEN_TELEMETRY_EXPORTER + ".compression";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_HEADERS = MULE_OPEN_TELEMETRY_EXPORTER + ".headers";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_TIMEOUT = MULE_OPEN_TELEMETRY_EXPORTER + ".timeout";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_ENDPOINT = MULE_OPEN_TELEMETRY_EXPORTER + ".endpoint";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_MAX_BATCH_SIZE = MULE_OPEN_TELEMETRY_EXPORTER + ".batch.maxSize";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_BATCH_QUEUE_SIZE =
      MULE_OPEN_TELEMETRY_EXPORTER + ".batch.queueSize";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_BATCH_SCHEDULED_DELAY =
      MULE_OPEN_TELEMETRY_EXPORTER + ".batch.scheduledDelay";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_BACKOFF_MAX_ATTEMPTS =
      MULE_OPEN_TELEMETRY_EXPORTER + ".backoff.maxAttempts";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_INITIAL_BACKOFF =
      MULE_OPEN_TELEMETRY_EXPORTER + ".backoff.initial";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_MAX_BACKOFF = MULE_OPEN_TELEMETRY_EXPORTER + ".backoff.maximum";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_BACKOFF_MULTIPLIER =
      MULE_OPEN_TELEMETRY_EXPORTER + ".backoff.multiplier";

  public static final String MULE_OPEN_TELEMETRY_EXPORTER_METRICS_LOG_FREQUENCY =
      MULE_OPEN_TELEMETRY_EXPORTER + ".metricsLogFrequency";

}
