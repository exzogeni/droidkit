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
import android.util.SparseArray;

/**
 * @author Daniel Serdyukov
 */
public final class HttpStatus {

  private static final String HTTP_UNKNOWN = "HTTP_UNKNOWN";

  private static final SparseArray<String> STATUS_LINES = new SparseArray<>();

  static {
    STATUS_LINES.put(200, "HTTP_OK");
    STATUS_LINES.put(201, "HTTP_CREATED");
    STATUS_LINES.put(202, "HTTP_ACCEPTED");
    STATUS_LINES.put(203, "HTTP_NOT_AUTHORITATIVE");
    STATUS_LINES.put(204, "HTTP_NO_CONTENT");
    STATUS_LINES.put(205, "HTTP_RESET");
    STATUS_LINES.put(206, "HTTP_PARTIAL");
    STATUS_LINES.put(300, "HTTP_MULT_CHOICE");
    STATUS_LINES.put(301, "HTTP_MOVED_PERM");
    STATUS_LINES.put(302, "HTTP_MOVED_TEMP");
    STATUS_LINES.put(303, "HTTP_SEE_OTHER");
    STATUS_LINES.put(304, "HTTP_NOT_MODIFIED");
    STATUS_LINES.put(305, "HTTP_USE_PROXY");
    STATUS_LINES.put(400, "HTTP_BAD_REQUEST");
    STATUS_LINES.put(401, "HTTP_UNAUTHORIZED");
    STATUS_LINES.put(402, "HTTP_PAYMENT_REQUIRED");
    STATUS_LINES.put(403, "HTTP_FORBIDDEN");
    STATUS_LINES.put(404, "HTTP_NOT_FOUND");
    STATUS_LINES.put(405, "HTTP_BAD_METHOD");
    STATUS_LINES.put(406, "HTTP_NOT_ACCEPTABLE");
    STATUS_LINES.put(407, "HTTP_PROXY_AUTH");
    STATUS_LINES.put(408, "HTTP_CLIENT_TIMEOUT");
    STATUS_LINES.put(409, "HTTP_CONFLICT");
    STATUS_LINES.put(410, "HTTP_GONE");
    STATUS_LINES.put(411, "HTTP_LENGTH_REQUIRED");
    STATUS_LINES.put(412, "HTTP_PRECON_FAILED");
    STATUS_LINES.put(413, "HTTP_ENTITY_TOO_LARGE");
    STATUS_LINES.put(414, "HTTP_REQ_TOO_LONG");
    STATUS_LINES.put(415, "HTTP_UNSUPPORTED_TYPE");
    STATUS_LINES.put(500, "HTTP_INTERNAL_ERROR");
    STATUS_LINES.put(501, "HTTP_NOT_IMPLEMENTED");
    STATUS_LINES.put(502, "HTTP_BAD_GATEWAY");
    STATUS_LINES.put(503, "HTTP_UNAVAILABLE");
    STATUS_LINES.put(504, "HTTP_GATEWAY_TIMEOUT");
    STATUS_LINES.put(505, "HTTP_VERSION");
  }

  private HttpStatus() {
  }

  @NonNull
  public static String getStatusLine(int statusCode) {
    synchronized (STATUS_LINES) {
      return statusCode + " " + STATUS_LINES.get(statusCode, HTTP_UNKNOWN);
    }
  }

}
