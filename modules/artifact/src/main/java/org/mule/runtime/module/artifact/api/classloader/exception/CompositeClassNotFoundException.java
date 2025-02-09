/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.api.classloader.exception;

import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.mule.runtime.api.exception.MuleException.MULE_VERBOSE_EXCEPTIONS;

import org.mule.api.annotation.NoInstantiate;
import org.mule.runtime.api.util.LazyValue;
import org.mule.runtime.module.artifact.api.classloader.LookupStrategy;

import java.util.List;

/**
 * Extends {@link ClassNotFoundException}, composing the individual exceptions of each place where the class was looked for and
 * wasn't found.
 */
@NoInstantiate
public final class CompositeClassNotFoundException extends ClassNotFoundException {

  private static final long serialVersionUID = -6941980241656380056L;

  private final String className;
  private final LookupStrategy lookupStrategy;
  private final List<ClassNotFoundException> exceptions;
  private final LazyValue<String> message;

  /**
   * Builds the exception.
   *
   * @param className      the name of the class that was trying to be loaded.
   * @param lookupStrategy the lookupStrategy that was used to load the class.
   * @param exceptions     the exceptions thrown by each individual classloader that was used for the loading.
   */
  public CompositeClassNotFoundException(String className, LookupStrategy lookupStrategy,
                                         List<ClassNotFoundException> exceptions) {
    super(null, exceptions.get(0));
    message = new LazyValue<>(() -> format("Cannot load class '%s': %s", className,
                                           exceptions.stream()
                                               .map((e) -> lineSeparator() + "\t" + e.getMessage())
                                               .collect(toList())));
    this.className = className;
    this.lookupStrategy = lookupStrategy;
    this.exceptions = unmodifiableList(exceptions);
  }

  /**
   * @return the name of the class that was trying to be loaded
   */
  public String getClassName() {
    return className;
  }

  /**
   * @return the lookupStrategy that was used to load the class.
   */
  public LookupStrategy getLookupStrategy() {
    return lookupStrategy;
  }

  /**
   * @return the exceptions thrown by each individual classloader that was used for the loading.
   */
  public List<ClassNotFoundException> getExceptions() {
    return exceptions;
  }

  @Override
  public String getMessage() {
    return message.get();
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    // This might happen during logger initialization,
    // so the implementation from MuleException cannot be used since it requires a logger.
    if (getBoolean(MULE_VERBOSE_EXCEPTIONS)) {
      return super.fillInStackTrace();
    } else {
      return this;
    }
  }
}
