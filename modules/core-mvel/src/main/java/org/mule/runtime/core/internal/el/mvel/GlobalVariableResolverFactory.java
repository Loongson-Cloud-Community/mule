/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el.mvel;

import org.mule.mvel2.ParserConfiguration;
import org.mule.mvel2.ast.Function;
import org.mule.mvel2.ast.FunctionInstance;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.internal.context.MuleContextWithRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

public class GlobalVariableResolverFactory extends MVELExpressionLanguageContext {

  private static final long serialVersionUID = -6819292692339684915L;

  // Minor optimization to avoid HashMap contains if it can be avoided.
  private boolean hasTarget = false;

  public GlobalVariableResolverFactory(Map<String, String> aliases, Map<String, Function> functions,
                                       ParserConfiguration parserConfiguration, MuleContext muleContext) {
    super(parserConfiguration, muleContext);
    Collection<ExpressionLanguageExtension> extensions = ((MuleContextWithRegistry) muleContext).getRegistry()
        .lookupObjectsForLifecycle(ExpressionLanguageExtension.class);

    hasTarget = !(aliases.isEmpty() && functions.isEmpty() && extensions.isEmpty());

    for (ExpressionLanguageExtension extension : extensions) {
      extension.configureContext(this);
    }
    for (Entry<String, String> alias : aliases.entrySet()) {
      addAlias(alias.getKey(), alias.getValue());
    }
    for (Entry<String, Function> function : functions.entrySet()) {
      addFinalVariable(function.getKey(), new FunctionInstance(function.getValue()));
    }
  }

  @Override
  public boolean isTarget(String name) {
    return hasTarget && super.isTarget(name);
  }

}
