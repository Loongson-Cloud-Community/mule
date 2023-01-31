/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.reboot.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constructs a default set of JAR Urls located under Mule home folder.
 */
// TODO this duplicates DefaultMuleClassPathConfig in the boot module. See if this class can be moved to mule-core
public class DefaultMuleClassPathConfig {

  protected static final String MULE_DIR = "/lib/mule";
  protected static final String USER_DIR = "/lib/user";
  protected static final String OPT_DIR = "/lib/opt";

  protected List<File> muleJars = new ArrayList<>();
  protected List<File> optJars = new ArrayList<>();
  protected List<File> userJars = new ArrayList<>();

  protected List<URL> urls = new ArrayList<>();

  public DefaultMuleClassPathConfig(File muleHome, File muleBase) {
    init(muleHome, muleBase);
  }

  protected void init(File muleHome, File muleBase) {
    /*
     * Pick up any local jars, if there are any. Doing this here insures that any local class that override the global classes
     * will in fact do so.
     */
    addMuleBaseUserLibs(muleHome, muleBase);

    userJars.addAll(addLibraryDirectory(muleHome, USER_DIR));
    muleJars.addAll(addLibraryDirectory(muleHome, MULE_DIR));
    optJars.addAll(addLibraryDirectory(muleHome, OPT_DIR));
  }

  protected void addMuleBaseUserLibs(File muleHome, File muleBase) {
    try {
      if (!muleHome.getCanonicalFile().equals(muleBase.getCanonicalFile())) {
        File userOverrideDir = new File(muleBase, USER_DIR);
        addFile(userOverrideDir);
        addFiles(listJars(userOverrideDir));
      }
    } catch (IOException ioe) {
      System.out.println("Unable to check to see if there are local jars to load: " + ioe.toString());
    }
  }

  protected List<File> addLibraryDirectory(File muleHome, String libDirectory) {
    File directory = new File(muleHome, libDirectory);
    addFile(directory);
    List<File> listJars = listJars(directory);
    addFiles(listJars);
    return listJars;
  }

  public List<URL> getURLs() {
    return new ArrayList<>(this.urls);
  }

  public void addURLs(List<URL> moreUrls) {
    if (moreUrls != null && !moreUrls.isEmpty()) {
      this.urls.addAll(moreUrls);
    }
  }

  /**
   * Add a URL to Mule's classpath.
   *
   * @param url folder (should end with a slash) or jar path
   */
  public void addURL(URL url) {
    this.urls.add(url);
  }

  public void addFiles(List<File> files) {
    for (File file : files) {
      this.addFile(file);
    }
  }

  public void addFile(File jar) {
    try {
      this.addURL(jar.getAbsoluteFile().toURI().toURL());
    } catch (MalformedURLException mux) {
      throw new RuntimeException("Failed to construct a classpath URL", mux);
    }
  }

  /**
   * Find and if necessary filter the jars for classpath.
   *
   * @return a list of {@link java.io.File}s
   */
  protected List<File> listJars(File path) {
    File[] jars = path.listFiles((FileFilter) pathname -> {
      try {
        return pathname.getCanonicalPath().endsWith(".jar");
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }
    });

    if (jars != null) {
      return Arrays.asList(jars);
    }
    return Collections.emptyList();
  }

  public List<File> getMuleJars() {
    return muleJars;
  }

  public List<File> getOptJars() {
    return optJars;
  }
}
