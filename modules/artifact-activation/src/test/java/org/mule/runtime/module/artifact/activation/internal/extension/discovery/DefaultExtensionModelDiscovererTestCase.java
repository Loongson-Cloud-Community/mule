/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.activation.internal.extension.discovery;

import static org.mule.runtime.api.meta.Category.COMMUNITY;
import static org.mule.runtime.core.api.extension.provider.RuntimeExtensionModelProvider.discoverRuntimeExtensionModels;
import static org.mule.runtime.extension.api.ExtensionConstants.ALL_SUPPORTED_JAVA_VERSIONS;
import static org.mule.test.allure.AllureConstants.ExtensionModelDiscoveryFeature.EXTENSION_MODEL_DISCOVERY;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;
import org.mule.runtime.extension.api.loader.ExtensionModelLoader;
import org.mule.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import org.mule.runtime.module.artifact.api.plugin.LoaderDescriber;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.junit.Test;

@Feature(EXTENSION_MODEL_DISCOVERY)
public class DefaultExtensionModelDiscovererTestCase extends AbstractMuleTestCase {

  @Test
  @Issue("MULE-19858")
  @Description("Check that not only 'mule' extension is loaded for xml sdk extension model generation, but all runtime ext models are (for instance: ee)")
  public void allRuntimeExtModelsDiscoveredForExtensionLoading() {
    String pluginName = "myPlugin";
    ArtifactPluginDescriptor descriptor = new ArtifactPluginDescriptor(pluginName);
    LoaderDescriber loaderDescriber = new LoaderDescriber("test");
    descriptor.setExtensionModelDescriptorProperty(loaderDescriber);
    descriptor.setBundleDescriptor(new BundleDescriptor.Builder().setGroupId("myGroup").setArtifactId(
                                                                                                      pluginName)
        .setVersion("1.0").setClassifier("mule-plugin").build());

    AtomicBoolean extensionDeclared = new AtomicBoolean();
    ExtensionModelLoader extModelLoader = new ExtensionModelLoader() {

      @Override
      public String getId() {
        return "test";
      }

      @Override
      protected void declareExtension(ExtensionLoadingContext context) {
        extensionDeclared.set(true);
        assertThat(context.getDslResolvingContext().getExtension("mule").isPresent(), is(true));
        assertThat(context.getDslResolvingContext().getExtension("testRuntime").isPresent(), is(true));

        context.getExtensionDeclarer()
            .named("test")
            .onVersion("0.1")
            .withCategory(COMMUNITY)
            .fromVendor("Mulesoft")
            .supportingJavaVersions(ALL_SUPPORTED_JAVA_VERSIONS);
      }
    };

    ExtensionModelLoaderRepository loaderRepository = mock(ExtensionModelLoaderRepository.class);
    when(loaderRepository.getExtensionModelLoader(loaderDescriber)).thenReturn(of(extModelLoader));

    ArtifactClassLoader artifactClassLoader = mock(ArtifactClassLoader.class);
    when(artifactClassLoader.getClassLoader()).thenReturn(this.getClass().getClassLoader());

    Set<ExtensionModel> extensionModels =
        new DefaultExtensionModelDiscoverer(new RepositoryLookupExtensionModelGenerator(artifactPluginDescriptor -> artifactClassLoader,
                                                                                        loaderRepository))
                                                                                            .discoverPluginsExtensionModels(new DefaultExtensionDiscoveryRequest(singletonList(descriptor),
                                                                                                                                                                 emptySet(),
                                                                                                                                                                 false,
                                                                                                                                                                 false));
    assertThat(extensionDeclared.get(), is(true));
    assertThat(extensionModels.size(), is(1 + discoverRuntimeExtensionModels().size()));
    assertThat((extensionModels.stream()
        .map(ExtensionModel::getArtifactCoordinates)
        .collect(toList())),
               hasItem(of(descriptor.getBundleDescriptor())));
  }

}
