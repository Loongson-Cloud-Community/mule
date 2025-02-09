/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mule.runtime.core.internal.util.CompositeClassLoader.from;

import org.mule.tck.classlaoder.TestClassLoader;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.tck.util.EnumerationMatcher;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

@SmallTest
public class CompositeClassLoaderTestCase extends AbstractMuleTestCase {

  private static final String CLASS_NAME = "java.lang.Object";
  private static final Class APP_LOADED_CLASS = Object.class;
  private static final Class PLUGIN_LOADED_CLASS = String.class;

  private static final String RESOURCE_NAME = "dummy.txt";
  private static final String PLUGIN_RESOURCE_NAME = "plugin.txt";

  private final URL APP_LOADED_RESOURCE;
  private final URL PLUGIN_LOADED_RESOURCE;

  private final InputStream APP_LOADED_STREAM_RESOURCE = mock(InputStream.class);
  private final InputStream PLUGIN_LOADED_STREAM_RESOURCE = mock(InputStream.class);
  private final TestClassLoader appClassLoader = new TestClassLoader(null);
  private final TestClassLoader pluginClassLoader = new TestClassLoader(null);


  public CompositeClassLoaderTestCase() throws MalformedURLException {
    APP_LOADED_RESOURCE = new URL("file:///app.txt");
    PLUGIN_LOADED_RESOURCE = new URL("file:///plugin.txt");
  }

  @Test
  public void loadsClassFromAppFirst() throws Exception {
    appClassLoader.addClass(CLASS_NAME, APP_LOADED_CLASS);
    pluginClassLoader.addClass(CLASS_NAME, PLUGIN_LOADED_CLASS);

    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    Class<?> aClass = compositeApplicationClassLoader.loadClass(CLASS_NAME);
    assertThat(aClass, equalTo(APP_LOADED_CLASS));
  }

  @Test
  public void loadsClassFromPluginWhenIsNotDefinedInApp() throws Exception {
    pluginClassLoader.addClass(CLASS_NAME, PLUGIN_LOADED_CLASS);

    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    Class<?> aClass = compositeApplicationClassLoader.loadClass(CLASS_NAME);
    assertThat(aClass, equalTo(PLUGIN_LOADED_CLASS));
  }

  @Test
  public void loadsClassFromPluginNotDefineInAppButLoadedFromExtendedClassLoader() throws Exception {
    pluginClassLoader.addClass(CLASS_NAME, PLUGIN_LOADED_CLASS);

    CompositeClassLoader compositeApplicationClassLoader = from(pluginClassLoader, appClassLoader);
    ClassLoader extendedClassLoader = new URLClassLoader(new URL[0], compositeApplicationClassLoader);

    Class<?> aClass = extendedClassLoader.loadClass(CLASS_NAME);
    assertThat(aClass, equalTo(PLUGIN_LOADED_CLASS));
  }

  @Test(expected = ClassNotFoundException.class)
  public void failsToLoadClassWhenIsNotDefinedInAnyClassLoader() throws Exception {
    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    compositeApplicationClassLoader.loadClass(CLASS_NAME);
  }

  @Test
  public void loadsResourceFromAppFirst() throws Exception {
    appClassLoader.addResource(RESOURCE_NAME, APP_LOADED_RESOURCE);
    pluginClassLoader.addResource(RESOURCE_NAME, PLUGIN_LOADED_RESOURCE);

    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    URL resource = compositeApplicationClassLoader.getResource(RESOURCE_NAME);
    assertThat(resource, equalTo(APP_LOADED_RESOURCE));
  }

  @Test
  public void loadsResourceFromPluginWhenIsNotDefinedInApp() throws Exception {
    pluginClassLoader.addResource(RESOURCE_NAME, PLUGIN_LOADED_RESOURCE);

    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    URL resource = compositeApplicationClassLoader.getResource(RESOURCE_NAME);
    assertThat(resource, equalTo(PLUGIN_LOADED_RESOURCE));
  }

  @Test
  public void returnsNullResourceWhenIsNotDefinedInAnyClassLoader() throws Exception {
    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    URL resource = compositeApplicationClassLoader.getResource(RESOURCE_NAME);
    assertThat(resource, equalTo(null));
  }

  @Test
  public void loadsStreamResourceFromAppFirst() throws Exception {
    appClassLoader.addStreamResource(RESOURCE_NAME, APP_LOADED_STREAM_RESOURCE);
    pluginClassLoader.addStreamResource(RESOURCE_NAME, PLUGIN_LOADED_STREAM_RESOURCE);

    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    InputStream resourceAsStream = compositeApplicationClassLoader.getResourceAsStream(RESOURCE_NAME);
    assertThat(resourceAsStream, equalTo(APP_LOADED_STREAM_RESOURCE));
  }

  @Test
  public void loadsStreamResourceFromPluginWhenIsNotDefinedInApp() throws Exception {
    pluginClassLoader.addStreamResource(RESOURCE_NAME, PLUGIN_LOADED_STREAM_RESOURCE);

    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    InputStream resourceAsStream = compositeApplicationClassLoader.getResourceAsStream(RESOURCE_NAME);
    assertThat(resourceAsStream, equalTo(PLUGIN_LOADED_STREAM_RESOURCE));
  }

  @Test
  public void returnsNullStreamResourceWhenIsNotDefinedInAnyClassLoader() throws Exception {
    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    InputStream resourceAsStream = compositeApplicationClassLoader.getResourceAsStream(RESOURCE_NAME);
    assertThat(resourceAsStream, equalTo(null));
  }

  @Test
  public void getsResourcesFromAppAndPluginClassLoader() throws Exception {
    appClassLoader.addResource(RESOURCE_NAME, APP_LOADED_RESOURCE);
    pluginClassLoader.addResource(PLUGIN_RESOURCE_NAME, PLUGIN_LOADED_RESOURCE);

    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    Enumeration<URL> resources = compositeApplicationClassLoader.getResources(RESOURCE_NAME);

    List<URL> expectedResources = new LinkedList<URL>();
    expectedResources.add(APP_LOADED_RESOURCE);
    expectedResources.add(PLUGIN_LOADED_RESOURCE);

    assertThat(resources, EnumerationMatcher.equalTo(expectedResources));
  }

  @Test
  public void filtersResourcesDuplicatedInAppAndPluginClassLoader() throws Exception {
    appClassLoader.addResource(RESOURCE_NAME, APP_LOADED_RESOURCE);
    pluginClassLoader.addResource(RESOURCE_NAME, APP_LOADED_RESOURCE);

    CompositeClassLoader compositeApplicationClassLoader = from(appClassLoader, pluginClassLoader);

    Enumeration<URL> resources = compositeApplicationClassLoader.getResources(RESOURCE_NAME);

    List<URL> expectedResources = new LinkedList<URL>();
    expectedResources.add(APP_LOADED_RESOURCE);
    expectedResources.add(APP_LOADED_RESOURCE);

    assertThat(resources, EnumerationMatcher.equalTo(expectedResources));
  }

  @Test
  public void twoClassLoadersFromTheSameOneAreTheSameInstanceOneArg() {
    CompositeClassLoader cl1 = from(appClassLoader);
    CompositeClassLoader cl2 = from(appClassLoader);
    assertThat(cl2, is(cl1));
  }

  @Test
  public void twoClassLoadersFromTheSameOneAreTheSameInstanceTwoArgs() {
    CompositeClassLoader cl1 = from(appClassLoader, pluginClassLoader);
    CompositeClassLoader cl2 = from(appClassLoader, pluginClassLoader);
    assertThat(cl2, is(cl1));
  }

  @Test
  public void twoClassLoadersFromTheSameOneAreTheSameInstanceMoreArgs() {
    CompositeClassLoader cl1 = from(appClassLoader, pluginClassLoader, pluginClassLoader);
    CompositeClassLoader cl2 = from(appClassLoader, pluginClassLoader, pluginClassLoader);
    assertThat(cl2, is(cl1));
  }

}
