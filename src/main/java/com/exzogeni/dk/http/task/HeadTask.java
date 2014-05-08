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

import android.net.Uri;
import android.support.annotation.NonNull;

import com.exzogeni.dk.http.Http;
import com.exzogeni.dk.http.HttpTask;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Daniel Serdyukov
 */
class HeadTask<V> extends HttpTask<V> {

  protected HeadTask(@NonNull String url) {
    super(url);
  }

  @NonNull
  @Override
  protected String getMethodName() {
    return Http.Method.HEAD;
  }

  @NonNull
  @Override
  protected String getEncodedUrl() {
    final Uri uri = Uri.parse(getUrl());
    try {
      return new URI(
          uri.getScheme(), uri.getAuthority(),
          uri.getPath(), uri.getQuery(), uri.getFragment()
      ).toASCIIString();
    } catch (URISyntaxException e) {
      return getUrl();
    }
  }

}
