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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Daniel Serdyukov
 */
public class BufferPoolInputStream extends BufferedInputStream {

  public BufferPoolInputStream(InputStream in) {
    super(in, 1);
    buf = ByteBufferPool.getInstance().obtain();
  }

  @Override
  public void close() throws IOException {
    final byte[] buffer = buf;
    try {
      super.close();
    } finally {
      ByteBufferPool.getInstance().free(buffer);
    }
  }

}
