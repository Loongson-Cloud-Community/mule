/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el.mvel.configuration;

import org.mule.runtime.core.internal.el.mvel.MVELExpressionLanguage;
import org.mule.runtime.dsl.api.component.AbstractComponentFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * POJO for parsing the expression-language element.
 *
 * @since 4.0
 */
public class MVELExpressionLanguageObjectFactory extends AbstractComponentFactory<MVELExpressionLanguage> {

  @Inject
  private MVELExpressionLanguage mvel;

  private boolean autoResolveVariables;
  private MVELGlobalFunctionsConfig globalFunctions;
  private List<AliasEntry> aliases;
  private List<ImportEntry> imports;

  public void setAutoResolveVariables(boolean autoResolveVariables) {
    this.autoResolveVariables = autoResolveVariables;
  }

  public void setGlobalFunctions(MVELGlobalFunctionsConfig globalFunctions) {
    this.globalFunctions = globalFunctions;
  }

  public void setAliases(List<AliasEntry> aliases) {
    this.aliases = aliases;
  }

  public void setImports(List<ImportEntry> imports) {
    this.imports = imports;
  }

  @Override
  public MVELExpressionLanguage doGetObject() throws Exception {
    mvel.setAutoResolveVariables(autoResolveVariables);

    if (globalFunctions != null) {
      mvel.setGlobalFunctionsFile(globalFunctions.getFile());
      mvel.setGlobalFunctionsString(globalFunctions.getInlineScript());
    }

    if (aliases != null) {
      Map<String, String> aliasesMap = new HashMap<>();
      aliases.forEach(x -> aliasesMap.put(x.getKey(), x.getValue()));
      mvel.setAliases(aliasesMap);
    }

    if (imports != null) {
      Map<String, Class<?>> importsMap = new HashMap<>();
      imports.forEach(x -> importsMap.put(x.getKey(), x.getValue()));
      mvel.setImports(importsMap);
    }

    // Refresh with the updated bindings
    mvel.initialise();
    return mvel;
  }
}
