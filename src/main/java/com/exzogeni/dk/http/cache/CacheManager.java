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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Serdyukov
 */
public class CacheManager {

  private final CachePolicy mCachePolicy;

  private final CacheStore mCacheStore;

  public CacheManager() {
    this(CachePolicy.NO_CACHE, null);
  }

  public CacheManager(@NonNull CacheStore store) {
    this(CachePolicy.DEFAULT, store);
  }

  public CacheManager(@NonNull CachePolicy policy, @Nullable CacheStore store) {
    mCachePolicy = policy;
    mCacheStore = store;
  }

  @Nullable
  public InputStream get(@NonNull URI uri, @Nullable Map<String, List<String>> headers) throws IOException {
    if (mCacheStore != null) {
      return mCacheStore.get(uri, headers);
    }
    return null;
  }

  @NonNull
  public InputStream put(@NonNull URI uri, @NonNull Map<String, List<String>> headers, @NonNull InputStream content)
      throws IOException {
    if (mCacheStore != null && mCachePolicy.shouldCache(uri)) {
      final long maxAge = mCachePolicy.maxAge(headers);
      if (maxAge > 0) {
        return mCacheStore.put(uri, headers, content, maxAge);
      }
    }
    return content;
  }

  @NonNull
  public InputStream update(@NonNull URI uri, @NonNull Map<String, List<String>> headers) throws IOException {
    if (mCacheStore != null && mCachePolicy.shouldCache(uri)) {
      return mCacheStore.update(uri, headers, mCachePolicy.maxAge(headers));
    }
    throw new IllegalStateException("update operation not supported");
  }

}
