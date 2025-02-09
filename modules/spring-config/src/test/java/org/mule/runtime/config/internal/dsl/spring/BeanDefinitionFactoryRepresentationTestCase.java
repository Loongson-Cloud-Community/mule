/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.config.internal.dsl.spring;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.component.Component.Annotations.NAME_ANNOTATION_KEY;
import static org.mule.runtime.config.internal.dsl.spring.BeanDefinitionFactory.resolveProcessorRepresentation;
import static org.mule.runtime.dsl.api.component.config.DefaultComponentLocation.from;

import org.mule.runtime.ast.internal.DefaultComponentMetadataAst;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Test;

public class BeanDefinitionFactoryRepresentationTestCase extends AbstractMuleTestCase {

  @Test
  public void withFailingProcessorNoPathResolver() {
    assertThat(resolveProcessorRepresentation("BeanDefinitionFactoryRepresentationTestCase", from("Mock@1"),
                                              DefaultComponentMetadataAst.builder()
                                                  .setFileName("unknown")
                                                  .build()),
               is("Mock@1 @ BeanDefinitionFactoryRepresentationTestCase:unknown:-1"));
  }

  @Test
  public void withFailingProcessorPathResolver() {
    assertThat(resolveProcessorRepresentation("BeanDefinitionFactoryRepresentationTestCase",
                                              from("flow/processor"),
                                              DefaultComponentMetadataAst.builder()
                                                  .setFileName("muleApp.xml")
                                                  .setStartLine(10)
                                                  .build()),
               is("flow/processor @ BeanDefinitionFactoryRepresentationTestCase:muleApp.xml:10"));
  }

  @Test
  public void withFailingProcessorNotPathResolver() {
    assertThat(resolveProcessorRepresentation("BeanDefinitionFactoryRepresentationTestCase", from("Mock@1"),
                                              DefaultComponentMetadataAst.builder()
                                                  .setFileName("unknown")
                                                  .build()),
               is("Mock@1 @ BeanDefinitionFactoryRepresentationTestCase:unknown:-1"));
  }

  @Test
  public void withAnnotatedFailingProcessorNoPathResolver() {
    assertThat(resolveProcessorRepresentation("BeanDefinitionFactoryRepresentationTestCase", from("Mock@1"),
                                              DefaultComponentMetadataAst.builder()
                                                  .setFileName("muleApp.xml")
                                                  .setStartLine(10)
                                                  .putDocAttribute(NAME_ANNOTATION_KEY.getLocalPart(), "Mock Component")
                                                  .build()),
               is("Mock@1 @ BeanDefinitionFactoryRepresentationTestCase:muleApp.xml:10 (Mock Component)"));
  }

  @Test
  public void withAnnotatedFailingProcessorPathResolver() {
    assertThat(resolveProcessorRepresentation("BeanDefinitionFactoryRepresentationTestCase",
                                              from("flow/processor"),
                                              DefaultComponentMetadataAst.builder()
                                                  .setFileName("muleApp.xml")
                                                  .setStartLine(10)
                                                  .putDocAttribute(NAME_ANNOTATION_KEY.getLocalPart(), "Mock Component")
                                                  .build()),
               is("flow/processor @ BeanDefinitionFactoryRepresentationTestCase:muleApp.xml:10 (Mock Component)"));
  }

  @Test
  public void withAnnotatedFailingProcessorNotPathResolver() {
    assertThat(resolveProcessorRepresentation("BeanDefinitionFactoryRepresentationTestCase", from("Mock@1"),
                                              DefaultComponentMetadataAst.builder()
                                                  .setFileName("muleApp.xml")
                                                  .setStartLine(10)
                                                  .putDocAttribute(NAME_ANNOTATION_KEY.getLocalPart(), "Mock Component")
                                                  .build()),
               is("Mock@1 @ BeanDefinitionFactoryRepresentationTestCase:muleApp.xml:10 (Mock Component)"));
  }

}
