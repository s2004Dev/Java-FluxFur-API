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

package lonter.jfa.api.events.channel;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.unions.ChannelUnion;
import lonter.jfa.api.events.Event;

import org.jetbrains.annotations.NotNull;

/**
 * Top-level channel event type
 * <br>All channel events JFA fires are derived from this class.
 *
 * <p>Can be used to check if an Object is a JFA event in {@link lonter.jfa.api.hooks.EventListener EventListener} implementations to distinguish what event is being fired.
 * <br>Adapter implementation: {@link lonter.jfa.api.hooks.ListenerAdapter ListenerAdapter}
 */
public class GenericChannelEvent extends Event {
    protected final Channel channel;

    public GenericChannelEvent(@NotNull JFA api, long responseNumber, Channel channel) {
        super(api, responseNumber);

        this.channel = channel;
    }

    /**
     * Whether this channel event happened in a {@link lonter.jfa.api.entities.Guild Guild}.
     * <br>If this is {@code false} then {@link #getGuild()} will throw an {@link java.lang.IllegalStateException}.
     *
     * @return True, if {@link #getChannelType()}.{@link ChannelType#isGuild() isGuild()} is true.
     */
    public boolean isFromGuild() {
        return getChannelType().isGuild();
    }

    /**
     * The {@link ChannelType} of the channel the event was fired from.
     *
     * @return The {@link ChannelType} of the channel the event was fired from.
     */
    @NotNull
    public ChannelType getChannelType() {
        return this.channel.getType();
    }

    /**
     * Used to determine if this event was received from a {@link Channel}
     * of the {@link lonter.jfa.api.entities.channel.ChannelType ChannelType} specified.
     *
     * <p>Useful for restricting functionality to a certain type of channels.
     *
     * @param  type
     *         The {@link ChannelType ChannelType} to check against.
     *
     * @return True if the {@link lonter.jfa.api.entities.channel.ChannelType ChannelType} which this message was received
     *         from is the same as the one specified by {@code type}.
     */
    public boolean isFromType(@NotNull ChannelType type) {
        return getChannelType() == type;
    }

    /**
     * The {@link Channel} the event was fired from.
     *
     * @return The {@link ChannelType} of the channel the event was fired from.
     */
    @NotNull
    public ChannelUnion getChannel() {
        return (ChannelUnion) this.channel;
    }

    /**
     * The {@link lonter.jfa.api.entities.Guild Guild} in which this channel event happened.
     * <br>If this channel event was not received in a {@link lonter.jfa.api.entities.channel.middleman.GuildChannel GuildChannel},
     * this will throw an {@link java.lang.IllegalStateException}.
     *
     * @throws java.lang.IllegalStateException
     *         If this channel event did not happen in a {@link lonter.jfa.api.entities.channel.middleman.GuildChannel}.
     *
     * @return The Guild in which this channel event happened
     *
     * @see    #isFromType(ChannelType)
     * @see    #getChannelType()
     */
    @NotNull
    public Guild getGuild() {
        if (!isFromGuild()) {
            throw new IllegalStateException("This channel event did not happen in a guild");
        }
        return ((GuildChannel) channel).getGuild();
    }
}
