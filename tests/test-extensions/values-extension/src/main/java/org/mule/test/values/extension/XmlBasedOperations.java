/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.values.extension;

import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.sdk.api.annotation.binding.Binding;
import org.mule.sdk.api.annotation.values.FieldValues;
import org.mule.test.values.extension.metadata.XmlTypeResolver;
import org.mule.test.values.extension.resolver.WithRequiredParameterSdkValueProvider;

import java.io.InputStream;

public class XmlBasedOperations {

  public void textAsActingForAttributeValue(@FieldValues(value = WithRequiredParameterSdkValueProvider.class,
      targetSelectors = "nested.tag.@customAttribute", bindings = @Binding(actingParameter = "requiredValue",
          extractionExpression = "xmlBody.nested.tag.\"__text\"")) @Content @TypeResolver(XmlTypeResolver.class) InputStream xmlBody) {}

  public void attributeAsActingForTagContentValue(@FieldValues(value = WithRequiredParameterSdkValueProvider.class,
      targetSelectors = "nested.tag", bindings = @Binding(actingParameter = "requiredValue",
          extractionExpression = "xmlBody.nested.anotherTag.@customAttribute")) @Content @TypeResolver(XmlTypeResolver.class) InputStream xmlBody) {}

  public void tagContentAsActingForAttributeValue(@FieldValues(value = WithRequiredParameterSdkValueProvider.class,
      targetSelectors = "nested.tag.@customAttribute", bindings = @Binding(actingParameter = "requiredValue",
          extractionExpression = "xmlBody.nested.someTag")) @org.mule.sdk.api.annotation.param.Content @TypeResolver(XmlTypeResolver.class) InputStream xmlBody) {}

}
