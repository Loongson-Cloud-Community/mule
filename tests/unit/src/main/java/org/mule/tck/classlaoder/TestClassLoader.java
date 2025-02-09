/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.classlaoder;

import org.mule.runtime.api.util.Pair;
import org.mule.runtime.core.internal.util.EnumerationAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fake {@link ClassLoader} for testing purposes
 */
public class TestClassLoader extends ClassLoader {

  private final Map<String, Class> classes = new HashMap<>();
  private final Map<String, URL> resources = new HashMap<>();
  private final Map<String, InputStream> streamResources = new HashMap<>();
  private final Map<String, String> libraries = new HashMap<>();

  private final List<Pair<String, String>> invocations = new ArrayList<>();

  public TestClassLoader(ClassLoader parent) {
    super(parent);
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    invocations.add(new Pair<>("loadClass", name));

    return findClass(name);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    invocations.add(new Pair<>("findClass", name));

    Class aClass = classes.get(name);
    if (aClass == null) {
      throw new TestClassNotFoundException(name, this);
    }
    return aClass;
  }

  @Override
  public URL getResource(String s) {
    invocations.add(new Pair<>("getResource", s));

    URL url = resources.get(s);
    if (url == null && getParent() != null) {
      url = getParent().getResource(s);
    }
    return url;
  }

  @Override
  public InputStream getResourceAsStream(String s) {
    invocations.add(new Pair<>("getResourceAsStream", s));

    return streamResources.get(s);
  }

  @Override
  public Enumeration<URL> getResources(String s) throws IOException {
    invocations.add(new Pair<>("getResources", s));

    return findResources(s);
  }

  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {
    invocations.add(new Pair<>("findResources", name));

    return new EnumerationAdapter<>(resources.values());
  }

  @Override
  protected URL findResource(String s) {
    invocations.add(new Pair<>("findResource", s));

    return resources.get(s);
  }

  @Override
  protected String findLibrary(String s) {
    invocations.add(new Pair<>("findLibrary", s));

    return libraries.get(s);
  }

  public void addClass(String className, Class aClass) {
    classes.put(className, aClass);
  }

  public void addResource(String resourceName, URL resourceUrl) {
    resources.put(resourceName, resourceUrl);
  }

  public void addStreamResource(String resourceName, InputStream resourceStream) {
    streamResources.put(resourceName, resourceStream);
  }

  public void addLibrary(String libraryName, String libraryPath) {
    libraries.put(libraryName, libraryPath);
  }

  @Override
  protected synchronized Class<?> loadClass(String s, boolean b) throws ClassNotFoundException {
    invocations.add(new Pair<>("loadClass", s));

    return loadClass(s);
  }

  public List<Pair<String, String>> getInvocations() {
    return invocations;
  }

  public static class TestClassNotFoundException extends ClassNotFoundException {

    private static final long serialVersionUID = 1L;

    private final ClassLoader classLoader;

    public TestClassNotFoundException(String s, ClassLoader classLoader) {
      super(s);
      this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
      return classLoader;
    }
  }
}
