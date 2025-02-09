/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.runner.api;

import static java.io.File.pathSeparator;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses java system properties to get the classpath {@link URL}s.
 *
 * @since 4.0
 */
public class ClassPathUrlProvider {

  protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());
  private final List<URL> urls;

  /**
   * Creates an instance of the provider that uses system properties to get the classpath {@link URL}s.
   */
  public ClassPathUrlProvider() {
    this(new ArrayList<>());
  }


  /**
   * Creates an instance of the provided with a list of {@link URL}s to be appended in addition to the classpath ones.
   *
   * @param urls {@link URL}s to be added to the ones already in classpath, not null or empty.
   */
  public ClassPathUrlProvider(List<URL> urls) {
    requireNonNull(urls, "urls cannot be null");

    this.urls = readUrlsFromSystemProperties();
    this.urls.addAll(urls);
  }

  /**
   * @return Gets the urls from the {@code sun.boot.class.path} and {@code java.class.path} and {@code surefire.test.class.path}
   *         system properties.
   */
  private List<URL> readUrlsFromSystemProperties() {
    final Set<URL> urls = new LinkedHashSet<>();
    addUrlsFromSystemProperty(urls, "jdk.module.path");
    addUrlsFromSystemProperty(urls, "java.class.path");
    addUrlsFromSystemProperty(urls, "sun.boot.class.path");
    addUrlsFromSystemProperty(urls, "surefire.test.class.path");

    if (logger.isDebugEnabled()) {
      StringBuilder builder = new StringBuilder("ClassPath:");
      urls.stream().forEach(url -> builder.append(pathSeparator).append(url));
      logger.debug(builder.toString());
    }

    return Lists.newArrayList(urls);
  }

  public List<URL> getURLs() {
    return this.urls;
  }

  protected void addUrlsFromSystemProperty(final Collection<URL> urls, final String propertyName) {
    String property = System.getProperty(propertyName);
    if (property != null) {
      for (String file : property.split(pathSeparator)) {
        try {
          urls.add(new File(file).toURI().toURL());
        } catch (MalformedURLException e) {
          throw new IllegalArgumentException("Cannot create a URL from file path: " + file, e);
        }
      }
    }
  }

}
