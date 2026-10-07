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

package lonter.jfa.api.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Compression algorithms that can be used with JFA.
 *
 * @see lonter.jfa.api.JFABuilder#setCompression(Compression)
 * @see lonter.jfa.api.sharding.DefaultShardManagerBuilder#setCompression(Compression)
 */
public enum Compression {
    /** Don't use any compression */
    NONE(""),
    /** Use ZLIB transport compression */
    ZLIB("zlib-stream");

    private final String key;

    Compression(String key) {
        this.key = key;
    }

    /**
     * The key used for the gateway query to enable this compression
     *
     * @return The query key
     */
    @NotNull
    public String getKey() {
        return key;
    }
}
