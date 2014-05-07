/*
 * Copyright (c) 2012-2014 Daniel Serdyukov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exzogeni.dk.log.formatter;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

/**
 * @author Daniel Serdyukov
 */
public class SimpleLogFormatter implements LogFormatter {

  private static StringBuilder newMessageBuilder(Thread thread, StackTraceElement caller) {
    return new StringBuilder(256)
        .append("[").append(thread.getName()).append("]")
        .append(" ").append(caller).append(" >>>>>\n");
  }

  @Override
  public String format(Thread thread, StackTraceElement caller, String format, Object... args) {
    final StringBuilder message = newMessageBuilder(thread, caller);
    if (args.length > 0) {
      message.append(String.format(Locale.US, format, args));
    } else {
      message.append(format);
    }
    return message.toString();
  }

  @Override
  public String format(Thread thread, StackTraceElement caller, Throwable e) {
    final StringBuilder message = newMessageBuilder(thread, caller);
    final StringWriter trace = new StringWriter();
    final PrintWriter traceWriter = new PrintWriter(new BufferedWriter(trace, 512), true);
    try {
      e.printStackTrace(traceWriter);
    } finally {
      traceWriter.flush();
      IOUtils.closeQuietly(traceWriter);
    }
    return message.append(trace.toString()).toString();
  }

}
