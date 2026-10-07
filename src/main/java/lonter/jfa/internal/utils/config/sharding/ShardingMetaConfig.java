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

package lonter.jfa.internal.utils.config.sharding;

import lonter.jfa.api.GatewayEncoding;
import lonter.jfa.api.utils.Compression;
import lonter.jfa.api.utils.cache.CacheFlag;
import lonter.jfa.internal.utils.config.MetaConfig;
import lonter.jfa.internal.utils.config.flags.ConfigFlag;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;
import java.util.function.IntFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShardingMetaConfig extends MetaConfig {
    private static final ShardingMetaConfig defaultConfig =
            new ShardingMetaConfig(2048, null, null, ConfigFlag.getDefault(), Compression.ZLIB, GatewayEncoding.JSON);
    private final Compression compression;
    private final GatewayEncoding encoding;
    private final IntFunction<? extends ConcurrentMap<String, String>> contextProvider;

    public ShardingMetaConfig(
            int maxBufferSize,
            @Nullable IntFunction<? extends ConcurrentMap<String, String>> contextProvider,
            @Nullable EnumSet<CacheFlag> cacheFlags,
            EnumSet<ConfigFlag> flags,
            Compression compression,
            GatewayEncoding encoding) {
        super(maxBufferSize, null, cacheFlags, flags);

        this.compression = compression;
        this.contextProvider = contextProvider;
        this.encoding = encoding;
    }

    @Nullable
    public ConcurrentMap<String, String> getContextMap(int shardId) {
        return contextProvider == null ? null : contextProvider.apply(shardId);
    }

    public Compression getCompression() {
        return compression;
    }

    public GatewayEncoding getEncoding() {
        return encoding;
    }

    @Nullable
    public IntFunction<? extends ConcurrentMap<String, String>> getContextProvider() {
        return contextProvider;
    }

    @NotNull
    public static ShardingMetaConfig getDefault() {
        return defaultConfig;
    }
}
