/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.capability.xml;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.TYPE_PROPERTY_NAME;
import static org.mule.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.VERSION;
import static org.mule.runtime.module.extension.internal.resources.BaseExtensionResourcesGeneratorAnnotationProcessor.COMPILATION_MODE;
import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader;
import org.mule.runtime.module.extension.internal.capability.xml.extension.multiple.config.TestExtensionWithDocumentationAndMultipleConfig;
import org.mule.runtime.module.extension.internal.capability.xml.schema.DefaultExtensionSchemaGenerator;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.xpath.XPathFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

@SmallTest
public class ExtensionResourcesGeneratorAnnotationProcessorTestCase extends AbstractMuleTestCase {

  private static final String GROUP_PARAMETER_1 = "Group parameter 1";
  private static final String GROUP_PARAMETER_2 = "Group parameter 2";

  private XPath xpath;
  private DocumentBuilderFactory builderFactory;

  @Before
  public void before() throws Exception {
    XPathFactory xpathFactory = new XPathFactoryImpl();
    xpath = xpathFactory.newXPath();

    builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
  }

  @Test
  public void generateDocumentedSchema() throws Exception {
    ArgumentCaptor<ByteSource> byteSourceCaptor = ArgumentCaptor.forClass(ByteSource.class);
    ByteSource byteSource = mock(ByteSource.class);
    when(byteSource.contentEquals(byteSourceCaptor.capture())).thenReturn(true);

    DefaultJavaExtensionModelLoader loader = new DefaultJavaExtensionModelLoader();
    ExtensionModel model = loader.loadExtensionModel(TestExtensionWithDocumentationAndMultipleConfig.class.getClassLoader(),
                                                     DslResolvingContext.getDefault(emptySet()),
                                                     ImmutableMap.<String, Object>builder()
                                                         .put(TYPE_PROPERTY_NAME,
                                                              TestExtensionWithDocumentationAndMultipleConfig.class.getName())
                                                         .put(VERSION, "4.0.0")
                                                         // TODO MULE-14517: This workaround should be replaced for a better and
                                                         // more complete mechanism
                                                         .put(COMPILATION_MODE, true)
                                                         .build());
    String generatedSchema = new DefaultExtensionSchemaGenerator().generate(model, DslResolvingContext.getDefault(emptySet()));
    assertThat(generatedSchema, is(notNullValue()));

    assertXpath(generatedSchema, "//xs:attribute[@name='configParameter']/xs:annotation/xs:documentation", "Config parameter");
    assertXpath(generatedSchema, "//xs:attribute[@name='configParameterWithComplexJavadoc']/xs:annotation/xs:documentation",
                "Config Parameter with an Optional value");
    assertXpath(generatedSchema, "//xs:attribute[@name='value1']/xs:annotation/xs:documentation", GROUP_PARAMETER_1);
    assertXpath(generatedSchema, "//xs:attribute[@name='value2']/xs:annotation/xs:documentation", GROUP_PARAMETER_2);


    assertXpath(generatedSchema, "//xs:element[@name='operation']/xs:annotation/xs:documentation", "Test Operation");
    assertXpath(generatedSchema,
                "//xs:complexType[@name='OperationType']/xs:complexContent/xs:extension/xs:attribute[@name='value']/xs:annotation/xs:documentation",
                "test value");
    assertXpath(generatedSchema,
                "//xs:complexType[@name='OperationType']/xs:complexContent/xs:extension/xs:attribute[@name='value1']/xs:annotation/xs:documentation",
                GROUP_PARAMETER_1);
    assertXpath(generatedSchema,
                "//xs:complexType[@name='OperationType']/xs:complexContent/xs:extension/xs:attribute[@name='value2']/xs:annotation/xs:documentation",
                GROUP_PARAMETER_2);

    assertXpath(generatedSchema, "//xs:element[@name='ignore-operation-should-be-ignored']/xs:annotation/xs:documentation", "");
    assertXpath(generatedSchema, "//xs:element[@name='private-operation-should-be-ignored']/xs:annotation/xs:documentation", "");

    assertXpath(generatedSchema,
                "//xs:element[@name='operation-with-blank-parameter-description']/xs:annotation/xs:documentation",
                "Test Operation with blank parameter description");
    assertXpath(generatedSchema,
                "//xs:complexType[@name='OperationWithBlankParameterDescriptionType']/xs:complexContent/xs:extension/xs:attribute[@name='value']/xs:annotation/xs:documentation",
                "");

    assertXpath(generatedSchema, "//xs:element[@name='operation-with-javadoc-link-references']/xs:annotation/xs:documentation",
                "Operation that returns a String value");
    assertXpath(generatedSchema,
                "//xs:complexType[@name='OperationWithJavadocLinkReferencesType']/xs:complexContent/xs:extension/xs:attribute[@name='value']/xs:annotation/xs:documentation",
                "this is the String to be returned");
  }

  private void assertXpath(String input, String expression, String expected) throws Exception {
    assertThat(xpath(input, expression), is(expected));
  }

  private String xpath(String input, String expression) throws Exception {
    Node node = builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(input)));
    return (String) xpath.evaluate(expression, node, XPathConstants.STRING);
  }
}
