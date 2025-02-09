/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.java;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mule.metadata.api.model.MetadataFormat.CSV;
import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import static org.mule.metadata.api.model.MetadataFormat.JSON;
import static org.mule.metadata.api.model.MetadataFormat.XML;
import static org.mule.metadata.api.utils.MetadataTypeUtils.getTypeId;
import static org.mule.runtime.module.extension.api.util.MuleExtensionUtils.loadExtension;

import org.mule.metadata.api.annotation.TypeIdAnnotation;
import org.mule.metadata.api.model.ArrayType;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.api.model.impl.DefaultBinaryType;
import org.mule.metadata.api.model.impl.DefaultObjectFieldType;
import org.mule.metadata.api.model.impl.DefaultObjectType;
import org.mule.metadata.api.model.impl.DefaultStringType;
import org.mule.runtime.api.meta.Typed;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.OutputModel;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.api.meta.model.source.SourceModel;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.testmodels.fruit.Banana;
import org.mule.test.metadata.extension.MetadataExtension;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class ExtensionWithCustomStaticTypesTestCase extends AbstractMuleTestCase {

  private static final String PERSON_TYPE_ID = "http://example.com/example.json";
  private static final String PERSONS_TYPE_ID = "http://example.com/persons.json";

  private ExtensionModel extension = loadExtension(MetadataExtension.class);

  @Test
  public void withInputXmlStaticType() throws Exception {
    OperationModel o = getOperation("xmlInput");
    ParameterModel param = o.getAllParameterModels().get(0);
    assertXmlOrder(param);
  }

  @Test
  public void withOutputXmlStaticType() throws Exception {
    OperationModel o = getOperation("xmlOutput");
    assertXmlOrder(o.getOutput());
  }

  @Test
  public void withOutputXmlStaticTypeSchemaWithImport() throws Exception {
    OperationModel o = getOperation("xmlOutputSchemaWithImport");
    assertXmlOrder(o.getOutput());
  }

  @Test
  public void withListOutputXmlStaticType() throws Exception {
    OperationModel o = getOperation("xmlOutputList");

    MetadataType type = o.getOutput().getType();
    assertThat(type, is(instanceOf(ArrayType.class)));
    MetadataType innerType = ((ArrayType) type).getType();
    assertThat(innerType.getMetadataFormat(), is(XML));
    assertThat(innerType.toString(), is("#root:shiporder"));
  }

  @Test
  public void withListOutputAndEmptySchemaXmlStaticType() throws Exception {
    OperationModel o = getOperation("xmlOutputListWithEmptySchema");

    MetadataType type = o.getOutput().getType();
    assertThat(type, is(instanceOf(ArrayType.class)));
    MetadataType innerType = ((ArrayType) type).getType();
    assertThat(innerType, is(instanceOf(DefaultBinaryType.class)));
  }

  @Test
  public void withOutputAttributesXmlStaticType() throws Exception {
    OperationModel o = getOperation("xmlAttributes");
    assertXmlOrder(o.getOutputAttributes());
  }

  @Test
  public void withInputJsonType() throws Exception {
    OperationModel o = getOperation("jsonInputStream");
    ParameterModel param = o.getAllParameterModels().get(0);
    assertJsonPerson(param);
  }

  @Test
  public void withInputJsonMapType() throws Exception {
    OperationModel o = getOperation("jsonInputMap");
    MetadataType type = o.getAllParameterModels().get(0).getType();
    assertJsonPerson(type);
  }

  @Test
  public void withInputJsonListType() throws Exception {
    OperationModel o = getOperation("jsonInputList");
    MetadataType type = o.getAllParameterModels().get(0).getType();
    assertThat(type, is(instanceOf(ArrayType.class)));
    assertJsonPerson(((ArrayType) type).getType());
  }

  @Test
  public void withOutputJsonType() throws Exception {
    OperationModel o = getOperation("jsonOutput");
    assertJsonPerson(o.getOutput());
  }

  @Test
  public void withArrayOutputJsonType() throws Exception {
    OperationModel o = getOperation("jsonOutputArray");

    MetadataType type = o.getOutput().getType();
    assertThat(type, is(instanceOf(ArrayType.class)));
    assertThat(getTypeId(type).get(), equalTo(PERSONS_TYPE_ID));

    assertJsonPerson(((ArrayType) type).getType());
  }

  @Test
  public void withOutputJsonTypeList() throws Exception {
    OperationModel o = getOperation("jsonOutputList");

    MetadataType type = o.getOutput().getType();
    assertThat(type, is(instanceOf(ArrayType.class)));

    assertJsonPerson(((ArrayType) type).getType());
  }

  @Test
  public void withOutputJsonTypePagingProvider() throws Exception {
    OperationModel o = getOperationFromDefaultConfig("jsonOutputPagingProvider");

    MetadataType type = o.getOutput().getType();
    assertThat(type, is(instanceOf(ArrayType.class)));

    assertJsonPerson(((ArrayType) type).getType());
  }

  @Test
  public void withOutputJsonArrayTypeList() throws Exception {
    OperationModel o = getOperation("jsonArrayOutputList");

    MetadataType type = o.getOutput().getType();
    assertThat(type, is(instanceOf(ArrayType.class)));

    MetadataType innerType = ((ArrayType) type).getType();
    assertThat(innerType, is(instanceOf(ArrayType.class)));
    assertThat(getTypeId(innerType).get(), equalTo(PERSONS_TYPE_ID));

    assertJsonPerson(((ArrayType) innerType).getType());
  }

  @Test
  public void withOutputAttributesJsonType() throws Exception {
    OperationModel o = getOperation("jsonAttributes");
    assertJsonPerson(o.getOutputAttributes());
  }

  @Test
  public void withOutputAttributesJsonArrayType() throws Exception {
    OperationModel o = getOperation("jsonArrayAttributes");

    MetadataType type = o.getOutputAttributes().getType();
    assertThat(type, is(instanceOf(ArrayType.class)));
    assertThat(getTypeId(type).get(), equalTo(PERSONS_TYPE_ID));

    assertJsonPerson(((ArrayType) type).getType());
  }

  @Test
  public void withOutputAttributesJsonTypeList() throws Exception {
    OperationModel o = getOperation("jsonAttributesList");

    MetadataType type = o.getOutputAttributes().getType();
    assertThat(type, is(instanceOf(ArrayType.class)));

    assertJsonPerson(((ArrayType) type).getType());
  }

  @Test
  public void withOutputAttributesJsonArrayTypeList() throws Exception {
    OperationModel o = getOperation("jsonArrayAttributesList");

    MetadataType type = o.getOutputAttributes().getType();
    assertThat(type, is(instanceOf(ArrayType.class)));

    MetadataType innerType = ((ArrayType) type).getType();
    assertThat(innerType, is(instanceOf(ArrayType.class)));
    assertThat(getTypeId(innerType).get(), equalTo(PERSONS_TYPE_ID));

    assertJsonPerson(((ArrayType) innerType).getType());
  }

  @Test
  public void withOutputAttributesJsonTypePagingProviderWithResult() throws Exception {
    OperationModel o = getOperationFromDefaultConfig("jsonAttributesPagingProviderWithResult");
    assertJsonPerson(o.getOutputAttributes().getType());
  }

  @Test
  public void customTypeOutput() throws Exception {
    OperationModel o = getOperation("customTypeOutput");
    OutputModel output = o.getOutput();
    MetadataType type = output.getType();
    assertThat(output.hasDynamicType(), is(false));
    assertThat(type.getMetadataFormat(), is(CSV));
    assertThat(type.toString(), is("csv-object"));
  }

  @Test
  public void customTypeListOutput() throws Exception {
    OperationModel o = getOperation("customTypeListOutput");
    OutputModel output = o.getOutput();
    MetadataType type = output.getType();
    assertThat(output.hasDynamicType(), is(false));
    assertThat(type.getMetadataFormat(), is(CSV));
    assertThat(type.toString(), is("csv-object"));
  }

  @Test
  public void customTypeInput() throws Exception {
    OperationModel o = getOperation("customTypeInput");
    ParameterModel param = o.getAllParameterModels().get(0);
    assertCustomJsonType(param);
  }

  @Test
  public void customTypeAttributes() throws Exception {
    OperationModel o = getOperation("customAttributesOutput");
    assertCustomJsonType(o.getOutputAttributes());
  }

  @Test
  public void customTypeInputAndOutput() throws Exception {
    OperationModel o = getOperation("customInputAndOutput");
    assertCustomJsonType(o.getAllParameterModels().get(0));
    assertCustomJavaType(o.getOutput());
  }

  @Test
  public void customTypeOutputWithStaticAttributes() throws Exception {
    OperationModel o = getOperation("customTypeOutputWithStaticAttributes");
    assertJsonPerson(o.getOutput());
    assertThat(getTypeId(o.getOutputAttributes().getType()).get(), is(Banana.class.getName()));
  }

  @Test
  public void sourceXmlOutput() {
    SourceModel s = getSource("xml-static-metadata");
    assertXmlOrder(s.getOutput());
    assertXmlOrder(s.getOutputAttributes());
  }

  @Test
  public void sourceCustomOutput() {
    SourceModel s = getSource("custom-static-metadata");
    assertCustomJavaType(s.getOutput());
  }

  @Test
  public void sourceOnErrorCustomType() {
    SourceModel s = getSource("custom-static-metadata");
    assertJsonPerson(s.getErrorCallback().get().getAllParameterModels().get(0));
  }

  @Test
  public void sourceOnSuccessCustomType() {
    SourceModel s = getSource("custom-static-metadata");
    assertXmlOrder(s.getSuccessCallback().get().getAllParameterModels().get(0));
  }

  private SourceModel getSource(String name) {
    return extension.getSourceModel(name).orElseThrow(() -> new RuntimeException("Source Not found"));
  }

  private void assertCustomJavaType(Typed t) {
    assertThat(t.hasDynamicType(), is(false));
    assertThat(t.getType().getMetadataFormat(), is(JAVA));
    assertThat(t.getType().getAnnotation(TypeIdAnnotation.class).get().getValue(), is("custom-java"));
    assertThat(t.getType(), is(instanceOf(ObjectType.class)));
  }

  private void assertXmlOrder(Typed typed) {
    MetadataType type = typed.getType();
    assertThat(typed.hasDynamicType(), is(false));
    assertThat(type.getMetadataFormat(), is(XML));
    assertThat(type.toString(), is("#root:shiporder"));

    Object[] typeFields = ((DefaultObjectType) type).getFields().toArray();
    DefaultObjectFieldType fieldOrder = (DefaultObjectFieldType) typeFields[0];
    DefaultObjectType order = (DefaultObjectType) fieldOrder.getValue();
    MatcherAssert.assertThat(order.getFields().toArray().length, is(3));

    DefaultObjectFieldType fieldPerson = (DefaultObjectFieldType) order.getFieldByName("orderperson").get();
    assertThat(fieldPerson.getValue(), instanceOf(DefaultStringType.class));

    DefaultObjectFieldType fieldShipTo = (DefaultObjectFieldType) order.getFieldByName("shipto").get();
    DefaultObjectType shipTo = (DefaultObjectType) fieldShipTo.getValue();
    MatcherAssert.assertThat(shipTo.getFields().toArray().length, is(4));

    DefaultObjectFieldType fieldItem = (DefaultObjectFieldType) order.getFieldByName("item").get();
    DefaultObjectType item = (DefaultObjectType) fieldItem.getValue();
    MatcherAssert.assertThat(item.getFields().toArray().length, is(4));
  }

  private void assertJsonPerson(Typed typed) {
    assertJsonPerson(typed.getType());
    assertThat(typed.hasDynamicType(), is(false));
  }

  private void assertJsonPerson(MetadataType type) {
    assertThat(type.getMetadataFormat(), is(JSON));
    assertThat(type, instanceOf(ObjectType.class));
    assertThat(getTypeId(type).get(), equalTo(PERSON_TYPE_ID));
    assertThat(((ObjectType) type).getFields(), hasSize(3));
  }

  private OperationModel getOperation(String ope) {
    return extension.getOperationModel(ope).orElseThrow(() -> new RuntimeException(ope + " not found"));
  }

  private OperationModel getOperationFromDefaultConfig(String ope) {
    return extension.getConfigurationModel("config").get().getOperationModel(ope)
        .orElseThrow(() -> new RuntimeException(ope + " not found"));
  }

  private void assertCustomJsonType(Typed typed) {
    MetadataType type = typed.getType();
    assertThat(typed.hasDynamicType(), is(false));
    assertThat(type.getMetadataFormat(), is(JSON));
    assertThat(type.toString(), is("json-object"));
  }
}
