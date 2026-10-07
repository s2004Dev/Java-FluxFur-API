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

package lonter.jfa.api.utils.cache;

import lonter.jfa.api.entities.ISnowflake;
import lonter.jfa.api.utils.MiscUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link lonter.jfa.api.utils.cache.CacheView CacheView} implementation
 * specifically to view {@link lonter.jfa.api.entities.ISnowflake ISnowflake} implementations.
 *
 * @see CacheView CacheView for details on Efficient Memory Usage
 */
public interface SnowflakeCacheView<T extends ISnowflake> extends CacheView<T> {
    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  id
     *         The ID of the entity
     *
     * @return Possibly-null entity for the specified ID
     */
    @Nullable
    T getElementById(long id);

    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  id
     *         The ID of the entity
     *
     * @throws java.lang.NumberFormatException
     *         If the provided String is {@code null} or
     *         cannot be resolved to an unsigned long id
     *
     * @return Possibly-null entity for the specified ID
     */
    @Nullable
    default T getElementById(@NotNull String id) {
        return getElementById(MiscUtil.parseSnowflake(id));
    }
}
