/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.junit4.rule;

import static java.io.File.separator;
import static java.lang.String.format;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DELETE_ON_CLOSE;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;

/**
 * Finds available port numbers in a specified range.
 */
public class FreePortFinder {

  private static final Logger logger = getLogger(FreePortFinder.class);

  private final int minPortNumber;
  private final int portRange;
  private final Random random = new Random();
  private final String LOCK_FILE_EXTENSION = ".lock";
  private final Map<Integer, FileLock> locks = new HashMap<>();
  private final Map<Integer, FileChannel> files = new HashMap<>();
  private final String basePath = System.getProperty("mule.freePortFinder.lockPath", ".");

  public FreePortFinder(int minPortNumber, int maxPortNumber) {
    this.minPortNumber = minPortNumber;
    this.portRange = maxPortNumber - minPortNumber;

    logger.debug("Building FreePortFinder {basePath='" + basePath + "', minPortNumber=" + minPortNumber + ", maxPortNumber="
        + maxPortNumber + "}");
  }

  public synchronized Integer find() {
    try {
      forceMkdir(get(basePath).toFile());
    } catch (IOException e) {
      throw new IllegalStateException("Unable to find an available port", e);
    }

    for (int i = 0; i < portRange; i++) {
      int port = minPortNumber + random.nextInt(portRange);
      logger.debug("Trying port " + port + "...");
      String portFile = port + LOCK_FILE_EXTENSION;
      try (FileChannel channel = open(get(basePath + separator + portFile), CREATE, WRITE, DELETE_ON_CLOSE)) {
        FileLock lock = channel.tryLock();
        if (lock == null) {
          // If the lock couldn't be acquired and tryLock didn't throw the exception, we throw it here
          throw new OverlappingFileLockException();
        }

        if (isPortFree(port)) {
          if (logger.isDebugEnabled()) {
            logger.debug("Found free port: " + port);
          }

          locks.put(port, lock);
          files.put(port, channel);
          return port;
        } else {
          lock.release();
          channel.close();
        }
      } catch (OverlappingFileLockException e) {
        // The file is locked,
        if (logger.isDebugEnabled()) {
          logger.debug("Port selected already locked");
        }
      } catch (IOException e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Error when trying to open port lock file, trying another port");
        }
      }

    }

    throw new IllegalStateException("Unable to find an available port");
  }

  /**
   * Indicates that the port is free from the point of view of the caller.
   * <p/>
   * Checks that the port was released, if it was not, then it would be marked as in use, so no other client receives the same
   * port again.
   *
   * @param port the port number to release.
   */
  public synchronized void releasePort(int port) {
    if (isPortFree(port) && locks.containsKey(port) && files.containsKey(port)) {
      FileLock lock = locks.remove(port);
      FileChannel file = files.remove(port);
      try {
        lock.release();
        file.close();

      } catch (IOException e) {
        // Ignore
      }
    } else {
      if (logger.isInfoEnabled()) {
        logger.info(format("Port %d was not correctly released", port));
      }
    }
  }

  /**
   * Check and log is a given port is available
   *
   * @param port the port number to check
   * @return true if the port is available, false otherwise
   */
  public static boolean isPortFree(int port) {
    boolean portIsFree = true;

    ServerSocket server = null;
    try {
      server = new ServerSocket(port);
      server.setReuseAddress(true);
    } catch (IOException e) {
      portIsFree = false;
    } finally {
      if (server != null) {
        try {
          server.close();
        } catch (IOException e) {
          // Ignore
        }
      }
    }

    return portIsFree;
  }
}
