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

package lonter.jfa.api.entities;

import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.managers.StageInstanceManager;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * A Stage Instance holds information about a live stage.
 *
 * <p>This instance indicates an active stage channel with speakers, usually to host events such as presentations or meetings.
 */
public interface StageInstance extends ISnowflake {
    /**
     * The {@link Guild} this stage instance is in
     *
     * @return The {@link Guild}
     */
    @NotNull
    Guild getGuild();

    /**
     * The {@link lonter.jfa.api.entities.channel.concrete.StageChannel} for this stage instance
     *
     * @return The {@link lonter.jfa.api.entities.channel.concrete.StageChannel}
     */
    @NotNull
    StageChannel getChannel();

    /**
     * The topic of this stage instance
     *
     * @return The topic
     */
    @NotNull
    String getTopic();

    /**
     * The {@link PrivacyLevel} of this stage instance
     *
     * @return The {@link PrivacyLevel}
     */
    @NotNull
    PrivacyLevel getPrivacyLevel();

    /**
     * All current speakers of this stage instance.
     *
     * <p>A member is considered a <b>speaker</b> when they are currently connected to the stage channel
     * and their voice state is not {@link GuildVoiceState#isSuppressed() suppressed}.
     * When a member is not a speaker, they are part of the {@link #getAudience() audience}.
     *
     * <p>Only {@link StageChannel#isModerator(Member) stage moderators} can promote or invite speakers.
     * A stage moderator can move between speaker and audience at any time.
     *
     * @return Immutable {@link List} of {@link Member Members} which can speak in this stage instance
     */
    @NotNull
    @Unmodifiable
    default List<Member> getSpeakers() {
        return getChannel().getMembers().stream()
                // voice states should not be null
                // since getMembers() checks only for connected members in the channel
                .filter(member -> !member.getVoiceState().isSuppressed())
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * All current audience members of this stage instance.
     *
     * <p>A member is considered part of the <b>audience</b> when they are currently connected to the stage channel
     * and their voice state is {@link GuildVoiceState#isSuppressed() suppressed}.
     * When a member is not part of the audience, they are considered a {@link #getSpeakers() speaker}.
     *
     * <p>Only {@link StageChannel#isModerator(Member) stage moderators} can promote or invite speakers.
     * A stage moderator can move between speaker and audience at any time.
     *
     * @return Immutable {@link List} of {@link Member Members} which cannot speak in this stage instance
     */
    @NotNull
    @Unmodifiable
    default List<Member> getAudience() {
        return getChannel().getMembers().stream()
                // voice states should not be null
                // since getMembers() checks only for connected members in the channel
                .filter(member -> member.getVoiceState().isSuppressed())
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Deletes this stage instance
     *
     * <p>Possible {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_STAGE_INSTANCE UNKNOWN_STAGE_INSTANCE}
     *     <br>If this stage instance is already deleted</li>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel was deleted</li>
     * </ul>
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the self member is not a {@link StageChannel#isModerator(Member) stage moderator}
     *
     * @return {@link RestAction}
     */
    @NotNull
    @CheckReturnValue
    RestAction<Void> delete();

    /**
     * The {@link StageInstanceManager} used to update this stage instance.
     * <p>This can be used to update multiple fields such as topic and privacy level in one request
     *
     * <p>If this stage instance is already deleted, this will fail with {@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_STAGE_INSTANCE ErrorResponse.UNKNOWN_STAGE_INSTANCE}.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the self member is not a {@link StageChannel#isModerator(Member) stage moderator}
     *
     * @return The {@link StageInstanceManager}
     */
    @NotNull
    @CheckReturnValue
    StageInstanceManager getManager();

    /**
     * The privacy level for a stage instance.
     *
     * <p>This indicates from where people can join the stage instance.
     */
    enum PrivacyLevel {
        /** Placeholder for future privacy levels, indicates that this version of JFA does not support this privacy level yet */
        UNKNOWN(-1),
        /** This stage instance can only be accessed by guild members */
        GUILD_ONLY(2);

        private final int key;

        PrivacyLevel(int key) {
            this.key = key;
        }

        /**
         * The raw API key for this privacy level
         *
         * @return The raw API value or {@code -1} if this is {@link #UNKNOWN}
         */
        public int getKey() {
            return key;
        }

        /**
         * Converts the raw API key into the respective enum value
         *
         * @param  key
         *         The API key
         *
         * @return The enum value or {@link #UNKNOWN}
         */
        @NotNull
        public static PrivacyLevel fromKey(int key) {
            for (PrivacyLevel level : values()) {
                if (level.key == key) {
                    return level;
                }
            }
            return UNKNOWN;
        }
    }
}
