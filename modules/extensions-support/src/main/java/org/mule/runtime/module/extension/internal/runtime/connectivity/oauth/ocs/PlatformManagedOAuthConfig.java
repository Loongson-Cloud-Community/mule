/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.runtime.connectivity.oauth.ocs;

import static org.mule.runtime.core.api.util.StringUtils.sanitizeUrl;
import static org.mule.runtime.extension.internal.ocs.OCSConstants.OCS_API_VERSION;
import static org.mule.runtime.extension.internal.ocs.OCSConstants.OCS_CLIENT_ID;
import static org.mule.runtime.extension.internal.ocs.OCSConstants.OCS_CLIENT_SECRET;
import static org.mule.runtime.extension.internal.ocs.OCSConstants.OCS_ORG_ID;
import static org.mule.runtime.extension.internal.ocs.OCSConstants.OCS_PLATFORM_AUTH_DEFAULT_PATH;
import static org.mule.runtime.extension.internal.ocs.OCSConstants.OCS_PLATFORM_AUTH_PATH;
import static org.mule.runtime.extension.internal.ocs.OCSConstants.OCS_PLATFORM_AUTH_URL;
import static org.mule.runtime.extension.internal.ocs.OCSConstants.OCS_SERVICE_URL;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;

import org.mule.runtime.api.component.ConfigurationProperties;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.connection.ConnectionProviderModel;
import org.mule.runtime.extension.api.connectivity.oauth.OAuthGrantType;
import org.mule.runtime.extension.api.connectivity.oauth.PlatformManagedOAuthGrantType;
import org.mule.runtime.module.extension.internal.runtime.connectivity.oauth.CustomOAuthParameters;
import org.mule.runtime.module.extension.internal.runtime.connectivity.oauth.OAuthConfig;

import java.nio.charset.Charset;

/**
 * {@link OAuthConfig} implementation for the {@link PlatformManagedOAuthGrantType}
 *
 * @since 4.3.0
 */
public class PlatformManagedOAuthConfig extends OAuthConfig<PlatformManagedOAuthGrantType> {

  private final String connectionUri;
  private final String serviceUrl;
  private final String platformAuthUrl;
  private final String clientId;
  private final String clientSecret;
  private final String orgId;
  private final String apiVersion;
  private final Charset encoding;
  private final PlatformManagedOAuthGrantType grantType;
  private final ExtensionModel extensionModel;
  private final ConnectionProviderModel delegateConnectionProviderModel;
  private final OAuthGrantType delegateGrantType;

  public static PlatformManagedOAuthConfig from(String ownerConfigName,
                                                String connectionUri,
                                                PlatformManagedOAuthGrantType grantType,
                                                ExtensionModel extensionModel,
                                                ConnectionProviderModel delegateConnectionProviderModel,
                                                OAuthGrantType delegateGrantType,
                                                ConfigurationProperties configurationProperties) {
    return new PlatformManagedOAuthConfig(ownerConfigName,
                                          connectionUri,
                                          getProperty(configurationProperties, OCS_SERVICE_URL),
                                          resolvePlatformAuthUrl(configurationProperties),
                                          getProperty(configurationProperties, OCS_CLIENT_ID),
                                          getProperty(configurationProperties, OCS_CLIENT_SECRET),
                                          getProperty(configurationProperties, OCS_ORG_ID),
                                          getProperty(configurationProperties, OCS_API_VERSION, false),
                                          UTF_8,
                                          grantType,
                                          extensionModel,
                                          delegateConnectionProviderModel,
                                          delegateGrantType);
  }

  private static String getProperty(ConfigurationProperties configurationProperties, String key, boolean isRequired) {
    if (isRequired) {
      return configurationProperties.resolveStringProperty(key)
          .orElseThrow(() -> new IllegalArgumentException(format("OCS property '%s' has not been set", key)));
    } else {
      return configurationProperties.resolveStringProperty(key)
          .orElse(null);
    }
  }

  private static String getProperty(ConfigurationProperties configurationProperties, String key) {
    return getProperty(configurationProperties, key, true);
  }

  private static String resolvePlatformAuthUrl(ConfigurationProperties configurationProperties) {
    return sanitizeUrl(getProperty(configurationProperties, OCS_PLATFORM_AUTH_URL))
        + configurationProperties.resolveStringProperty(OCS_PLATFORM_AUTH_PATH).orElse(OCS_PLATFORM_AUTH_DEFAULT_PATH);
  }

  public PlatformManagedOAuthConfig(String ownerConfigName,
                                    String connectionUri,
                                    String serviceUrl,
                                    String platformAuthUrl,
                                    String clientId,
                                    String clientSecret,
                                    String orgId,
                                    String apiVersion,
                                    Charset encoding,
                                    PlatformManagedOAuthGrantType grantType,
                                    ExtensionModel extensionModel,
                                    ConnectionProviderModel delegateConnectionProviderModel,
                                    OAuthGrantType delegateGrantType) {
    super(ownerConfigName, empty(), new CustomOAuthParameters(), emptyMap());
    this.connectionUri = connectionUri;
    this.serviceUrl = serviceUrl;
    this.platformAuthUrl = platformAuthUrl;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.orgId = orgId;
    this.apiVersion = apiVersion;
    this.encoding = encoding;
    this.grantType = grantType;
    this.extensionModel = extensionModel;
    this.delegateConnectionProviderModel = delegateConnectionProviderModel;
    this.delegateGrantType = delegateGrantType;
  }

  public String getConnectionUri() {
    return connectionUri;
  }

  public String getServiceUrl() {
    return serviceUrl;
  }

  public String getPlatformAuthUrl() {
    return platformAuthUrl;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getOrgId() {
    return orgId;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public Charset getEncoding() {
    return encoding;
  }

  public ConnectionProviderModel getDelegateConnectionProviderModel() {
    return delegateConnectionProviderModel;
  }

  public OAuthGrantType getDelegateGrantType() {
    return delegateGrantType;
  }

  @Override
  public PlatformManagedOAuthGrantType getGrantType() {
    return grantType;
  }

  public ExtensionModel getExtensionModel() {
    return extensionModel;
  }

}
