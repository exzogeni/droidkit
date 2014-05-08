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
import com.exzogeni.dk.http.task.HttpFactory;
import com.exzogeni.dk.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Daniel Serdyukov
 */
public class HttpManager {

  private static final long SLOW_LOG_THRESHOLD = 3000;

  private final ReentrantLock mConfigLock = new ReentrantLock();

  private final AtomicInteger mTimeoutMs = new AtomicInteger(30000);

  private final AsyncQueue mAsyncQueue;

  private final HttpFactory mFactory;

  private final Map<String, List<String>> mHeaders = new ConcurrentHashMap<>();

  private CookieManager mCookieManager = new CookieManager();

  public HttpManager() {
    this(HttpFactory.DEFAULT);
  }

  public HttpManager(@NonNull HttpFactory factory) {
    this(AsyncQueue.get(), factory);
  }

  public HttpManager(@NonNull AsyncQueue queue, @NonNull HttpFactory factory) {
    mAsyncQueue = queue;
    mFactory = factory;
  }

  public HttpManager setTimeoutMs(int timeoutMs) {
    if (timeoutMs > 0) {
      mTimeoutMs.set(timeoutMs);
      return this;
    }
    throw new IllegalArgumentException("timeoutMs must be positive int");
  }

  @NonNull
  public HttpManager setCookieManager(@NonNull CookieManager manager) {
    mConfigLock.lock();
    try {
      mCookieManager = manager;
    } finally {
      mConfigLock.unlock();
    }
    return this;
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

  // Hidden API

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

  @NonNull
  Map<String, List<String>> getCookies(@NonNull URI uri, @NonNull Map<String, List<String>> headers)
      throws IOException {
    mConfigLock.lock();
    try {
      return mCookieManager.get(uri, headers);
    } finally {
      mConfigLock.unlock();
    }
  }

  void saveCookies(@NonNull URI uri, @NonNull Map<String, List<String>> headers) throws IOException {
    mConfigLock.lock();
    try {
      mCookieManager.put(uri, headers);
    } finally {
      mConfigLock.unlock();
    }
  }

  InputStream saveToCache(@NonNull URI uri, int statusCode, @NonNull Map<String, List<String>> headers,
                          @NonNull InputStream content) {
    return content;
  }

  void log(@NonNull HttpTask<?> task, long execTime, String statusLine) {
    if (execTime > SLOW_LOG_THRESHOLD) {
      Logger.error("%s - %s [%d ms] SLOW REQUEST", task, statusLine, execTime);
    } else {
      Logger.info("%s - %s [%d ms]", task, statusLine, execTime);
    }
  }

}
