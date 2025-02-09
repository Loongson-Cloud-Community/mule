/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.security;

import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.security.Authentication;
import org.mule.runtime.api.security.SecurityContext;
import org.mule.runtime.api.security.UnknownAuthenticationTypeException;
import org.mule.runtime.core.internal.security.DefaultSecurityContextFactory;

public abstract class AbstractSecurityProvider extends AbstractComponent implements SecurityProvider, Initialisable {

  private String name;
  private SecurityContextFactory securityContextFactory;

  public AbstractSecurityProvider(String name) {
    this.name = name;
  }

  @Override
  public final void initialise() throws InitialisationException {
    doInitialise();

    if (securityContextFactory == null) {
      securityContextFactory = new DefaultSecurityContextFactory();
    }
  }

  protected void doInitialise() throws InitialisationException {
    // do nothing by default
  }

  @Override
  public boolean supports(Class<?> aClass) {
    return Authentication.class.isAssignableFrom(aClass);
  }

  @Override
  public SecurityContext createSecurityContext(Authentication authentication) throws UnknownAuthenticationTypeException {
    return securityContextFactory.create(authentication);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public SecurityContextFactory getSecurityContextFactory() {
    return securityContextFactory;
  }

  public void setSecurityContextFactory(SecurityContextFactory securityContextFactory) {
    this.securityContextFactory = securityContextFactory;
  }

}
