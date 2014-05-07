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

package com.exzogeni.dk.log;

import com.exzogeni.dk.log.appender.LogAppender;
import com.exzogeni.dk.log.formatter.LogFormatter;
import com.exzogeni.dk.log.policy.LogPolicy;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Daniel Serdyukov
 */
public class LogManager {

  private final Collection<LogAppender> mAppenders = new CopyOnWriteArrayList<>();

  private final AtomicReference<String> mLogTag = new AtomicReference<>(Logger.class.getSimpleName());

  private final AtomicBoolean mEnabled = new AtomicBoolean();

  private final LogPolicy mPolicy;

  private final LogFormatter mFormatter;

  public LogManager() {
    this(LogPolicy.LOG_NONE);
  }

  public LogManager(LogPolicy policy) {
    this(policy, LogFormatter.SIMPLE);
  }

  public LogManager(LogPolicy policy, LogFormatter formatter) {
    if (policy == null) {
      throw new IllegalArgumentException("LogPolicy is null");
    }
    if (formatter == null) {
      throw new IllegalArgumentException("LogFormatter is null");
    }
    mPolicy = policy;
    mFormatter = formatter;
  }

  public LogManager setLogTag(String logTag) {
    mLogTag.compareAndSet(mLogTag.get(), logTag);
    return this;
  }

  public LogManager setEnabled(boolean enabled) {
    mEnabled.compareAndSet(mEnabled.get(), enabled);
    return this;
  }

  public LogManager addAppenders(LogAppender... appenders) {
    Collections.addAll(mAppenders, appenders);
    return this;
  }

  void debug(Thread thread, StackTraceElement caller, String format, Object... args) {
    if (mEnabled.get() && mPolicy.shouldLog(thread, caller)) {
      for (final LogAppender appender : mAppenders) {
        appender.debug(mLogTag.get(), mFormatter.format(thread, caller, format, args));
      }
    }
  }

  void info(Thread thread, StackTraceElement caller, String format, Object... args) {
    if (mEnabled.get() && mPolicy.shouldLog(thread, caller)) {
      for (final LogAppender appender : mAppenders) {
        appender.info(mLogTag.get(), mFormatter.format(thread, caller, format, args));
      }
    }
  }

  void warn(Thread thread, StackTraceElement caller, String format, Object... args) {
    if (mEnabled.get() && mPolicy.shouldLog(thread, caller)) {
      for (final LogAppender appender : mAppenders) {
        appender.warn(mLogTag.get(), mFormatter.format(thread, caller, format, args));
      }
    }
  }

  void error(Thread thread, StackTraceElement caller, String format, Object... args) {
    if (mEnabled.get() && mPolicy.shouldLog(thread, caller)) {
      for (final LogAppender appender : mAppenders) {
        appender.error(mLogTag.get(), mFormatter.format(thread, caller, format, args));
      }
    }
  }

  void error(Thread thread, StackTraceElement caller, Throwable e) {
    if (mEnabled.get() && mPolicy.shouldLog(thread, caller)) {
      for (final LogAppender appender : mAppenders) {
        appender.error(mLogTag.get(), mFormatter.format(thread, caller, e));
      }
    }
  }

}
