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

package lonter.jfa.api.requests.restaction;

import lonter.jfa.api.entities.StageInstance;
import lonter.jfa.api.requests.RestAction;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specialized {@link RestAction} used to create a {@link StageInstance}
 *
 * @see lonter.jfa.api.entities.channel.concrete.StageChannel#createStageInstance(String)
 */
public interface StageInstanceAction extends RestAction<StageInstance> {
    @NotNull
    @Override
    @CheckReturnValue
    StageInstanceAction setCheck(@Nullable BooleanSupplier checks);

    @NotNull
    @Override
    @CheckReturnValue
    StageInstanceAction timeout(long timeout, @NotNull TimeUnit unit);

    @NotNull
    @Override
    @CheckReturnValue
    StageInstanceAction deadline(long timestamp);

    /**
     * Sets the topic for the stage instance.
     * <br>This shows up in stage discovery and in the stage view.
     *
     * @param  topic
     *         The topic, must be 1-120 characters long
     *
     * @throws IllegalArgumentException
     *         If the topic is null, empty, or longer than 120 characters
     *
     * @return The StageInstanceAction for chaining
     */
    @NotNull
    @CheckReturnValue
    StageInstanceAction setTopic(@NotNull String topic);
}
