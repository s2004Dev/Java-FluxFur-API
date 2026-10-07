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

package lonter.jfa.api.sharding;

import lonter.jfa.internal.utils.Checks;

import java.util.concurrent.ExecutorService;
import java.util.function.IntFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called by {@link DefaultShardManager} when building a JFA instance.
 * <br>Every time a JFA instance is built, the manager will first call {@link #provide(int)} followed by
 * a call to {@link #shouldShutdownAutomatically(int)}.
 *
 * @param <T>
 *        The type of executor
 */
public interface ThreadPoolProvider<T extends ExecutorService> {
    /**
     * Provides an instance of the specified executor, or null
     *
     * @param  shardId
     *         The current shard id
     *
     * @return The Executor Service
     */
    @Nullable
    T provide(int shardId);

    /**
     * Whether the previously provided executor should be shutdown by {@link lonter.jfa.api.JFA#shutdown()}.
     *
     * @param  shardId
     *         The current shard id
     *
     * @return True, if the executor should be shutdown by JFA
     */
    default boolean shouldShutdownAutomatically(int shardId) {
        return false;
    }

    /**
     * Provider that initializes with a {@link DefaultShardManagerBuilder#setShardsTotal(int) shard_total}
     * and provides the same pool to share between shards.
     *
     * @param  init
     *         Function to initialize the shared pool, called with the shard total
     * @param  <T>
     *         The type of executor
     *
     * @return The lazy pool provider
     */
    @NotNull
    static <T extends ExecutorService> LazySharedProvider<T> lazy(@NotNull IntFunction<T> init) {
        Checks.notNull(init, "Initializer");
        return new LazySharedProvider<>(init);
    }

    final class LazySharedProvider<T extends ExecutorService> implements ThreadPoolProvider<T> {
        private final IntFunction<T> initializer;
        private T pool;

        LazySharedProvider(@NotNull IntFunction<T> initializer) {
            this.initializer = initializer;
        }

        /**
         * Called with the shard total to initialize the shared pool.
         *
         * <p>This also destroys the temporary pool created for fetching the recommended shard total.
         *
         * @param shardTotal
         *        The shard total
         */
        public synchronized void init(int shardTotal) {
            if (pool == null) {
                pool = initializer.apply(shardTotal);
            }
        }

        /**
         * Shuts down the shared pool and the temporary pool.
         */
        public synchronized void shutdown() {
            if (pool != null) {
                pool.shutdown();
                pool = null;
            }
        }

        /**
         * Provides the initialized pool or the temporary pool if not initialized yet.
         *
         * @param  shardId
         *         The current shard id
         *
         * @return The thread pool instance
         */
        @Nullable
        @Override
        public synchronized T provide(int shardId) {
            return pool;
        }
    }
}
