/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.api.util.xmlsecurity;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;

public class XMLSecureFactoriesTestCase {

  @Test
  public void createsTheCorrectInstances() {
    XMLSecureFactories xmlSecureFactories = XMLSecureFactories.createDefault();

    // As there are casts inside, creation would fail if the appropriate type is not returned
    assertThat(xmlSecureFactories.getDocumentBuilderFactory(), notNullValue());
    assertThat(xmlSecureFactories.getSAXParserFactory(), notNullValue());
    assertThat(xmlSecureFactories.getXMLInputFactory(), notNullValue());
    assertThat(xmlSecureFactories.getTransformerFactory(), notNullValue());
  }

  @Test
  public void cachesXmlFactory() {
    DocumentBuilderFactory documentBuilderFactoryOne = XMLSecureFactories.createDefault().getDocumentBuilderFactory();
    DocumentBuilderFactory documentBuilderFactoryTwo = XMLSecureFactories.createDefault().getDocumentBuilderFactory();

    assertThat(documentBuilderFactoryOne, sameInstance(documentBuilderFactoryTwo));
  }

  @Test
  public void handlesDifferentConfigurations() {
    DocumentBuilderFactory insecureFactoryOne = XMLSecureFactories.createWithConfig(true, true).getDocumentBuilderFactory();
    DocumentBuilderFactory secureFactoryOne = XMLSecureFactories.createWithConfig(true, false).getDocumentBuilderFactory();
    DocumentBuilderFactory insecureFactoryTwo = XMLSecureFactories.createWithConfig(true, true).getDocumentBuilderFactory();
    DocumentBuilderFactory secureFactoryTwo = XMLSecureFactories.createWithConfig(true, false).getDocumentBuilderFactory();

    assertThat(insecureFactoryOne, sameInstance(insecureFactoryTwo));
    assertThat(secureFactoryOne, sameInstance(secureFactoryTwo));
    assertThat(insecureFactoryOne, not(sameInstance(secureFactoryOne)));
  }
}
