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
import android.text.format.DateUtils;

import com.exzogeni.dk.http.HttpDate;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Serdyukov
 */
public class DefaultCachePolicy implements CachePolicy {

  static final String HEADER_CACHE_CONTROL = "cache-control";

  static final String HEADER_EXPIRES = "expires";

  private static final String NO_CACHE = "no-cache";

  private static final Pattern MAX_AGE = Pattern.compile("max\\-age=([\\d]+)");

  @Override
  public boolean shouldCache(@NonNull URI uri) {
    return true;
  }

  @Override
  public long maxAge(@NonNull Map<String, List<String>> headers) {
    long maxAge = 0;
    for (final String key : headers.keySet()) {
      if (HEADER_CACHE_CONTROL.equalsIgnoreCase(key)) {
        maxAge = getMaxAge(headers.get(key)) * DateUtils.SECOND_IN_MILLIS;
      } else if (HEADER_EXPIRES.equalsIgnoreCase(key)) {
        maxAge = getExpires(headers.get(key));
      }
    }
    return maxAge;
  }

  private long getMaxAge(List<String> header) {
    if (header.get(0).toLowerCase().contains(NO_CACHE)) {
      return 0;
    }
    final Matcher matcher = MAX_AGE.matcher(header.get(0));
    if (matcher.find()) {
      return Long.parseLong(matcher.group(1));
    }
    return 0;
  }

  private long getExpires(List<String> header) {
    return HttpDate.parse(header.get(0)).getTime() - System.currentTimeMillis();
  }

}
