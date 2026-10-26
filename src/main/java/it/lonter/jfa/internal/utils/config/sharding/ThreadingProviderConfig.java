/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JFA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.lonter.jfa.internal.utils.config.sharding;

import it.lonter.jfa.api.sharding.ThreadPoolProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ThreadingProviderConfig(ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitSchedulerProvider,
                                      ThreadPoolProvider<? extends ExecutorService> rateLimitElasticProvider,
                                      ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider,
                                      ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider,
                                      ThreadPoolProvider<? extends ExecutorService> eventPoolProvider,
                                      ThreadPoolProvider<? extends ScheduledExecutorService> audioPoolProvider,
                                      ThreadFactory threadFactory) {
    public ThreadingProviderConfig(
      @Nullable ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitSchedulerProvider,
      @Nullable ThreadPoolProvider<? extends ExecutorService> rateLimitElasticProvider,
      @Nullable ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider,
      @Nullable ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider,
      @Nullable ThreadPoolProvider<? extends ExecutorService> eventPoolProvider,
      @Nullable ThreadPoolProvider<? extends ScheduledExecutorService> audioPoolProvider,
      @Nullable ThreadFactory threadFactory) {
        this.rateLimitSchedulerProvider = rateLimitSchedulerProvider;
        this.rateLimitElasticProvider = rateLimitElasticProvider;
        this.gatewayPoolProvider = gatewayPoolProvider;
        this.callbackPoolProvider = callbackPoolProvider;
        this.eventPoolProvider = eventPoolProvider;
        this.audioPoolProvider = audioPoolProvider;
        this.threadFactory = threadFactory;
    }

    @Override
    @Nullable
    public ThreadFactory threadFactory() {
        return threadFactory;
    }

    private void init(ThreadPoolProvider<?> provider, int shardTotal) {
        if (provider instanceof ThreadPoolProvider.LazySharedProvider) {
            ((ThreadPoolProvider.LazySharedProvider<?>) provider).init(shardTotal);
        }
    }

    private void shutdown(ThreadPoolProvider<?> provider) {
        if (provider instanceof ThreadPoolProvider.LazySharedProvider) {
            ((ThreadPoolProvider.LazySharedProvider<?>) provider).shutdown();
        }
    }

    public void init(int shardTotal) {
        init(rateLimitSchedulerProvider, shardTotal);
        init(rateLimitElasticProvider, shardTotal);
        init(gatewayPoolProvider, shardTotal);
        init(callbackPoolProvider, shardTotal);
        init(eventPoolProvider, shardTotal);
        init(audioPoolProvider, shardTotal);
    }

    public void shutdown() {
        shutdown(rateLimitSchedulerProvider);
        shutdown(rateLimitElasticProvider);
        shutdown(gatewayPoolProvider);
        shutdown(callbackPoolProvider);
        shutdown(eventPoolProvider);
        shutdown(audioPoolProvider);
    }

    @Override
    @Nullable
    public ThreadPoolProvider<? extends ScheduledExecutorService> rateLimitSchedulerProvider() {
        return rateLimitSchedulerProvider;
    }

    @Override
    @Nullable
    public ThreadPoolProvider<? extends ExecutorService> rateLimitElasticProvider() {
        return rateLimitElasticProvider;
    }

    @Override
    @Nullable
    public ThreadPoolProvider<? extends ScheduledExecutorService> gatewayPoolProvider() {
        return gatewayPoolProvider;
    }

    @Override
    @Nullable
    public ThreadPoolProvider<? extends ExecutorService> callbackPoolProvider() {
        return callbackPoolProvider;
    }

    @Override
    @Nullable
    public ThreadPoolProvider<? extends ExecutorService> eventPoolProvider() {
        return eventPoolProvider;
    }

    @Override
    @Nullable
    public ThreadPoolProvider<? extends ScheduledExecutorService> audioPoolProvider() {
        return audioPoolProvider;
    }

    @NotNull
    public static ThreadingProviderConfig getDefault() {
        return new ThreadingProviderConfig(null, null, null, null, null, null, null);
    }
}
