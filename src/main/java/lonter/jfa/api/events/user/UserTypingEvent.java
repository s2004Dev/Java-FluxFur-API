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

package lonter.jfa.api.events.user;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a {@link lonter.jfa.api.entities.User User} started typing. (Similar to the typing indicator in the Fluxer client)
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_MESSAGE_TYPING GUILD_MESSAGE_TYPING} intent to be enabled to fire
 * for guild channels, and {@link lonter.jfa.api.requests.GatewayIntent#DIRECT_MESSAGE_TYPING DIRECT_MESSAGE_TYPING} to fire for private channels.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable these by default!
 *
 * <p>Can be used to retrieve the User who started typing and when and in which MessageChannel they started typing.
 */
public class UserTypingEvent extends GenericUserEvent {
    private final Member member;
    private final MessageChannel channel;
    private final OffsetDateTime timestamp;

    public UserTypingEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull User user,
            @NotNull MessageChannel channel,
            @NotNull OffsetDateTime timestamp,
            @Nullable Member member) {
        super(api, responseNumber, user);
        this.member = member;
        this.channel = channel;
        this.timestamp = timestamp;
    }

    /**
     * The time when the user started typing
     *
     * @return The time when the typing started
     */
    @NotNull
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * The channel where the typing was started
     *
     * @return The channel
     */
    @NotNull
    public MessageChannelUnion getChannel() {
        return (MessageChannelUnion) channel;
    }

    /**
     * Whether the user started typing in a channel of the specified type.
     *
     * @param  type
     *         {@link ChannelType ChannelType}
     *
     * @return True, if the user started typing in a channel of the specified type
     */
    public boolean isFromType(@NotNull ChannelType type) {
        return channel.getType() == type;
    }

    /**
     * The {@link ChannelType ChannelType}
     *
     * @return The {@link ChannelType ChannelType}
     */
    @NotNull
    public ChannelType getType() {
        return channel.getType();
    }

    /**
     * {@link lonter.jfa.api.entities.Guild Guild} in which this users started typing,
     * or {@code null} if this was not in a Guild.
     *
     * @return Possibly-null {@link lonter.jfa.api.entities.Guild Guild}
     */
    @Nullable
    public Guild getGuild() {
        return getType().isGuild() ? member.getGuild() : null;
    }

    /**
     * {@link lonter.jfa.api.entities.Member Member} instance for the User, or null if this was not in a Guild.
     *
     * @return Possibly-null {@link lonter.jfa.api.entities.Member Member}
     */
    @Nullable
    public Member getMember() {
        return member;
    }
}
