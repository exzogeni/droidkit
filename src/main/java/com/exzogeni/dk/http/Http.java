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

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Daniel Serdyukov
 */
public final class Http {

  private static final AtomicReference<HttpManager> HTTP_MANAGER = new AtomicReference<>(new HttpManager());

  private Http() {
  }

  public static void setHttpManager(HttpManager manager) {
    HTTP_MANAGER.compareAndSet(HTTP_MANAGER.get(), manager);
  }

  @NonNull
  public static <V> HttpTask<V> head(@NonNull String url) {
    return newTask(Method.HEAD, url);
  }

  @NonNull
  public static <V> HttpTask<V> get(@NonNull String url) {
    return newTask(Method.GET, url);
  }

  @NonNull
  public static <V> HttpTask<V> post(@NonNull String url) {
    return newTask(Method.POST, url);
  }

  @NonNull
  public static <V> HttpTask<V> put(@NonNull String url) {
    return newTask(Method.PUT, url);
  }

  @NonNull
  public static <V> HttpTask<V> delete(@NonNull String url) {
    return newTask(Method.DELETE, url);
  }

  @NonNull
  public static <V> HttpTask<V> newTask(@NonNull String method, @NonNull String url) {
    return HTTP_MANAGER.get().newTask(method, url);
  }

  public interface Method {
    String HEAD = "HEAD";
    String GET = "GET";
    String POST = "POST";
    String PUT = "PUT";
    String DELETE = "DELETE";
  }

}
