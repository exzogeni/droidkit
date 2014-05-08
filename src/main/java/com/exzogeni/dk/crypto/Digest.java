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

package com.exzogeni.dk.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Serdyukov
 */
public final class Digest {

  private static final String MD5 = "MD5";

  private static final String SHA1 = "SHA-1";

  private static final String SHA256 = "SHA-256";

  private static final Map<String, Digest> INSTANCE = new ConcurrentHashMap<>();

  private final String mAlgorithm;

  private Digest(String algorithm) {
    mAlgorithm = algorithm;
  }

  public static byte[] md5(byte[] data) throws NoSuchAlgorithmException {
    return getInstance(MD5).hash(data);
  }

  public static byte[] sha1(byte[] data) throws NoSuchAlgorithmException {
    return getInstance(SHA1).hash(data);
  }

  public static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
    return getInstance(SHA256).hash(data);
  }

  public static Digest getInstance() {
    return getInstance(SHA1);
  }

  public static Digest getInstance(String algorithm) {
    Digest hash = INSTANCE.get(algorithm);
    if (hash == null) {
      hash = new Digest(algorithm);
      INSTANCE.put(algorithm, hash);
    }
    return hash;
  }

  public byte[] hash(byte[] data) throws NoSuchAlgorithmException {
    final MessageDigest hash = MessageDigest.getInstance(mAlgorithm);
    hash.update(data);
    return hash.digest();
  }

  public String hash(String data) {
    try {
      return Hex.toHexString(hash(data.getBytes()));
    } catch (NoSuchAlgorithmException e) {
      return Hex.toHexString(data.getBytes()).substring(0, 40);
    }
  }

  public String hashToHexString(byte[] data) throws NoSuchAlgorithmException {
    return Hex.toHexString(hash(data));
  }

}
