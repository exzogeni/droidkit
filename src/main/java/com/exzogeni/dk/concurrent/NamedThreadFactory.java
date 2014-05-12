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

package com.exzogeni.dk.concurrent;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daniel Serdyukov
 */
public class NamedThreadFactory implements ThreadFactory {

  private final AtomicInteger mSequence = new AtomicInteger();

  private final String mThreadName;

  private ThreadGroup mThreadGroup;

  public NamedThreadFactory() {
    this("thread");
  }

  public NamedThreadFactory(String threadName) {
    mThreadName = threadName;
    final SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      mThreadGroup = sm.getThreadGroup();
    }
  }

  @Override
  public Thread newThread(@NonNull Runnable r) {
    return new Thread(mThreadGroup, r, mThreadName + " #" + mSequence.incrementAndGet(), 0);
  }

}
