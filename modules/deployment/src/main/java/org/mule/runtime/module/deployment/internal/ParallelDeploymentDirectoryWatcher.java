/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.internal;

import static org.mule.runtime.api.scheduler.SchedulerConfig.config;

import static java.lang.Thread.currentThread;
import static java.util.Optional.empty;

import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.deployment.model.api.application.Application;
import org.mule.runtime.deployment.model.api.domain.Domain;
import org.mule.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import org.mule.runtime.module.artifact.api.descriptor.DomainDescriptor;
import org.mule.runtime.module.deployment.internal.util.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Provides parallel deployment of Mule applications.
 *
 * @since 3.8.2
 */
public class ParallelDeploymentDirectoryWatcher extends DeploymentDirectoryWatcher {

  public static final int MAX_APPS_IN_PARALLEL_DEPLOYMENT = 20;

  private Scheduler threadPoolExecutor;

  public ParallelDeploymentDirectoryWatcher(DomainBundleArchiveDeployer domainBundleDeployer,
                                            ArchiveDeployer<DomainDescriptor, Domain> domainArchiveDeployer,
                                            ArchiveDeployer<ApplicationDescriptor, Application> applicationArchiveDeployer,
                                            ObservableList<Domain> domains, ObservableList<Application> applications,
                                            Supplier<SchedulerService> schedulerServiceSupplier, ReentrantLock deploymentLock) {
    super(domainBundleDeployer, domainArchiveDeployer, applicationArchiveDeployer, domains, applications,
          schedulerServiceSupplier, deploymentLock);
  }

  @Override
  protected void deployPackedApps(String[] zips) {
    if (zips.length == 0) {
      return;
    }

    List<Callable<Object>> tasks = new ArrayList<>(zips.length);
    for (final String zip : zips) {
      tasks.add(() -> {
        try {
          applicationArchiveDeployer.deployPackagedArtifact(zip, empty());
        } catch (Exception e) {
          // Ignore and continue
        }
        return null;
      });
    }

    waitForTasksToFinish(tasks);
  }


  @Override
  protected void deployExplodedApps(String[] apps) {
    List<Callable<Object>> tasks = new ArrayList<>(apps.length);

    for (final String addedApp : apps) {
      if (applicationArchiveDeployer.isUpdatedZombieArtifact(addedApp)) {
        tasks.add(() -> {
          try {
            applicationArchiveDeployer.deployExplodedArtifact(addedApp, empty());
          } catch (Exception e) {
            // Ignore and continue
          }
          return null;
        });
      }
    }

    if (!tasks.isEmpty()) {
      waitForTasksToFinish(tasks);
    }
  }

  private void waitForTasksToFinish(List<Callable<Object>> tasks) {
    this.threadPoolExecutor =
        schedulerServiceSupplier.get()
            .ioScheduler(config().withName("parallelDeployment").withMaxConcurrentTasks(MAX_APPS_IN_PARALLEL_DEPLOYMENT));
    try {
      final List<Future<Object>> futures = threadPoolExecutor.invokeAll(tasks);

      for (Future<Object> future : futures) {
        try {
          future.get();
        } catch (ExecutionException e) {
          // Ignore and continue with the next one
        }
      }

    } catch (InterruptedException e) {
      currentThread().interrupt();
    } finally {
      threadPoolExecutor.stop();
    }
  }
}
