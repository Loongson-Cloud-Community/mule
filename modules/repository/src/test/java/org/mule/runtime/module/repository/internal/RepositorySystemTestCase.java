/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.repository.internal;

import static org.mule.runtime.module.repository.internal.RepositoryServiceFactory.MULE_REMOTE_REPOSITORIES_PROPERTY;
import static org.mule.runtime.module.repository.internal.RepositoryServiceFactory.MULE_REPOSITORY_FOLDER_PROPERTY;
import static org.mule.tck.MuleTestUtils.testWithSystemProperty;
import static org.mule.tck.junit4.rule.RequiresConnectivity.checkConnectivity;

import static java.lang.String.format;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.junit.rules.ExpectedException.none;

import org.mule.runtime.module.artifact.api.descriptor.BundleDependency;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import org.mule.runtime.module.repository.api.BundleNotFoundException;
import org.mule.runtime.module.repository.api.RepositoryConnectionException;
import org.mule.runtime.module.repository.api.RepositoryService;
import org.mule.runtime.module.repository.api.RepositoryServiceDisabledException;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class RepositorySystemTestCase extends AbstractMuleTestCase {

  // TODO W-13645342: use a mock repository to allow this test to run offline
  private static final String MAVEN_CENTRAL_REPO_URL = "https://repo.maven.apache.org";

  private static final BundleDescriptor VALID_BUNDLE_DESCRIPTOR =
      new BundleDescriptor.Builder().setGroupId("ant").setArtifactId("ant-antlr").setVersion("1.6").build();
  private static final BundleDependency VALID_BUNDLE =
      new BundleDependency.Builder().setDescriptor(VALID_BUNDLE_DESCRIPTOR).build();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = none();

  @Test
  public void existingResourceFromMaven() throws Exception {
    executeTestWithDefaultRemoteRepo(() -> {
      RepositoryService defaultRepositoryService = new RepositoryServiceFactory().createRepositoryService();
      File bundleFile = defaultRepositoryService.lookupBundle(VALID_BUNDLE);
      assertThat(bundleFile, notNullValue());
      assertThat(bundleFile.exists(), is(true));
      assertThat(bundleFile.getAbsolutePath().startsWith(temporaryFolder.getRoot().getAbsolutePath()), is(true));
    });
  }

  @Test
  public void nonExistentResource() throws Exception {
    executeTestWithDefaultRemoteRepo(() -> {
      RepositoryService defaultRepositoryService = new RepositoryServiceFactory().createRepositoryService();
      BundleDescriptor bundleDescriptor =
          new BundleDescriptor.Builder().setGroupId("no").setArtifactId("existent").setVersion("bundle").build();
      expectedException.expect(BundleNotFoundException.class);
      defaultRepositoryService
          .lookupBundle(new BundleDependency.Builder().setDescriptor(bundleDescriptor).build());
    });
  }

  @Test
  public void invalidExternalRepository() throws Exception {
    executeTestWithCustomRepoRepo("http://doesnotexists/repo", () -> {
      RepositoryService defaultRepositoryService = new RepositoryServiceFactory().createRepositoryService();
      expectedException.expect(RepositoryConnectionException.class);
      defaultRepositoryService.lookupBundle(VALID_BUNDLE);
    });
  }

  @Test
  public void noRepositoryConfigured() throws Exception {
    executeTestWithCustomRepoRepo(null, () -> {
      RepositoryService defaultRepositoryService = new RepositoryServiceFactory().createRepositoryService();
      expectedException.expect(RepositoryServiceDisabledException.class);
      defaultRepositoryService.lookupBundle(VALID_BUNDLE);
    });
  }

  private void executeTestWithDefaultRemoteRepo(TestTask test) throws Exception {
    assumeTrue(format("No connectivity to %s. Ignoring test.", MAVEN_CENTRAL_REPO_URL),
               checkConnectivity(MAVEN_CENTRAL_REPO_URL));

    testWithSystemProperty(MULE_REPOSITORY_FOLDER_PROPERTY, temporaryFolder.getRoot().getAbsolutePath(), () -> {
      testWithSystemProperty(MULE_REMOTE_REPOSITORIES_PROPERTY, format("%s/maven2/", MAVEN_CENTRAL_REPO_URL), () -> {
        test.execute();
      });
    });
  }

  private void executeTestWithCustomRepoRepo(String repositoryUrl, TestTask test) throws Exception {
    testWithSystemProperty(MULE_REPOSITORY_FOLDER_PROPERTY, temporaryFolder.getRoot().getAbsolutePath(), () -> {
      testWithSystemProperty(MULE_REMOTE_REPOSITORIES_PROPERTY, repositoryUrl, () -> {
        test.execute();
      });
    });
  }

  interface TestTask {

    void execute() throws Exception;
  }

}
