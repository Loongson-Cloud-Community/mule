/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.functional.api.component;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.runtime.api.exception.MuleException.EXCEPTION_MESSAGE_SECTION_DELIMITER;

import org.mule.runtime.api.component.AbstractComponent;

import java.util.List;
import java.util.regex.Pattern;

import org.hamcrest.Matcher;

public abstract class AbstractLogChecker extends AbstractComponent implements LogChecker {

  protected static final Pattern STACKTRACE_METHOD_CALL_REGEX_PATTERN =
      compile("^.*at ([^A-Z]*)\\.([0-9A-Z]+[^\\.]*)\\.([^\\(]*)\\([^):]*[:]?([0-9]*).*");
  protected static final Pattern STACKTRACE_FILTERED_ENTRY_REGEX_PATTERN =
      compile("^.*at ([^A-Z]*)\\.\\* \\(([0-9]+) elements filtered from stack()().*");
  protected static final Pattern STACKTRACE_EXCEPTION_CAUSE_REGEX_PATTERN =
      compile("\\s*(Caused by: )?([a-zA-Z0-9\\.]+(Exception|Error)+)(: .*|\\z)");
  protected static final Pattern STACKTRACE_COLLAPSED_INFORMATION_REGEX_PATTERN = compile(".*\\.\\.\\. [0-9]* more.*");



  @Override
  public abstract void check(String logMessage);

  /**
   * Extracts the message part of the log. Separating it from the stack trace.
   *
   * @return a string list with the log message lines
   */
  protected List<String> getMessageLinesFromLogLines(List<String> initialLogLines) {
    int index = initialLogLines.indexOf(EXCEPTION_MESSAGE_SECTION_DELIMITER.trim());
    if (index > -1) {
      return initialLogLines.subList(0, index);
    } else {
      return removeStacktraceLines(initialLogLines);
    }
  }

  /**
   * Extracts the stack trace from the log, ignoring any other information that could be there.
   *
   * @return a string list with the log stack trace lines.
   */
  protected List<String> getStacktraceLinesFromLogLines(List<String> initialLogLines) {
    int index = initialLogLines.indexOf(EXCEPTION_MESSAGE_SECTION_DELIMITER.trim());
    if (index > -1) {
      return initialLogLines.subList(index, initialLogLines.size());
    } else {
      return getStacktraceLines(initialLogLines);
    }
  }

  private boolean isStacktrace(String line) {
    return (STACKTRACE_METHOD_CALL_REGEX_PATTERN.matcher(line).matches()
        || STACKTRACE_FILTERED_ENTRY_REGEX_PATTERN.matcher(line).matches()
        || STACKTRACE_EXCEPTION_CAUSE_REGEX_PATTERN.matcher(line).matches()
        || STACKTRACE_COLLAPSED_INFORMATION_REGEX_PATTERN.matcher(line).matches());
  }

  private List<String> getStacktraceLines(List<String> allLines) {
    return allLines.stream().filter((line) -> isStacktrace(line)).collect(toList());
  }

  private List<String> removeStacktraceLines(List<String> allLines) {
    return allLines.stream().filter((line) -> !isStacktrace(line)).collect(toList());
  }

  protected List<String> splitLines(String wholeMessage) {
    // The following approach is required until the usage of line separators get fixed at
    // org.mule.runtime.api.exception.MuleException#getSummaryMessage. More info at: MULE-15932
    return asList(wholeMessage.replaceAll("\r", "").split("\n"));
  }

  protected <T> boolean assertAndSaveError(T checkedValue, Matcher<T> comparison, String failureMessagePrefix,
                                           StringBuilder errorCatcher) {
    try {
      assertThat(checkedValue, comparison);
    } catch (AssertionError e) {
      errorCatcher.append(lineSeparator());
      errorCatcher.append(failureMessagePrefix);
      errorCatcher.append(e.getMessage());
      errorCatcher.append(lineSeparator());
      return false;
    }
    return true;
  }

}
