/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.config.builders;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.api.config.i18n.CoreMessages.configurationBuilderNoMatching;
import static org.mule.runtime.core.api.util.ClassUtils.getResource;
import static org.mule.runtime.core.api.util.PropertiesUtils.loadProperties;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationBuilder;
import org.mule.runtime.core.api.config.ConfigurationException;
import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.core.api.config.builders.AbstractResourceConfigurationBuilder;
import org.mule.runtime.core.api.util.ClassUtils;
import org.mule.runtime.core.internal.config.ParentMuleContextAwareConfigurationBuilder;
import org.mule.runtime.dsl.api.ConfigResource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configures Mule from a configuration resource or comma separated list of configuration resources by auto-detecting the
 * ConfigurationBuilder to use for each resource. This is resolved by either checking the classpath for config modules e.g.
 * spring-config or by using the file extension or a combination.
 *
 * @deprecated Use SpringXmlConfigurationBuilder instead
 */
@Deprecated
public class AutoConfigurationBuilder extends AbstractResourceConfigurationBuilder
    implements ParentMuleContextAwareConfigurationBuilder {

  private final ArtifactType artifactType;
  private MuleContext parentContext;
  private ArtifactAst parentAst;

  public AutoConfigurationBuilder(String resource, Map<String, String> artifactProperties, ArtifactType artifactType)
      throws ConfigurationException {
    super(resource, artifactProperties);
    this.artifactType = artifactType;
  }

  @Override
  protected void doConfigure(MuleContext muleContext) throws ConfigurationException {
    autoConfigure(muleContext, artifactConfigResources);
  }

  protected void autoConfigure(MuleContext muleContext, ConfigResource[] resources) throws ConfigurationException {
    Map<String, List<ConfigResource>> configsMap = new LinkedHashMap<>();

    for (ConfigResource resource : resources) {
      String configExtension = substringAfterLast(resource.getUrl().getPath(), ".");
      List<ConfigResource> configs = configsMap.get(configExtension);
      if (configs == null) {
        configs = new ArrayList<>();
        configsMap.put(configExtension, configs);
      }
      configs.add(resource);
    }

    try {
      Properties props = loadProperties(getResource("configuration-builders.properties", this.getClass()).openStream());

      for (Map.Entry<String, List<ConfigResource>> e : configsMap.entrySet()) {
        String extension = e.getKey();
        List<ConfigResource> configs = e.getValue();

        String className = (String) props.get(extension);

        if (className == null || !ClassUtils.isClassOnPath(className, this.getClass())) {
          throw new ConfigurationException(configurationBuilderNoMatching(createConfigResourcesString()));
        }

        ConfigurationBuilder cb = (ConfigurationBuilder) ClassUtils
            .instantiateClass(className, new Object[] {
                configs.stream().map(ConfigResource::getResourceName).toArray(String[]::new), getArtifactProperties(),
                artifactType});
        if (parentContext != null && cb instanceof ParentMuleContextAwareConfigurationBuilder) {
          ((ParentMuleContextAwareConfigurationBuilder) cb).setParentContext(parentContext, parentAst);
        } else if (parentContext != null) {
          throw new MuleRuntimeException(createStaticMessage(format("ConfigurationBuilder %s does not support domain context",
                                                                    cb.getClass().getCanonicalName())));
        }
        cb.configure(muleContext);
      }
    } catch (ConfigurationException e) {
      throw e;
    } catch (Exception e) {
      throw new ConfigurationException(e);
    }
  }

  @Override
  public void setParentContext(MuleContext parentContext, ArtifactAst parentAst) {
    this.parentContext = parentContext;
    this.parentAst = parentAst;
  }
}
