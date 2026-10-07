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

package lonter.jfa.api.events.guild;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.User;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link lonter.jfa.api.entities.User User} was banned from a {@link lonter.jfa.api.entities.Guild Guild}.
 *
 * <p>Can be used to retrieve the user who was banned (if available) and the triggering guild.
 * <br><b>Note</b>: This does not directly indicate that a Member is removed from the Guild!
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_MODERATION GUILD_MODERATION} intent to be enabled.
 *
 * @see lonter.jfa.api.events.guild.member.GuildMemberRemoveEvent
 */
public class GuildBanEvent extends GenericGuildEvent {
    private final User user;

    public GuildBanEvent(@NotNull JFA api, long responseNumber, @NotNull Guild guild, @NotNull User user) {
        super(api, responseNumber, guild);
        this.user = user;
    }

    /**
     * The banned {@link lonter.jfa.api.entities.User User}
     *
     * @return The banned user
     */
    @NotNull
    public User getUser() {
        return user;
    }
}
