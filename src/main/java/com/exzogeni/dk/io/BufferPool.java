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

package com.exzogeni.dk.io;

import android.support.annotation.NonNull;

import com.exzogeni.dk.log.Logger;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Daniel Serdyukov
 */
public abstract class BufferPool<T> {

  protected static final int BUFFER_SIZE = 128 * 1024;

  private final Queue<T> mPool = new ConcurrentLinkedQueue<>();

  @NonNull
  public T obtain() {
    T buffer = mPool.poll();
    if (buffer == null) {
      buffer = allocateBufferInternal();
    }
    return buffer;
  }

  public void free(@NonNull T entry) {
    mPool.offer(entry);
  }

  public void onLowMemory() {
    mPool.clear();
  }

  @NonNull
  protected abstract T allocateBuffer();

  @NonNull
  private T allocateBufferInternal() {
    Logger.error(new IOException("allocate new buffer"));
    return allocateBuffer();
  }

}
