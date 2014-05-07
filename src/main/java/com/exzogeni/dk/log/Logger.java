/*
 * Copyright 2012-2014 Daniel Serdyukov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exzogeni.dk.log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Daniel Serdyukov
 */
public final class Logger {

  private static final AtomicReference<LogManager> LOG_MANAGER_REF = new AtomicReference<>();

  static {
    LOG_MANAGER_REF.compareAndSet(null, new LogManager());
  }

  private Logger() {
  }

  public static void setLogManager(LogManager lm) {
    LOG_MANAGER_REF.compareAndSet(LOG_MANAGER_REF.get(), lm);
  }

  public static void quiet(String format, Object... args) {

  }

  public static void debug(String format, Object... args) {
    LOG_MANAGER_REF.get().debug(Thread.currentThread(), obtainCaller(), format, args);
  }

  public static void info(String format, Object... args) {
    LOG_MANAGER_REF.get().info(Thread.currentThread(), obtainCaller(), format, args);
  }

  public static void warn(String format, Object... args) {
    LOG_MANAGER_REF.get().warn(Thread.currentThread(), obtainCaller(), format, args);
  }

  public static void error(String format, Object... args) {
    LOG_MANAGER_REF.get().error(Thread.currentThread(), obtainCaller(), format, args);
  }

  public static void error(Throwable e) {
    LOG_MANAGER_REF.get().error(Thread.currentThread(), obtainCaller(), e);
  }

  private static StackTraceElement obtainCaller() {
    final StackTraceElement[] stackTrace = new IOException().fillInStackTrace().getStackTrace();
    for (final StackTraceElement ste : stackTrace) {
      final String className = ste.getClassName();
      if (!className.equals(Logger.class.getName())) {
        return ste;
      }
    }
    return stackTrace[stackTrace.length - 1];
  }

}
