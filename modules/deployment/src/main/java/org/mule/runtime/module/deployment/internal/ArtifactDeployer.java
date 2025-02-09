/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.internal;

import org.mule.runtime.deployment.model.api.DeployableArtifact;

/**
 * Deploys and Undeploys artifacts in the container.
 *
 * @param <T> artifact type
 */
public interface ArtifactDeployer<T extends DeployableArtifact> {

  /**
   * Deploys an artifact.
   * <p>
   * The deployer executes the artifact installation phases until the artifact is deployed. After this method call the Artifact
   * will be installed in the container and its start dispatched asynchronously.
   *
   * @param artifact      artifact to be deployed
   * @param startArtifact whether the artifact should be started after initialisation
   */
  void deploy(final T artifact, boolean startArtifact);

  /**
   * Deploys an artifact.
   * <p>
   * The deployer executes the artifact installation phases until the artifact is deployed. After this method call the Artifact
   * will be installed in the container and its start dispatched asynchronously.
   *
   * @param artifact artifact to be deployed
   */
  default void deploy(final T artifact) {
    deploy(artifact, true);
  }

  /**
   * Undeploys an artifact.
   * <p>
   * The deployer executes the artifact unsinstallation phases until the artifact is undeployed. After this method call the
   * Artifact will not longer be running inside the container.
   *
   * @param artifact artifact to be undeployed
   */
  void undeploy(final T artifact);

  /**
   * Cancels the persistence of a stop of an artifact.
   * <p>
   * A stop of a certain artifact must only be persisted when it was stopped by the external users. In case of undeployment, it
   * should not be persisted.
   *
   * @param artifact artifact to be undeployed
   */
  void doNotPersistArtifactStop(T artifact);

}
