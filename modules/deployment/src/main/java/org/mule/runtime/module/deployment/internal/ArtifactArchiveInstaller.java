/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.internal;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.toFile;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.api.util.FileUtils.deleteTree;
import static org.mule.runtime.module.deployment.internal.DefaultArchiveDeployer.JAR_FILE_SUFFIX;
import static org.mule.runtime.module.deployment.internal.MuleDeploymentService.ARTIFACT_ANCHOR_SUFFIX;

import org.mule.runtime.core.api.util.FileUtils;
import org.mule.runtime.deployment.model.api.DeploymentException;
import org.mule.runtime.deployment.model.api.DeploymentInitException;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Installer for mule artifacts inside the mule container directories.
 */
public class ArtifactArchiveInstaller {

  protected static final String ANCHOR_FILE_BLURB =
      "Delete this file while Mule is running to remove the artifact in a clean way.";

  private static final Logger logger = LoggerFactory.getLogger(ArtifactArchiveInstaller.class);

  private final File artifactParentDir;

  public ArtifactArchiveInstaller(File artifactParentDir) {
    this.artifactParentDir = artifactParentDir;
  }

  /**
   * Installs an artifact in the mule container.
   * <p>
   * Creates the artifact directory and the anchor file related.
   *
   * @param artifactUri URI of the artifact to install. It must be present in the artifact directory as a zip file.
   * @return the location of the installed artifact.
   * @throws IOException in case there was an error reading from the artifact or writing to the artifact folder.
   */
  public File installArtifact(final URI artifactUri) throws IOException {
    if (!artifactUri.toString().toLowerCase().endsWith(JAR_FILE_SUFFIX)) {
      throw new IllegalArgumentException("Invalid Mule artifact archive: '" + artifactUri + "'");
    }

    final File artifactFile = new File(artifactUri);
    final String baseName = getBaseName(artifactFile.getName());
    if (baseName.contains(" ")) {
      throw new DeploymentInitException(createStaticMessage("Mule artifact name may not contain spaces: '" + baseName + "'"));
    }

    File artifactDir = null;
    boolean errorEncountered = false;
    String artifactName;
    try {
      final String fullPath = artifactFile.getAbsolutePath();

      if (logger.isInfoEnabled()) {
        logger.info("Exploding a Mule artifact archive: '" + fullPath + "'");
      }

      artifactName = getBaseName(fullPath);
      artifactDir = new File(artifactParentDir, artifactName);

      // Removes previous deployed artifact
      if (artifactDir.exists() && !deleteTree(artifactDir)) {
        throw new IOException("Cannot delete existing folder '" + artifactDir + "'");
      }

      // normalize the full path + protocol to make unzip happy
      final File source = artifactFile;

      FileUtils.unzip(source, artifactDir);
      if ("file".equals(artifactUri.getScheme())
          && toFile(artifactUri.toURL()).getAbsolutePath().startsWith(artifactParentDir.getAbsolutePath())) {
        deleteQuietly(source);
      }
    } catch (IOException e) {
      errorEncountered = true;
      throw e;
    } catch (Throwable t) {
      errorEncountered = true;
      final String msg = "Failed to install artifact from URI: '" + artifactUri + "'";
      throw new DeploymentInitException(createStaticMessage(msg), t);
    } finally {
      // delete an artifact dir, as it's broken
      if (errorEncountered && artifactDir != null && artifactDir.exists()) {
        deleteTree(artifactDir);
      }
    }
    return artifactDir;
  }

  /**
   * Uninstalls an artifact from the Mule container installation.
   *
   * It will remove the artifact folder and the anchor file related
   *
   * @param artifactName name of the artifact to be uninstalled.
   */
  void uninstallArtifact(final String artifactName) {
    try {
      final File artifactDir = new File(artifactParentDir, artifactName);
      deleteDirectory(artifactDir);
      // remove a marker, harmless, but a tidy artifact dir is always better :)
      File marker = getArtifactAnchorFile(artifactName);
      marker.delete();
      Introspector.flushCaches();
    } catch (Throwable t) {
      if (t instanceof DeploymentException) {
        throw ((DeploymentException) t);
      }

      final String msg = format("Failed to undeployArtifact artifact [%s]", artifactName);
      throw new DeploymentException(createStaticMessage(msg), t);
    }
  }

  private File getArtifactAnchorFile(String artifactName) {
    return new File(artifactParentDir, format("%s%s", artifactName, ARTIFACT_ANCHOR_SUFFIX));
  }

  void createAnchorFile(String artifactName) throws IOException {
    // save artifact's state in the marker file
    File marker = getArtifactAnchorFile(artifactName);
    writeStringToFile(marker, ANCHOR_FILE_BLURB);
  }

}
