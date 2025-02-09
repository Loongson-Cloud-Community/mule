/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.extension.internal.loader.validation;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.mockSubTypes;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.toMetadataType;
import static org.mule.test.module.extension.internal.util.ExtensionsTestUtils.validate;
import org.mule.runtime.extension.api.exception.IllegalModelDefinitionException;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.SubTypesModel;
import org.mule.runtime.extension.internal.loader.validator.SubtypesModelValidator;
import org.mule.runtime.module.extension.internal.loader.java.validation.JavaSubtypesModelValidator;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import com.google.common.collect.ImmutableSet;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class SubtypesModelValidatorTestCase extends AbstractMuleTestCase {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ExtensionModel extensionModel;

  private SubtypesModelValidator validator = new SubtypesModelValidator();


  @Test
  public void validSubtypes() {
    mockSubTypes(extensionModel,
                 new SubTypesModel(toMetadataType(BaseAbstractPojo.class), singleton(toMetadataType(Pojo.class))),
                 new SubTypesModel(toMetadataType(BaseCustomInterface.class), singleton(toMetadataType(Pojo.class))));
    when(extensionModel.getImportedTypes()).thenReturn(emptySet());

    validate(extensionModel, validator);
  }

  @Test(expected = IllegalModelDefinitionException.class)
  public void invalidAbstractSubtypes() {
    Set<SubTypesModel> subTypes = new LinkedHashSet<>();
    subTypes.add(new SubTypesModel(toMetadataType(BaseAbstractPojo.class), singleton(toMetadataType(AbstractPojo.class))));
    subTypes.add(new SubTypesModel(toMetadataType(BaseCustomInterface.class), singleton(toMetadataType(CustomInterface.class))));

    when(extensionModel.getSubTypes()).thenReturn(subTypes);
    when(extensionModel.getImportedTypes()).thenReturn(emptySet());

    validate(extensionModel, new JavaSubtypesModelValidator());
  }

  @Test(expected = IllegalModelDefinitionException.class)
  public void invalidNotSubtypesOfBaseType() {
    mockSubTypes(extensionModel, new SubTypesModel(toMetadataType(BaseCustomInterface.class),
                                                   ImmutableSet.of(toMetadataType(AbstractPojo.class),
                                                                   toMetadataType(CustomInterface.class))));
    when(extensionModel.getImportedTypes()).thenReturn(emptySet());

    validate(extensionModel, validator);
  }

  private static abstract class BaseAbstractPojo {

    protected String basefield;

    public String getBaseField() {
      return basefield;
    }
  }

  private static abstract class AbstractPojo extends BaseAbstractPojo {

    protected String field;

    public String getField() {
      return field;
    }
  }

  public static class Pojo extends AbstractPojo implements CustomInterface {

    protected String childField;

    public String getChildField() {
      return childField;
    }

    public String getBaseField() {
      return "";
    }

    public String getField() {
      return "";
    }
  }


  private interface BaseCustomInterface {

    String getBaseField();
  }


  private interface CustomInterface extends BaseCustomInterface {

    String getField();
  }
}
