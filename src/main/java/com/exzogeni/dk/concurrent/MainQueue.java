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

package com.exzogeni.dk.concurrent;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Daniel Serdyukov
 */
public class MainQueue implements ThreadQueue {

  private final Handler mHandler = new Handler(Looper.getMainLooper());

  public static MainQueue get() {
    return Holder.INSTANCE;
  }

  @NonNull
  @Override
  public <V> Future<V> submit(@NonNull Callable<V> task) {
    final FutureTask<V> future = new FutureImpl<>(task);
    mHandler.post(future);
    return future;
  }

  @Override
  public void execute(@NonNull Runnable task) {
    mHandler.post(task);
  }

  private static final class Holder {
    public static final MainQueue INSTANCE = new MainQueue();
  }

  private final class FutureImpl<V> extends FutureTask<V> {

    public FutureImpl(Callable<V> callable) {
      super(callable);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      mHandler.removeCallbacks(this);
      return super.cancel(false);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
      throw new UnsupportedOperationException();
    }

    @Override
    public V get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException,
        TimeoutException {
      throw new UnsupportedOperationException();
    }

  }

}
