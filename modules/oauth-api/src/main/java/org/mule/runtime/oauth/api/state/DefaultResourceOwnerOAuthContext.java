/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.oauth.api.state;

import static org.mule.oauth.client.api.state.DancerState.HAS_TOKEN;
import static org.mule.oauth.client.api.state.DancerState.NO_TOKEN;
import static org.mule.oauth.client.api.state.ResourceOwnerOAuthContextWithRefreshState.createRefreshOAuthContextLock;

import org.mule.oauth.client.api.state.DancerState;
import org.mule.oauth.client.api.state.ResourceOwnerOAuthContext;
import org.mule.oauth.client.api.state.ResourceOwnerOAuthContextWithRefreshState;
import org.mule.runtime.api.lock.LockFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * OAuth state for a particular resource owner which typically represents an user.
 *
 * @since 4.0, was ResourceOwnerOAuthContext in previous versions
 *
 * @deprecated Use {@link ResourceOwnerOAuthContextWithRefreshState} instead.
 */
@Deprecated
public final class DefaultResourceOwnerOAuthContext
    implements ResourceOwnerOAuthContext, org.mule.runtime.oauth.api.state.ResourceOwnerOAuthContext, Serializable {

  private static final long serialVersionUID = -4260965520423792113L;

  private final String resourceOwnerId;
  private transient Lock refreshUserOAuthContextLock;
  private transient DancerState dancerState;
  private String accessToken;
  private String refreshToken;
  private String state;
  private String expiresIn;
  private Map<String, Object> tokenResponseParameters = new HashMap<>();


  public DefaultResourceOwnerOAuthContext(final Lock refreshUserOAuthContextLock, final String resourceOwnerId) {
    this.refreshUserOAuthContextLock = refreshUserOAuthContextLock;
    this.resourceOwnerId = resourceOwnerId;
  }

  @Override
  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public String getRefreshToken() {
    return refreshToken;
  }

  @Override
  public String getState() {
    return state;
  }

  public void setAccessToken(final String accessToken) {
    this.accessToken = accessToken;
  }

  public void setRefreshToken(final String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void setExpiresIn(final String expiresIn) {
    this.expiresIn = expiresIn;
  }

  @Override
  public String getExpiresIn() {
    return expiresIn;
  }

  public void setState(final String state) {
    this.state = state;
  }

  @Override
  public Map<String, Object> getTokenResponseParameters() {
    return tokenResponseParameters;
  }

  public void setTokenResponseParameters(final Map<String, Object> tokenResponseParameters) {
    this.tokenResponseParameters = tokenResponseParameters;
  }

  /**
   * @return a lock that can be used to avoid concurrency problems trying to update oauth context.
   */
  public Lock getRefreshUserOAuthContextLock() {
    return refreshUserOAuthContextLock;
  }

  @Override
  public String getResourceOwnerId() {
    return resourceOwnerId == null ? DEFAULT_RESOURCE_OWNER_ID : resourceOwnerId;
  }

  public void setRefreshUserOAuthContextLock(Lock refreshUserOAuthContextLock) {
    this.refreshUserOAuthContextLock = refreshUserOAuthContextLock;
  }

  @Override
  public DancerState getDancerState() {
    return this.dancerState != null
        ? dancerState
        : accessToken == null ? NO_TOKEN : HAS_TOKEN;
  }

  @Override
  public void setDancerState(DancerState dancerState) {
    this.dancerState = dancerState;
  }

  @Override
  public Lock getRefreshOAuthContextLock(String lockNamePrefix, LockFactory lockFactory) {
    return createRefreshOAuthContextLock(lockNamePrefix, lockFactory, resourceOwnerId);
  }
}
