/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.oauth.api;

import org.mule.api.annotation.Experimental;
import org.mule.api.annotation.NoImplement;
import org.mule.oauth.client.api.AuthorizationCodeOAuthDancer;
import org.mule.oauth.client.api.ClientCredentialsOAuthDancer;
import org.mule.runtime.api.el.MuleExpressionLanguage;
import org.mule.runtime.api.lock.LockFactory;
import org.mule.runtime.api.service.Service;
import org.mule.runtime.oauth.api.builder.OAuthPlatformManagedDancerBuilder;

import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Allows for creation of OAuth dancer implementations for the supported grant types:
 * <ul>
 * <li>Authorization Code Grant Type</li>
 * <li>Client Credentials</li>
 * </ul>
 *
 * @since 4.0
 */
@NoImplement
public interface OAuthService extends Service {

  /**
   * Creates a builder for an {@link ClientCredentialsOAuthDancer} for client credentials grant type. The dancer will use the
   * given {@code lockProvider} and {@code tokensStore} to manage its internal state.
   *
   * @param lockProvider        a factory for {@link Lock}s, uniquely identified by the {@code name} passed to
   *                            {@link LockFactory#createLock(String)}.
   * @param tokensStore         the repository for the tokens for the returned {@link ClientCredentialsOAuthDancer dancer}.
   * @param expressionEvaluator the object used to evaluate expressions.
   * @return a builder for a client-credentials grant type dancer.
   */
  <T> org.mule.runtime.oauth.api.builder.OAuthClientCredentialsDancerBuilder clientCredentialsGrantTypeDancerBuilder(LockFactory lockProvider,
                                                                                                                     Map<String, T> tokensStore,
                                                                                                                     MuleExpressionLanguage expressionEvaluator);

  /**
   * Creates a builder for an {@link AuthorizationCodeOAuthDancer} for authorization code grant type. The dancer will use the
   * given {@code lockProvider} and {@code tokensStore} to manage its internal state.
   *
   * @param lockProvider        a factory for {@link Lock}s, uniquely identified by the {@code name} passed to
   *                            {@link LockFactory#createLock(String)}.
   * @param tokensStore         the repository for the tokens for the returned {@link AuthorizationCodeOAuthDancer dancer}.
   * @param expressionEvaluator the object used to evaluate expressions.
   * @return a builder for an authorization-code grant type dancer.
   */
  <T> org.mule.runtime.oauth.api.builder.OAuthAuthorizationCodeDancerBuilder authorizationCodeGrantTypeDancerBuilder(LockFactory lockProvider,
                                                                                                                     Map<String, T> tokensStore,
                                                                                                                     MuleExpressionLanguage expressionEvaluator);

  /**
   * Creates a builder for a {@link PlatformManagedOAuthDancer} that obtains access tokens that are provisioned and managed by the
   * Anypoint Platform.
   * <p>
   * Platform Managed OAuth is an experimental feature. It will only be enabled on selected environments and scenarios. Backwards
   * compatibility is not guaranteed.
   *
   * @param lockFactory         a factory for {@link Lock}s, uniquely identified by the {@code name} passed to
   *                            {@link LockFactory#createLock(String)}.
   * @param tokensStore         the repository for the tokens for the returned {@link ClientCredentialsOAuthDancer dancer}.
   * @param expressionEvaluator the object used to evaluate expressions.
   * @return a builder for a client-credentials grant type dancer.
   * @since 4.3.0
   */
  @Experimental
  default <T> OAuthPlatformManagedDancerBuilder platformManagedOAuthDancerBuilder(LockFactory lockFactory,
                                                                                  Map<String, T> tokensStore,
                                                                                  MuleExpressionLanguage expressionEvaluator) {
    throw new UnsupportedOperationException("Platform Managed OAuth is only supported in the Enterprise Edition");
  }
}
