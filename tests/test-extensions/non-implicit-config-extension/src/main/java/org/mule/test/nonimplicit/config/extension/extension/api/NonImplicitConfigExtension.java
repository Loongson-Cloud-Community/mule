/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.nonimplicit.config.extension.extension.api;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.NoImplicit;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.test.nonimplicit.config.extension.extension.internal.NonImplicitConnectionProvider;
import org.mule.test.nonimplicit.config.extension.extension.internal.NonImplicitOperations;

@Extension(name = "Non Implicit")
@Operations({NonImplicitOperations.class})
@Xml(namespace = "http://www.mulesoft.org/schema/mule/implicit", prefix = "non-implicit")
@ConnectionProviders(NonImplicitConnectionProvider.class)
@NoImplicit
public class NonImplicitConfigExtension implements Initialisable, Startable, MuleContextAware {

  private MuleContext muleContext;
  private int initialise = 0;
  private int start = 0;

  @Parameter
  @Optional
  private String optionalNoDefault;

  @Parameter
  @Optional(defaultValue = "#[vars.number]")
  private Integer optionalWithDefault;

  @org.mule.sdk.api.annotation.param.ParameterGroup(name = "nullSafeGroup")
  private NullSafeParameterGroup nullSafeGroup;

  @ParameterGroup(name = "nullSafeGroupShowInDsl", showInDsl = true)
  private NullSafeParameterGroupShowInDsl nullSafeGroupShowInDsl;

  @Override
  public void initialise() throws InitialisationException {
    initialise++;
  }

  @Override
  public void setMuleContext(MuleContext context) {
    muleContext = context;
  }

  @Override
  public void start() throws MuleException {
    start++;
  }

  public MuleContext getMuleContext() {
    return muleContext;
  }

  public int getInitialise() {
    return initialise;
  }

  public int getStart() {
    return start;
  }

  public String getOptionalNoDefault() {
    return optionalNoDefault;
  }

  public Integer getOptionalWithDefault() {
    return optionalWithDefault;
  }

  public NullSafeParameterGroup getNullSafeGroup() {
    return nullSafeGroup;
  }

  public void setNullSafeGroup(NullSafeParameterGroup nullSafeGroup) {
    this.nullSafeGroup = nullSafeGroup;
  }

  public NullSafeParameterGroupShowInDsl getNullSafeGroupShowInDsl() {
    return nullSafeGroupShowInDsl;
  }

  public void setNullSafeGroupShowInDsl(NullSafeParameterGroupShowInDsl nullSafeGroupShowInDsl) {
    this.nullSafeGroupShowInDsl = nullSafeGroupShowInDsl;
  }
}
