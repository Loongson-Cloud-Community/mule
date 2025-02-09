/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.deployment.model.internal.domain;

import static org.mule.runtime.container.api.MuleFoldersUtil.getDomainFolder;
import static org.mule.runtime.deployment.model.api.domain.DomainDescriptor.DEFAULT_DOMAIN_NAME;

import static java.util.Collections.emptyList;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.mule.runtime.core.api.config.MuleProperties;
import org.mule.runtime.core.api.util.FileUtils;
import org.mule.runtime.module.artifact.activation.internal.classloader.MuleSharedDomainClassLoader;
import org.mule.runtime.module.artifact.activation.internal.nativelib.ArtifactCopyNativeLibraryFinder;
import org.mule.runtime.module.artifact.activation.internal.nativelib.NativeLibraryFinder;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.junit4.rule.SystemPropertyTemporaryFolder;
import org.mule.tck.size.SmallTest;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SmallTest
public class MuleSharedDomainClassLoaderTestCase extends AbstractMuleTestCase {

  public static final String RESOURCE_FILE_NAME = "file.properties";

  private static final String CUSTOM_DOMAIN_NAME = "custom-domain";
  private static final String NATIVE_LIBRARY = "native-library";

  @Rule
  public SystemPropertyTemporaryFolder temporaryFolder =
      new SystemPropertyTemporaryFolder(MuleProperties.MULE_HOME_DIRECTORY_PROPERTY);
  private final ClassLoaderLookupPolicy lookupPolicy = mock(ClassLoaderLookupPolicy.class);

  @Rule
  public TemporaryFolder nativeLibraryFolder = new TemporaryFolder();

  @Test
  public void findResourcesInProvidedUrls() throws Exception {
    createDomainFolder(DEFAULT_DOMAIN_NAME);
    final File resourceFile = createDomainResource(DEFAULT_DOMAIN_NAME, RESOURCE_FILE_NAME);
    final List<URL> urls = Collections.singletonList(resourceFile.toURI().toURL());

    MuleSharedDomainClassLoader classLoader = new MuleSharedDomainClassLoader(new ArtifactDescriptor(DEFAULT_DOMAIN_NAME),
                                                                              getClass().getClassLoader(), lookupPolicy, urls);

    assertThat(classLoader.findResource(RESOURCE_FILE_NAME), notNullValue());
  }

  private File createDomainResource(String domainName, String resourceFile) throws Exception {
    final File file = new File(getDomainFolder(domainName), resourceFile);
    assertThat(FileUtils.createFile(file.getAbsolutePath()).exists(), is(true));

    return file;
  }

  private void createDomainFolder(String domainName) {
    assertThat(getDomainFolder(domainName).mkdirs(), is(true));
  }

  @Test
  public void defaultDomainNotUseNativeLibrary() {
    MuleSharedDomainClassLoader classLoader = new MuleSharedDomainClassLoader(new ArtifactDescriptor(DEFAULT_DOMAIN_NAME),
                                                                              getClass().getClassLoader(), lookupPolicy,
                                                                              emptyList());

    assertThat(classLoader.findLibrary(NATIVE_LIBRARY), is(nullValue()));
  }

  @Test
  public void customDomainLoadNativeLibrary() throws Exception {
    // Create native library
    File nativeLibrary = createNativeLibraryFile(NATIVE_LIBRARY);
    NativeLibraryFinder nativeLibraryFinder =
        new ArtifactCopyNativeLibraryFinder(nativeLibraryFolder.getRoot(), new URL[] {nativeLibrary.toURL()});

    MuleSharedDomainClassLoader classLoader = new MuleSharedDomainClassLoader(new ArtifactDescriptor(CUSTOM_DOMAIN_NAME),
                                                                              getClass().getClassLoader(), lookupPolicy,
                                                                              emptyList(), nativeLibraryFinder);

    assertThat(classLoader.findLibrary(NATIVE_LIBRARY), is(notNullValue()));
  }

  private File createNativeLibraryFile(String libName) throws Exception {
    return createNativeLibraryFile(nativeLibraryFolder.getRoot(), System.mapLibraryName(libName));
  }

  private File createNativeLibraryFile(File folder, String libFileName) throws Exception {
    File libraryFile = new File(folder, libFileName);
    assertThat(FileUtils.createFile(libraryFile.getAbsolutePath()).exists(), is(true));
    return libraryFile;
  }
}
