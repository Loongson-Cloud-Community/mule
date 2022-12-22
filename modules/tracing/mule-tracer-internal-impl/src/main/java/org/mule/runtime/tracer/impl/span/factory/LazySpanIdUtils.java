/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.tracer.impl.span.factory;

import org.mule.runtime.tracer.api.span.InternalSpan;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class LazySpanIdUtils {

  private static final Supplier<Random> randomSupplier = ThreadLocalRandom::current;

  private static final long INVALID_ID = 0;

  private static final ThreadLocal<char[]> CHAR_ARRAY = new ThreadLocal<>();

  private static final String INVALID = "0000000000000000";

  private static final String ALPHABET = "0123456789abcdef";

  private static final int BYTES_LENGTH = 16;

  private static final int HEX_LENGTH = 2 * BYTES_LENGTH;

  static final int BYTE_BASE16 = 2;

  private static final char[] ENCODING = buildEncodingArray();

  static final String TRACE_PARENT = "traceparent";
  static final String TRACE_STATE = "tracestate";

  private static final int HEX_LENGTH_SPAN_ID = 16;
  private static final String VERSION = "00";
  private static final int VERSION_SIZE = 2;
  private static final char TRACEPARENT_DELIMITER = '-';
  private static final int TRACEPARENT_DELIMITER_SIZE = 1;
  private static final int TRACE_ID_HEX_SIZE = HEX_LENGTH;
  private static final int SPAN_ID_HEX_SIZE = HEX_LENGTH_SPAN_ID;
  private static final int TRACE_OPTION_HEX_SIZE = 2;
  private static final int TRACE_ID_OFFSET = VERSION_SIZE + TRACEPARENT_DELIMITER_SIZE;
  private static final int SPAN_ID_OFFSET =
      TRACE_ID_OFFSET + TRACE_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
  private static final int TRACE_OPTION_OFFSET =
      SPAN_ID_OFFSET + SPAN_ID_HEX_SIZE + TRACEPARENT_DELIMITER_SIZE;
  private static final int TRACEPARENT_HEADER_SIZE = TRACE_OPTION_OFFSET + TRACE_OPTION_HEX_SIZE;


  private static char[] buildEncodingArray() {
    char[] encoding = new char[512];
    for (int i = 0; i < 256; ++i) {
      encoding[i] = ALPHABET.charAt(i >>> 4);
      encoding[i | 0x100] = ALPHABET.charAt(i & 0xF);
    }
    return encoding;
  }

  public static String fromLong(long id) {
    if (id == 0) {
      return getInvalid();
    }
    char[] result = chars(HEX_LENGTH_SPAN_ID);
    longToBase16String(id, result, 0);
    return new String(result, 0, HEX_LENGTH_SPAN_ID);
  }

  public static void longToBase16String(long value, char[] dest, int destOffset) {
    byteToBase16((byte) (value >> 56 & 0xFFL), dest, destOffset);
    byteToBase16((byte) (value >> 48 & 0xFFL), dest, destOffset + BYTE_BASE16);
    byteToBase16((byte) (value >> 40 & 0xFFL), dest, destOffset + 2 * BYTE_BASE16);
    byteToBase16((byte) (value >> 32 & 0xFFL), dest, destOffset + 3 * BYTE_BASE16);
    byteToBase16((byte) (value >> 24 & 0xFFL), dest, destOffset + 4 * BYTE_BASE16);
    byteToBase16((byte) (value >> 16 & 0xFFL), dest, destOffset + 5 * BYTE_BASE16);
    byteToBase16((byte) (value >> 8 & 0xFFL), dest, destOffset + 6 * BYTE_BASE16);
    byteToBase16((byte) (value & 0xFFL), dest, destOffset + 7 * BYTE_BASE16);
  }

  public static void byteToBase16(byte value, char[] dest, int destOffset) {
    int b = value & 0xFF;
    dest[destOffset] = ENCODING[b];
    dest[destOffset + 1] = ENCODING[b | 0x100];
  }

  public static char[] chars(int len) {
    char[] buffer = CHAR_ARRAY.get();
    if (buffer == null || buffer.length < len) {
      buffer = new char[len];
      CHAR_ARRAY.set(buffer);
    }
    return buffer;
  }

  public static String fromLongs(long traceIdLongHighPart, long traceIdLongLowPart) {
    if (traceIdLongHighPart == 0 && traceIdLongLowPart == 0) {
      return getInvalid();
    }
    char[] chars = chars(HEX_LENGTH);
    longToBase16String(traceIdLongHighPart, chars, 0);
    longToBase16String(traceIdLongLowPart, chars, 16);
    return new String(chars, 0, HEX_LENGTH);
  }

  public static String getInvalid() {
    return INVALID;
  }

  public static String generateSpanId() {
    long id;
    Random random = randomSupplier.get();
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return fromLong(id);
  }

  public static String generateTraceId(InternalSpan parentInternalSpan) {
    if (parentInternalSpan instanceof LazyExecutionSpan) {
      return ((LazyExecutionSpan) parentInternalSpan).getTraceId();
    }

    Random random = randomSupplier.get();
    long idHi = random.nextLong();
    long idLo;
    do {
      idLo = random.nextLong();
    } while (idLo == INVALID_ID);
    return fromLongs(idHi, idLo);
  }

  public static Map<String, String> getContext(LazyExecutionSpan lazyExecutionSpan) {
    Map<String, String> context = new HashMap<>();
    if (lazyExecutionSpan.getSpanId() == INVALID) {
      return Collections.emptyMap();
    }

    char[] chars = chars(TRACEPARENT_HEADER_SIZE);
    chars[0] = VERSION.charAt(0);
    chars[1] = VERSION.charAt(1);
    chars[2] = TRACEPARENT_DELIMITER;

    String traceId = lazyExecutionSpan.getTraceId();
    traceId.getChars(0, traceId.length(), chars, TRACE_ID_OFFSET);

    chars[SPAN_ID_OFFSET - 1] = TRACEPARENT_DELIMITER;

    String spanId = lazyExecutionSpan.getSpanId();
    spanId.getChars(0, spanId.length(), chars, SPAN_ID_OFFSET);

    chars[TRACE_OPTION_OFFSET - 1] = TRACEPARENT_DELIMITER;
    chars[TRACE_OPTION_OFFSET] = '0';
    chars[TRACE_OPTION_OFFSET + 1] = '1';
    context.put(TRACE_PARENT, new String(chars, 0, TRACEPARENT_HEADER_SIZE));

    return context;
  }
}
