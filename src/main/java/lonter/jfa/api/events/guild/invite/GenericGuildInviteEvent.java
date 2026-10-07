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

package lonter.jfa.api.events.guild.invite;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Invite;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.unions.GuildChannelUnion;
import lonter.jfa.api.events.guild.GenericGuildEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that an {@link Invite} was created or deleted in a {@link Guild}.
 * <br>Every GuildInviteEvent is derived from this event and can be casted.
 *
 * <p>Can be used to detect any GuildInviteEvent.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_INVITES GUILD_INVITES} intent to be enabled.
 * <br>These events will only fire for invite events that occur in channels where you can {@link lonter.jfa.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}.
 */
public class GenericGuildInviteEvent extends GenericGuildEvent {
    private final String code;
    private final GuildChannel channel;

    public GenericGuildInviteEvent(
            @NotNull JFA api, long responseNumber, @NotNull String code, @NotNull GuildChannel channel) {
        super(api, responseNumber, channel.getGuild());
        this.code = code;
        this.channel = channel;
    }

    /**
     * The invite code.
     * <br>This can be converted to a url with {@code fluxer.gg/<code>}.
     *
     * @return The invite code
     */
    @NotNull
    public String getCode() {
        return code;
    }

    /**
     * The invite url.
     * <br>This uses the {@code https://fluxer.gg/<code>} format.
     *
     * @return The invite url
     */
    @NotNull
    public String getUrl() {
        return "https://fluxer.gg/" + code;
    }

    /**
     * The {@link GuildChannel} this invite points to.
     *
     * @return {@link GuildChannel}
     */
    @NotNull
    public GuildChannelUnion getChannel() {
        return (GuildChannelUnion) channel;
    }

    /**
     * The {@link ChannelType} for of the {@link #getChannel() channel} this invite points to.
     *
     * @return {@link ChannelType}
     */
    @NotNull
    public ChannelType getChannelType() {
        return channel.getType();
    }
}
