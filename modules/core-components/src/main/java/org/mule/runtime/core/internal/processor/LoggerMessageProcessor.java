/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.processor;

import static java.util.Arrays.asList;
import static org.mule.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static org.mule.runtime.api.util.MuleSystemProperties.MULE_LOGGING_BLOCKING_CATEGORIES;
import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.BLOCKING;
import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_LITE;
import static org.mule.runtime.core.api.util.StringUtils.EMPTY;

import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.el.ExtendedExpressionManager;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.StringUtils;
import org.mule.runtime.core.internal.interception.HasParamsAsTemplateProcessor;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessageProcessor implementation that logs the current element of a value evaluated from it using an expression evaluator. By
 * default the current messages is logged using the {@link Level#INFO} level to the
 * 'org.mule.runtime.core.internal.processor.LoggerMessageProcessor' category. The level and category can both be configured to
 * suit your needs.
 */
public class LoggerMessageProcessor extends AbstractComponent
    implements HasParamsAsTemplateProcessor, Processor, Initialisable, MuleContextAware {

  // TODO - MULE-16446: Logger execution type should be defined according to the appender used
  private static final String BLOCKING_CATEGORIES_PROPERTY = System.getProperty(MULE_LOGGING_BLOCKING_CATEGORIES, "");
  private static final Set<String> BLOCKING_CATEGORIES = new HashSet<>(asList(BLOCKING_CATEGORIES_PROPERTY.split(",")));
  private static final String WILDCARD = "*";

  protected transient Logger logger;

  protected String message;
  protected String category;
  protected String level = "INFO";

  protected MuleContext muleContext;
  ExtendedExpressionManager expressionManager;

  private volatile ProcessingType processingType;

  @Override
  public void initialise() throws InitialisationException {
    initLogger();
    initProcessingTypeIfPossible();
    expressionManager = muleContext.getExpressionManager();
  }

  protected void initLogger() {
    if (category != null) {
      logger = LoggerFactory.getLogger(category);
    } else {
      logger = LoggerFactory.getLogger(LoggerMessageProcessor.class);
    }
  }

  protected void initProcessingTypeIfPossible() {
    if (getBlockingCategories().size() == 1 && getBlockingCategories().contains(EMPTY)) {
      processingType = CPU_LITE;
    } else if (getBlockingCategories().contains(WILDCARD)) {
      processingType = BLOCKING;
    }
  }

  @Override
  public CoreEvent process(CoreEvent event) throws MuleException {
    log(event);
    return event;
  }

  @Override
  public ProcessingType getProcessingType() {
    if (processingType == null) {
      synchronized (this) {
        if (processingType == null) {
          processingType = isBlocking(category) ? BLOCKING : CPU_LITE;
        }
      }
    }
    return processingType;
  }

  private boolean isBlocking(String category) {
    return getBlockingCategories().stream().anyMatch(blockingCategory -> blockingCategory.equals(category) ||
        (category != null && category.startsWith(blockingCategory + ".")));
  }

  protected void log(CoreEvent event) {
    if (event == null) {
      logWithLevel(null);
    } else {
      if (StringUtils.isEmpty(message)) {
        logWithLevel(event.getMessage());
      } else {
        LogLevel logLevel = LogLevel.valueOf(level);
        if (LogLevel.valueOf(level).isEnabled(logger)) {
          logLevel.log(logger, expressionManager.parseLogTemplate(message, event, getLocation(), NULL_BINDING_CONTEXT));
        }
      }
    }
  }

  protected void logWithLevel(Object object) {
    LogLevel logLevel = LogLevel.valueOf(level);
    if (logLevel.isEnabled(logger)) {
      logLevel.log(logger, object);
    }
  }

  @Override
  public void setMuleContext(MuleContext muleContext) {
    this.muleContext = muleContext;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setLevel(String level) {
    this.level = level.toUpperCase();
  }

  public enum LogLevel {

    ERROR {

      @Override
      public void log(Logger logger, Object object) {
        logger.error(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isErrorEnabled();
      }
    },
    WARN {

      @Override
      public void log(Logger logger, Object object) {
        logger.warn(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isWarnEnabled();
      }
    },
    INFO {

      @Override
      public void log(Logger logger, Object object) {
        logger.info(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isInfoEnabled();
      }
    },
    DEBUG {

      @Override
      public void log(Logger logger, Object object) {
        logger.debug(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isDebugEnabled();
      }
    },
    TRACE {

      @Override
      public void log(Logger logger, Object object) {
        logger.trace(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isTraceEnabled();
      }
    };

    public abstract void log(Logger logger, Object object);

    public abstract boolean isEnabled(Logger logger);
  }

  protected Set<String> getBlockingCategories() {
    return BLOCKING_CATEGORIES;
  }
}
