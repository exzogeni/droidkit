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

package com.exzogeni.dk.http;

import android.support.annotation.NonNull;

import com.exzogeni.dk.concurrent.AsyncQueue;
import com.exzogeni.dk.http.cache.CacheManager;
import com.exzogeni.dk.http.task.HttpFactory;
import com.exzogeni.dk.log.Logger;

import java.net.CookieManager;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daniel Serdyukov
 */
public class HttpManager {

  private static final long SLOW_LOG_THRESHOLD = 3000;

  private final AtomicInteger mTimeoutMs = new AtomicInteger(30000);

  private final AtomicBoolean mLogging = new AtomicBoolean();

  private final AsyncQueue mAsyncQueue;

  private final HttpFactory mFactory;

  private final CookieManager mCookieManager;

  private final CacheManager mCacheManager;

  private final Map<String, List<String>> mHeaders = new ConcurrentHashMap<>();

  public HttpManager() {
    this(AsyncQueue.get());
  }

  public HttpManager(@NonNull AsyncQueue queue) {
    this(queue, new CookieManager(), new CacheManager());
  }

  public HttpManager(@NonNull CookieManager cookieManager,
                     @NonNull CacheManager cacheManager) {
    this(AsyncQueue.get(), cookieManager, cacheManager);
  }

  public HttpManager(@NonNull AsyncQueue queue, @NonNull CookieManager cookieManager,
                     @NonNull CacheManager cacheManager) {
    this(queue, HttpFactory.DEFAULT, cookieManager, cacheManager);
  }

  public HttpManager(@NonNull AsyncQueue queue, @NonNull HttpFactory factory, @NonNull CookieManager cookieManager,
                     @NonNull CacheManager cacheManager) {
    mAsyncQueue = queue;
    mFactory = factory;
    mCookieManager = cookieManager;
    mCacheManager = cacheManager;
  }

  public HttpManager setTimeoutMs(int timeoutMs) {
    if (timeoutMs > 0) {
      mTimeoutMs.set(timeoutMs);
      return this;
    }
    throw new IllegalArgumentException("timeoutMs must be positive int");
  }

  @NonNull
  public HttpManager addHeader(@NonNull String key, @NonNull String... values) {
    List<String> headers = mHeaders.get(key);
    if (headers == null) {
      headers = new CopyOnWriteArrayList<>();
      mHeaders.put(key, headers);
    }
    headers.clear();
    Collections.addAll(headers, values);
    return this;
  }

  @NonNull
  public HttpManager setLoggingEnabled(boolean enabled) {
    mLogging.compareAndSet(mLogging.get(), enabled);
    return this;
  }

  //@hide

  <V> HttpTask<V> newTask(@NonNull String method, @NonNull String url) {
    final HttpTask<V> task = mFactory.newHttpTask(method, url);
    task.setHttpManager(this);
    task.setTimeoutMs(mTimeoutMs.get());
    task.addHeaders(Collections.unmodifiableMap(mHeaders));
    return task;
  }

  @NonNull
  <V> Future<V> submit(@NonNull HttpTask<V> task) {
    return mAsyncQueue.submit(task);
  }

  void log(@NonNull HttpTask<?> task, long execTime, String statusLine) {
    if (mLogging.get()) {
      if (execTime > SLOW_LOG_THRESHOLD) {
        Logger.error("%s - %s [%d ms] SLOW REQUEST", task, statusLine, execTime);
      } else {
        Logger.info("%s - %s [%d ms]", task, statusLine, execTime);
      }
    }
  }

  @NonNull
  CookieManager getCookieManager() {
    return mCookieManager;
  }

  @NonNull
  CacheManager getCacheManager() {
    return mCacheManager;
  }

}
