/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines that a test running with {@link ArtifactClassLoaderRunner} would need to get access to the container class loaders in
 * order to load classes from it.
 * <p/>
 * A private static method should be defined and annotated with this annotation in order to be called by the runner so the test
 * later could get access to the container {@link ClassLoader}. Only one method should be annotated with this annotation.
 * <p/>
 *
 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface ContainerClassLoaderAware {
}
