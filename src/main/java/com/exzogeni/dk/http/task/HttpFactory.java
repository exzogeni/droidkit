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

package com.exzogeni.dk.http.task;

import android.support.annotation.NonNull;

import com.exzogeni.dk.http.HttpTask;

/**
 * @author Daniel Serdyukov
 */
public interface HttpFactory {

  HttpFactory DEFAULT = new DefaultHttpFactory();

  @NonNull
  <V> HttpTask<V> newHttpTask(@NonNull String method, @NonNull String url);

}
