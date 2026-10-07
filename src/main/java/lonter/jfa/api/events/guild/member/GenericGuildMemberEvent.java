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

package lonter.jfa.api.events.guild.member;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.events.guild.GenericGuildEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link lonter.jfa.api.entities.Guild Guild} member event is fired.
 * <br>Every GuildMemberEvent is an instance of this event and can be casted.
 *
 * <p>Most of these events require the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Can be used to detect any GuildMemberEvent.
 */
public abstract class GenericGuildMemberEvent extends GenericGuildEvent {
    private final Member member;

    public GenericGuildMemberEvent(@NotNull JFA api, long responseNumber, @NotNull Member member) {
        super(api, responseNumber, member.getGuild());
        this.member = member;
    }

    /**
     * The {@link lonter.jfa.api.entities.User User} instance
     * <br>Shortcut for {@code getMember().getUser()}
     *
     * @return The User instance
     */
    @NotNull
    public User getUser() {
        return getMember().getUser();
    }

    /**
     * The {@link lonter.jfa.api.entities.Member Member} instance
     *
     * @return The Member instance
     */
    @NotNull
    public Member getMember() {
        return member;
    }
}
