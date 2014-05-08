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

import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
public class ByteBufferPool extends BufferPool<byte[]> {

  public static ByteBufferPool getInstance() {
    return Holder.INSTANCE;
  }

  @Override
  public void free(@NonNull byte[] entry) {
    Arrays.fill(entry, (byte) 0);
    super.free(entry);
  }

  @NonNull
  @Override
  protected byte[] allocateBuffer() {
    return new byte[BUFFER_SIZE];
  }

  private static final class Holder {
    public static final ByteBufferPool INSTANCE = new ByteBufferPool();
  }

}
