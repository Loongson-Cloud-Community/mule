/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.container.internal;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.rules.ExpectedException.none;
import static org.mule.runtime.container.internal.MuleClassLoaderLookupPolicy.invalidLookupPolicyOverrideError;
import static org.mule.runtime.module.artifact.api.classloader.ChildFirstLookupStrategy.CHILD_FIRST;
import static org.mule.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy.PARENT_FIRST;
import static org.mule.runtime.module.artifact.api.classloader.ParentOnlyLookupStrategy.PARENT_ONLY;

import org.mule.runtime.module.artifact.api.classloader.ChildFirstLookupStrategy;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import org.mule.runtime.module.artifact.api.classloader.LookupStrategy;
import org.mule.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SmallTest
public class MuleClassLoaderLookupPolicyProviderTestCase extends AbstractMuleTestCase {

  private static final String FOO_PACKAGE = "org.foo";
  private static final String FOO_PACKAGE_PREFIX = FOO_PACKAGE + ".";
  private static final String FOO_CLASS = FOO_PACKAGE_PREFIX + "Object";
  private static final String SYSTEM_PACKAGE = "java";
  private static final String JAVA_PACKAGE = SYSTEM_PACKAGE + ".lang";
  private static final String JAVA_PACKAGE_PREFIX = JAVA_PACKAGE + ".";

  @Rule
  public ExpectedException expectedException = none();

  @Test
  public void extendingCustomLookupStrategyForSystemPackage() {
    final String overrideClassName = Object.class.getPackage().getName();
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(invalidLookupPolicyOverrideError(overrideClassName, CHILD_FIRST));

    new MuleClassLoaderLookupPolicy(emptyMap(), singleton(JAVA_PACKAGE))
        .extend(singletonMap(overrideClassName, CHILD_FIRST));
  }

  @Test
  public void returnsConfiguredLookupStrategy() {
    MuleClassLoaderLookupPolicy lookupPolicy =
        new MuleClassLoaderLookupPolicy(singletonMap(JAVA_PACKAGE, CHILD_FIRST),
                                        emptySet());

    LookupStrategy lookupStrategy = lookupPolicy.getClassLookupStrategy(Object.class.getName());
    assertThat(lookupStrategy, sameInstance(CHILD_FIRST));

    lookupPolicy =
        new MuleClassLoaderLookupPolicy(singletonMap(JAVA_PACKAGE_PREFIX, CHILD_FIRST), emptySet());

    lookupStrategy = lookupPolicy.getClassLookupStrategy(Object.class.getName());
    assertThat(lookupStrategy, sameInstance(CHILD_FIRST));
  }

  @Test
  public void usesParentOnlyForSystemPackage() {
    ClassLoaderLookupPolicy lookupPolicy = new MuleClassLoaderLookupPolicy(emptyMap(), singleton(JAVA_PACKAGE));

    assertThat(lookupPolicy.getClassLookupStrategy(Object.class.getName()), sameInstance(PARENT_ONLY));
  }

  @Test
  public void usesChildFirstForNoConfiguredPackage() {
    ClassLoaderLookupPolicy lookupPolicy = new MuleClassLoaderLookupPolicy(emptyMap(), emptySet());

    assertThat(lookupPolicy.getClassLookupStrategy(FOO_CLASS), sameInstance(CHILD_FIRST));
  }

  @Test
  public void extendsPolicy() {
    ClassLoaderLookupPolicy lookupPolicy = new MuleClassLoaderLookupPolicy(emptyMap(), emptySet());

    final ClassLoaderLookupPolicy extendedPolicy =
        lookupPolicy.extend(singletonMap(FOO_PACKAGE, PARENT_FIRST));

    assertThat(extendedPolicy.getClassLookupStrategy(FOO_CLASS), sameInstance(PARENT_FIRST));
  }

  @Test
  public void maintainsOriginalLookupStrategy() {
    ClassLoaderLookupPolicy lookupPolicy =
        new MuleClassLoaderLookupPolicy(singletonMap(FOO_PACKAGE,
                                                     new ContainerOnlyLookupStrategy(getClass().getClassLoader())),
                                        emptySet());

    final ClassLoaderLookupPolicy extendedPolicy =
        lookupPolicy.extend(singletonMap(FOO_PACKAGE, PARENT_FIRST));

    assertThat(extendedPolicy.getClassLookupStrategy(FOO_CLASS), instanceOf(ContainerOnlyLookupStrategy.class));
    assertThat(extendedPolicy.getClassLookupStrategy(FOO_CLASS).getClassLoaders(getClass().getClassLoader()),
               hasItem(getClass().getClassLoader()));
  }

  @Test
  public void maintainsOriginalLookupStrategyExplicitNotOverwrite() {
    ClassLoaderLookupPolicy lookupPolicy =
        new MuleClassLoaderLookupPolicy(singletonMap(FOO_PACKAGE,
                                                     new ContainerOnlyLookupStrategy(getClass().getClassLoader())),
                                        emptySet());

    final ClassLoaderLookupPolicy extendedPolicy =
        lookupPolicy.extend(singletonMap(FOO_PACKAGE, PARENT_FIRST), false);

    assertThat(extendedPolicy.getClassLookupStrategy(FOO_CLASS), instanceOf(ContainerOnlyLookupStrategy.class));
    assertThat(extendedPolicy.getClassLookupStrategy(FOO_CLASS).getClassLoaders(getClass().getClassLoader()),
               hasItem(getClass().getClassLoader()));
  }

  @Test
  public void overwritesOriginalLookupStrategy() {
    ClassLoaderLookupPolicy lookupPolicy =
        new MuleClassLoaderLookupPolicy(singletonMap(FOO_PACKAGE,
                                                     new ContainerOnlyLookupStrategy(getClass().getClassLoader())),
                                        emptySet());

    final ClassLoaderLookupPolicy extendedPolicy =
        lookupPolicy.extend(singletonMap(FOO_PACKAGE, PARENT_FIRST), true);

    assertThat(extendedPolicy.getClassLookupStrategy(FOO_CLASS), instanceOf(ParentFirstLookupStrategy.class));
  }

  @Test
  public void normalizesLookupStrategies() {
    ClassLoaderLookupPolicy lookupPolicy =
        new MuleClassLoaderLookupPolicy(singletonMap(FOO_PACKAGE,
                                                     new ContainerOnlyLookupStrategy(getClass().getClassLoader())),
                                        emptySet());

    final ClassLoaderLookupPolicy extendedPolicy =
        lookupPolicy.extend(singletonMap(FOO_PACKAGE_PREFIX, PARENT_FIRST));

    assertThat(extendedPolicy.getClassLookupStrategy(FOO_CLASS), instanceOf(ContainerOnlyLookupStrategy.class));
    assertThat(extendedPolicy.getClassLookupStrategy(FOO_CLASS).getClassLoaders(getClass().getClassLoader()),
               hasItem(getClass().getClassLoader()));
  }

  @Test
  public void cannotExtendPolicyWithSystemPackage() {
    ClassLoaderLookupPolicy lookupPolicy = new MuleClassLoaderLookupPolicy(emptyMap(), singleton(JAVA_PACKAGE));

    final String overrideClassName = Object.class.getPackage().getName();
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(invalidLookupPolicyOverrideError(overrideClassName, PARENT_FIRST));

    lookupPolicy.extend(singletonMap(overrideClassName, PARENT_FIRST));
  }

  @Test
  public void jaxbNotSystemInJava11() {
    ClassLoaderLookupPolicy lookupPolicy = new MuleClassLoaderLookupPolicy(emptyMap(), singleton("javax.xml"));

    assertThat(lookupPolicy.getPackageLookupStrategy("javax.xml.bind.attachment"), instanceOf(ChildFirstLookupStrategy.class));
  }
}
