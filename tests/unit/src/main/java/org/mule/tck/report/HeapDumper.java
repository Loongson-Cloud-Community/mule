/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tck.report;

import static java.lang.System.currentTimeMillis;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

import javax.management.MBeanServer;

import com.sun.management.HotSpotDiagnosticMXBean;

public class HeapDumper {

  // This is the name of the HotSpot Diagnostic MBean
  private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

  // field to store the hotspot diagnostic MBean
  private static volatile HotSpotDiagnosticMXBean hotspotMBean;

  /**
   * Call this method from your application whenever you want to dump the heap snapshot into a file.
   *
   * @param fileName name of the heap dump file
   * @param live     flag that tells whether to dump only the live objects
   */
  static void dumpHeap(String fileName, boolean live) {
    // initialize hotspot diagnostic MBean
    initHotspotMBean();
    try {
      hotspotMBean.dumpHeap(fileName, live);
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception exp) {
      throw new RuntimeException(exp);
    }
  }

  // initialize the hotspot diagnostic MBean field
  private static void initHotspotMBean() {
    if (hotspotMBean == null) {
      synchronized (HeapDumper.class) {
        if (hotspotMBean == null) {
          hotspotMBean = getHotspotMBean();
        }
      }
    }
  }

  // get the hotspot diagnostic MBean from the
  // platform MBean server
  private static HotSpotDiagnosticMXBean getHotspotMBean() {
    try {
      MBeanServer server = getPlatformMBeanServer();
      HotSpotDiagnosticMXBean bean = newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
      return bean;
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception exp) {
      throw new RuntimeException(exp);
    }
  }

  public static void main(String[] args) {
    // default heap dump file name
    String fileName = "heap_" + currentTimeMillis() + ".hprof";
    // by default dump only the live objects
    boolean live = true;

    // simple command line options
    switch (args.length) {
      case 2:
        live = args[1].equals("true");
      case 1:
        fileName = args[0];
    }

    // dump the heap
    dumpHeap(fileName, live);
  }
}
