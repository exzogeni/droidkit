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

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.exzogeni.dk.http.callback.HttpCallback;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daniel Serdyukov
 */
public abstract class HttpTask<V> implements Callable<V> {

  private static final AtomicInteger SEQUENCE = new AtomicInteger();

  private final AtomicInteger mStatusCode = new AtomicInteger();

  private final int mSequence = SEQUENCE.incrementAndGet();

  private final Map<String, List<String>> mHeaders = new ConcurrentHashMap<>();

  private final String mUrl;

  private HttpManager mManager;

  private HttpCallback<V> mCallback;

  private int mTimeoutMs;

  private String mEncodedUrl;

  protected HttpTask(@NonNull String url) {
    mUrl = url;
  }

  private static Map<String, List<String>> getHeaderFields(HttpURLConnection cn) {
    final Map<String, List<String>> headers = cn.getHeaderFields();
    if (headers != null) {
      final Map<String, List<String>> localHeaders = new HashMap<>(headers);
      localHeaders.remove(null);
      return Collections.unmodifiableMap(localHeaders);
    }
    return Collections.emptyMap();
  }

  private static InputStream getInputStream(HttpURLConnection cn) {
    try {
      return cn.getInputStream();
    } catch (IOException e) {
      final InputStream stream = cn.getErrorStream();
      if (stream == null) {
        return new ByteArrayInputStream(new byte[0]);
      }
      return stream;
    }
  }

  @NonNull
  public String getUrl() {
    return mUrl;
  }

  @NonNull
  public HttpTask<V> setCallback(@Nullable HttpCallback<V> callback) {
    mCallback = callback;
    return this;
  }

  @NonNull
  public HttpTask<V> setTimeoutMs(int timeoutMs) {
    mTimeoutMs = timeoutMs;
    return this;
  }

  @NonNull
  public HttpTask<V> addHeader(@NonNull String key, @NonNull String... values) {
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
  public Future<V> submit() {
    return mManager.submit(this);
  }

  @Override
  public V call() throws Exception {
    final long startTime = SystemClock.uptimeMillis();
    final HttpURLConnection cn = openConnection();
    try {
      onPrepareConnectionInternal(cn);
      onPerformRequest(cn);
      return onSuccessInternal(cn);
    } finally {
      cn.disconnect();
      mManager.log(this, (SystemClock.uptimeMillis() - startTime), HttpStatus.getStatusLine(mStatusCode.get()));
    }
  }

  @Override
  public String toString() {
    return "[" + mSequence + "] " + getMethodName() + " " + getEncodedUrlInternal();
  }

  @NonNull
  protected abstract String getMethodName();

  @NonNull
  protected abstract String getEncodedUrl();

  @NonNull
  protected HttpURLConnection openConnection() throws Exception {
    try {
      return (HttpURLConnection) new URL(getEncodedUrlInternal()).openConnection();
    } catch (IOException e) {
      throw onException(new HttpException(getEncodedUrlInternal(), e));
    }
  }

  protected void onPrepareConnection(HttpURLConnection cn) throws Exception {

  }

  protected void onPerformRequest(HttpURLConnection cn) throws Exception {

  }

  void setHttpManager(@NonNull HttpManager manager) {
    mManager = manager;
  }

  void addHeaders(@NonNull Map<String, List<String>> headers) {
    mHeaders.putAll(headers);
  }

  private HttpException onException(HttpException e) {
    if (mCallback != null) {
      mCallback.onException(e);
    }
    return e;
  }

  @NonNull
  private String getEncodedUrlInternal() {
    if (TextUtils.isEmpty(mEncodedUrl)) {
      mEncodedUrl = getEncodedUrl();
    }
    return mEncodedUrl;
  }

  private void onPrepareConnectionInternal(HttpURLConnection cn) throws Exception {
    final URI uri = cn.getURL().toURI();
    cn.setRequestMethod(getMethodName());
    cn.setConnectTimeout(mTimeoutMs);
    cn.setReadTimeout(mTimeoutMs);
    final Map<String, List<String>> cookies = mManager.getCookies(uri, Collections.<String, List<String>>emptyMap());
    for (final Map.Entry<String, List<String>> cookie : cookies.entrySet()) {
      for (final String value : cookie.getValue()) {
        cn.addRequestProperty(cookie.getKey(), value);
      }
    }
    for (final Map.Entry<String, List<String>> header : mHeaders.entrySet()) {
      for (final String value : header.getValue()) {
        cn.addRequestProperty(header.getKey(), value);
      }
    }
    onPrepareConnection(cn);
  }

  @SuppressWarnings("checkstyle:illegalcatch")
  private V onSuccessInternal(HttpURLConnection cn) throws Exception {
    try {
      mStatusCode.compareAndSet(mStatusCode.get(), cn.getResponseCode());
      final URI uri = cn.getURL().toURI();
      final Map<String, List<String>> headers = getHeaderFields(cn);
      mManager.saveCookies(uri, headers);
      final InputStream content = mManager.saveToCache(uri, mStatusCode.get(), headers, getInputStream(cn));
      try {
        if (mCallback != null) {
          return mCallback.onSuccess(mStatusCode.get(), headers, content);
        }
      } finally {
        IOUtils.closeQuietly(content);
      }
    } catch (Exception e) {
      throw onException(new HttpException(getEncodedUrlInternal(), e));
    }
    return null;
  }

}
