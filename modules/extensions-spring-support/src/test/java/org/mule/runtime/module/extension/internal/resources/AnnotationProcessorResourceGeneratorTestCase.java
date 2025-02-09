/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.resources;

import static javax.tools.StandardLocation.SOURCE_OUTPUT;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mule.runtime.extension.api.resources.ResourcesGenerator;
import org.mule.runtime.module.extension.internal.resources.test.ResourcesGeneratorContractTestCase;
import org.mule.tck.size.SmallTest;

import java.io.OutputStream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class AnnotationProcessorResourceGeneratorTestCase extends ResourcesGeneratorContractTestCase {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ProcessingEnvironment processingEnvironment;

  @Override
  protected ResourcesGenerator buildGenerator() {
    return new AnnotationProcessorResourceGenerator(resourceFactories, processingEnvironment);
  }

  @Test
  public void write() throws Exception {
    FileObject file = mock(FileObject.class);
    when(processingEnvironment.getFiler().createResource(SOURCE_OUTPUT, EMPTY, RESOURCE_PATH)).thenReturn(file);

    OutputStream out = mock(OutputStream.class, RETURNS_DEEP_STUBS);
    when(file.openOutputStream()).thenReturn(out);

    generator.generateFor(extensionModel);

    verify(out).write(RESOURCE_CONTENT);
    verify(out).flush();
  }
}
