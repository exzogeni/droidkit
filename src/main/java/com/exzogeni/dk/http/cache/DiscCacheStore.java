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

package com.exzogeni.dk.http.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.AtomicFile;
import android.text.TextUtils;

import com.exzogeni.dk.crypto.Digest;
import com.exzogeni.dk.http.HttpDate;
import com.exzogeni.dk.io.BufferPoolInputStream;
import com.exzogeni.dk.io.BufferPoolOutputStream;
import com.exzogeni.dk.io.ByteBufferPool;
import com.exzogeni.dk.log.Logger;

import org.apache.commons.io.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Serdyukov
 */
public class DiscCacheStore implements CacheStore {

  private static final Map<String, String> ACCEPT_HEADERS = new ConcurrentHashMap<>(2);

  static {
    ACCEPT_HEADERS.put("etag", "If-None-Match");
    ACCEPT_HEADERS.put("last-modified", "If-Modified-Since");
  }

  private final File mCacheDir;

  public DiscCacheStore(@NonNull File cacheDir) {
    mCacheDir = cacheDir;
  }

  @Nullable
  @Override
  public InputStream get(@NonNull URI uri, @Nullable Map<String, List<String>> headers) throws IOException {
    final File cacheFile = new File(mCacheDir, Digest.getInstance().hash(uri.toString()));
    final File metaFile = new File(mCacheDir, "." + cacheFile.getName());
    if (cacheFile.exists() && metaFile.exists()) {
      if (headers == null) {
        return new AtomicFile(cacheFile).openRead();
      } else {
        final long expireTime = readMetaFile(metaFile, headers);
        Logger.debug("expireTime=%s, systemTime=%s", HttpDate.format(expireTime),
            HttpDate.format(System.currentTimeMillis()));
        if (expireTime > System.currentTimeMillis()) {
          return new AtomicFile(cacheFile).openRead();
        }
      }
    }
    return null;
  }

  @NonNull
  @Override
  public InputStream put(@NonNull URI uri, @NonNull Map<String, List<String>> headers, @NonNull InputStream content,
                         long maxAge) throws IOException {
    if (!mCacheDir.exists() && !mCacheDir.mkdirs()) {
      throw new IOException("Couldn't create directory " + mCacheDir);
    }
    final File cacheFile = new File(mCacheDir, Digest.getInstance().hash(uri.toString()));
    final File metaFile = new File(mCacheDir, "." + cacheFile.getName());
    saveMetaFile(metaFile, getMetaHeaders(headers), maxAge);
    return saveCacheFile(cacheFile, content);
  }

  @NonNull
  @Override
  public InputStream update(@NonNull URI uri, @NonNull Map<String, List<String>> headers, long maxAge)
      throws IOException {
    final File cacheFile = new File(mCacheDir, Digest.getInstance().hash(uri.toString()));
    final File metaFile = new File(mCacheDir, "." + cacheFile.getName());
    if (cacheFile.exists() && metaFile.exists()) {
      saveMetaFile(metaFile, getMetaHeaders(headers), maxAge);
      return new AtomicFile(cacheFile).openRead();
    }
    throw new FileNotFoundException(cacheFile.getAbsolutePath());
  }

  @NonNull
  private Map<String, List<String>> getMetaHeaders(@NonNull Map<String, List<String>> headers) {
    final Map<String, List<String>> metaHeaders = new HashMap<>(ACCEPT_HEADERS.size());
    for (final Map.Entry<String, List<String>> header : headers.entrySet()) {
      final String metaHeaderName = ACCEPT_HEADERS.get(header.getKey().toLowerCase());
      if (!TextUtils.isEmpty(metaHeaderName)) {
        metaHeaders.put(metaHeaderName, header.getValue());
      }
    }
    return metaHeaders;
  }

  private void saveMetaFile(@NonNull File metaFile, @NonNull Map<String, List<String>> metaHeaders, long maxAge)
      throws IOException {
    final AtomicFile af = new AtomicFile(metaFile);
    final FileOutputStream fos = af.startWrite();
    try {
      final DataOutputStream dat = new DataOutputStream(new BufferPoolOutputStream(fos));
      dat.writeLong(System.currentTimeMillis() + maxAge);
      dat.writeInt(metaHeaders.size());
      for (final Map.Entry<String, List<String>> header : metaHeaders.entrySet()) {
        dat.writeUTF(header.getKey());
        dat.writeInt(header.getValue().size());
        for (final String value : header.getValue()) {
          dat.writeUTF(value);
        }
      }
      IOUtils.closeQuietly(dat);
      af.finishWrite(fos);
    } catch (IOException e) {
      af.failWrite(fos);
      af.delete();
      throw e;
    }
  }

  private InputStream saveCacheFile(@NonNull File cacheFile, @NonNull InputStream content) throws IOException {
    final AtomicFile af = new AtomicFile(cacheFile);
    final FileOutputStream fos = af.startWrite();
    try {
      final OutputStream out = new BufferPoolOutputStream(fos);
      final byte[] buffer = ByteBufferPool.getInstance().obtain();
      try {
        IOUtils.copyLarge(content, out, buffer);
        IOUtils.closeQuietly(out);
        af.finishWrite(fos);
      } finally {
        ByteBufferPool.getInstance().free(buffer);
      }
    } catch (IOException e) {
      af.failWrite(fos);
      af.delete();
      throw e;
    }
    return af.openRead();
  }

  private long readMetaFile(@NonNull File metaFile, @NonNull Map<String, List<String>> headers) throws IOException {
    final AtomicFile af = new AtomicFile(metaFile);
    final FileInputStream fis = af.openRead();
    final DataInputStream dat = new DataInputStream(new BufferPoolInputStream(fis));
    try {
      final long expireTime = dat.readLong();
      int headersCount = dat.readInt();
      while (headersCount-- > 0) {
        final String name = dat.readUTF();
        int valuesCount = dat.readInt();
        final List<String> values = new ArrayList<>(valuesCount);
        while (valuesCount-- > 0) {
          values.add(dat.readUTF());
        }
        headers.put(name, values);
      }
      return expireTime;
    } finally {
      IOUtils.closeQuietly(dat);
    }
  }

}
