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

package lonter.jfa.api.managers;

import lonter.jfa.api.entities.StageInstance;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manager providing functionality to update one or more fields for a {@link lonter.jfa.api.entities.StageInstance StageInstance}.
 *
 * <p><b>Example</b>
 * {@snippet lang="java":
 * manager.setTopic("LMAO JOIN FOR FREE NITRO")
 *        .setPrivacyLevel(PrivacyLevel.PUBLIC)
 *        .queue();
 * manager.reset(ChannelManager.TOPIC | ChannelManager.PRIVACY_LEVEL)
 *        .setTopic("Talent Show | WINNER GETS FREE NITRO")
 *        .setPrivacyLevel(PrivacyLevel.GUILD_ONLY)
 *        .queue();
 * }
 *
 * @see lonter.jfa.api.entities.StageInstance#getManager()
 */
public interface StageInstanceManager extends Manager<StageInstanceManager> {
    /** Used to reset the topic field */
    long TOPIC = 1;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(ChannelManager.TOPIC | ChannelManager.PRIVACY_LEVEL);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #TOPIC}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return StageInstanceManager for chaining convenience
     */
    @NotNull
    @Override
    @CheckReturnValue
    StageInstanceManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(ChannelManager.TOPIC, ChannelManager.PRIVACY_LEVEL);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #TOPIC}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return StageInstanceManager for chaining convenience
     */
    @NotNull
    @Override
    @CheckReturnValue
    StageInstanceManager reset(@NotNull long... fields);

    /**
     * The associated {@link StageInstance}
     *
     * @return The {@link StageInstance}
     */
    @NotNull
    StageInstance getStageInstance();

    /**
     * Sets the topic for this stage instance.
     * <br>This shows up in stage discovery and in the stage view.
     *
     * @param  topic
     *         The topic or null to reset, must be 1-120 characters long
     *
     * @throws IllegalArgumentException
     *         If the topic is longer than 120 characters
     *
     * @return StageInstanceManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    StageInstanceManager setTopic(@Nullable String topic);
}
