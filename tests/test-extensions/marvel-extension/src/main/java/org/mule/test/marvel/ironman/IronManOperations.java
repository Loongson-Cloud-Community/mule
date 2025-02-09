/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.test.marvel.ironman;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.lang.ThreadLocal.withInitial;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.api.meta.model.display.PathModel.Location.EMBEDDED;
import static org.mule.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;
import static org.mule.runtime.core.api.util.UUID.getUUID;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import static org.mule.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.execution.Execution;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.mule.sdk.api.annotation.param.display.ClassValue;
import org.mule.test.marvel.model.Missile;
import org.mule.test.marvel.model.Villain;

import java.util.concurrent.ScheduledExecutorService;

public class IronManOperations implements Initialisable, Disposable {

  public static final int MISSILE_TRAVEL_TIME = 200;
  public static final String FLIGHT_PLAN = "Go Straight";

  private static final ThreadLocal<String> taskTokenInThread = withInitial(IronManOperations::generateTaskToken);

  private ScheduledExecutorService executorService;

  @Override
  public void initialise() throws InitialisationException {
    executorService = newSingleThreadScheduledExecutor();
  }

  @Override
  public void dispose() {
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }

  @MediaType(TEXT_PLAIN)
  public void fireMissile(@Config IronMan ironMan,
                          @Connection Missile missile,
                          Villain at,
                          org.mule.sdk.api.runtime.process.CompletionCallback<String, Void> callback) {
    final Runnable launch = () -> {
      try {
        ironMan.track(missile);
        callback.success(org.mule.sdk.api.runtime.operation.Result.<String, Void>builder()
            .output(missile.fireAt(at)).build());
      } catch (Exception e) {
        callback.error(e);
      }
    };

    // it takes the missile some time to reach target. Don't block while you kill
    executorService.schedule(launch, MISSILE_TRAVEL_TIME, MILLISECONDS);
  }

  @MediaType(TEXT_PLAIN)
  public void fireMissileMishap(@Config IronMan ironMan,
                                @org.mule.sdk.api.annotation.param.Connection Missile missile,
                                Villain at,
                                CompletionCallback<String, Void> callback) {
    // A non blocking operation throwing an exception instead of calling callback#error.
    throw new IllegalStateException("Ultron jammed the missile system!");
  }

  @MediaType(TEXT_PLAIN)
  public void fireMissileEpicShot(@Config IronMan ironMan,
                                  @Connection Missile missile,
                                  Villain at,
                                  org.mule.sdk.api.runtime.process.CompletionCallback<String, Void> callback) {
    // the last missile being fired in the movie has the hero looking at the missile going forward to its target. Doesn't look
    // epic if he's doing something else.
    try {
      ironMan.track(missile);
      callback.success(org.mule.sdk.api.runtime.operation.Result.<String, Void>builder()
          .output(missile.fireAt(at)).build());
    } catch (Exception e) {
      callback.error(e);
    }
  }

  @MediaType(TEXT_PLAIN)
  public String findInstructions(@Optional @Path(acceptedFileExtensions = {"xml"}, location = EMBEDDED) String instructionsFile,
                                 @Optional @ClassValue(extendsOrImplements = {"com.starkindustries.Reader"}) String readerClass) {
    return instructionsFile;
  }

  @Execution(CPU_INTENSIVE)
  public void computeFlightPlan(@Config IronMan ironMan, CompletionCallback<Void, Void> callback) {
    final Runnable launch = () -> {
      callback.success(Result.<Void, Void>builder().build());
      ironMan.setFlightPlan(FLIGHT_PLAN);
    };

    // building a flight plan requires a lot of computation. Don't block while you kill
    executorService.schedule(launch, MISSILE_TRAVEL_TIME, MILLISECONDS);
  }

  /**
   * Verifies that a thread switch on the nested {@code interceptedChain} did occur.
   *
   * @param interceptedChain the chain in which a thread switch is expected.
   * @param callback         the {@link CompletionCallback}.
   */
  @MediaType(value = ANY, strict = false)
  public void assertResponseDifferentTask(Chain interceptedChain, CompletionCallback<Object, Object> callback) {
    String requestTaskToken = taskTokenInThread.get();

    interceptedChain.process(r -> {
      String responseTaskToken = taskTokenInThread.get();

      if (requestTaskToken.equals(responseTaskToken)) {
        final IllegalStateException e = new IllegalStateException(format("Response task (%s) was same as request task (%s)",
                                                                         responseTaskToken, requestTaskToken));
        callback.error(e);
      } else {
        callback.success(r);
      }
    }, (t, r) -> callback.error(t));
  }

  private static String generateTaskToken() {
    return currentThread().getName() + " - " + getUUID();
  }
}
