/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el.mvel.context;


import org.mule.runtime.core.internal.el.context.ServerContext;
import org.mule.runtime.core.internal.el.datetime.DateTime;

/**
 * This is for compatibility mode only adds the missing functionality that 3.x supports.
 * <li><b>nanoSeconds</b> <i>Current system time in nanoseconds</i>
 * <li><b>dateTime</b> <i>Current system time via a DateTime utility object (see below)</i>
 *
 * <b>dateTime</b>
 *
 * <li><b>milliSeconds, seconds, minutes, hours</b> <i>Integer values for milliSeconds, seconds and minutes.</i>
 * <li><b>dayOfWeek, dayOfMonth, dayOfYear</b> <i>Integer value for day of week, month and year.</i>
 * <li><b>weekOfMonth, weekOfYear</b> <i>Integer value for week of month and year</i>
 * <li><b>month</b> <i>Integer value for month of year</i>
 * <li><b>zone</b> <i>String. The TimeZone display name.</i>
 * <li><b>withTimeZone('timeZoneString')</b> <i>Changes TimeZone to that specified using TimeZone string identifier. Returns
 * DateTime for method chaining. (Does not alter system timeZone or affect other uses of server.dateTime)</i>
 * <li><b>withLocale('localeString')</b> <i>Changes DateTime Locale to that specified using Locale string identifier. Returns
 * DateTime for method chaining.</i> Returns DateTime for method chaining. (Does not alter system locale or affect other uses of
 * server.dateTime)</i>
 * <li><b>isBefore(DateTimeContext date)</b> <i>Boolean. Returns true if the date parameter is before the current DateTime.</i>
 * <li><b>isAfter(DateTimeContext date)</b> <i>Boolean. Returns true if the date parameter is after the current DateTime.</i>
 * <li><b>addSeconds(int seconds)</b> <i>Add n seconds to the current DateTime. Returns DateTime for method chaining.</i>
 * <li><b>addMinutes(int minutes)</b> <i>Add n minutes to the current DateTime. Returns DateTime for method chaining.</i>
 * <li><b>addHours(int hours)</b> <i>Add n hours to the current DateTime. Returns DateTime for method chaining.</i>
 * <li><b>addDay(int days)</b> <i>Add n days to the current DateTime. Returns DateTime for method chaining.</i>
 * <li><b>addWeeks(int weeks)</b> <i>Add n weeks to the current DateTime. Returns DateTime for method chaining.</i>
 * <li><b>addMonths(int months)</b> <i>Add n months to the current DateTime. Returns DateTime for method chaining.</i>
 * <li><b>addYears(int years)</b> <i>Add n years to the current DateTime. Returns DateTime for method chaining.</i>
 */
public class ExtendedServerContext extends ServerContext {

  public DateTime getDateTime() {
    return new DateTime();
  }

  public long nanoTime() {
    return System.nanoTime();
  }
}
