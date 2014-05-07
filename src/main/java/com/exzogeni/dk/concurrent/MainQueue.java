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
class MainQueue extends AsyncQueue {

  private final Handler mHandler = new Handler(Looper.getMainLooper());

  public static MainQueue getInstance() {
    return Holder.INSTANCE;
  }

  @Override
  public <V> Future<V> submit(@NonNull Callable<V> task) {
    final FutureTask<V> futureTask = new FutureTask<>(task);
    final Future<V> future = new MainFuture<>(futureTask);
    mHandler.post(futureTask);
    return future;
  }

  private static final class Holder {
    public static final MainQueue INSTANCE = new MainQueue();
  }

  private final class MainFuture<V> implements Future<V> {

    private final FutureTask<V> mTask;

    private MainFuture(FutureTask<V> task) {
      mTask = task;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      mHandler.removeCallbacks(mTask);
      return mTask.cancel(false);
    }

    @Override
    public boolean isCancelled() {
      return mTask.isCancelled();
    }

    @Override
    public boolean isDone() {
      return mTask.isDone();
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
