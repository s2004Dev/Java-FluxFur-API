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

package lonter.jfa.api.events.guild.member.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.events.UpdateEvent;
import lonter.jfa.api.events.guild.member.GenericGuildMemberEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a {@link lonter.jfa.api.entities.Guild Guild} member event is fired.
 * <br>Every GuildMemberUpdateEvent is an instance of this event and can be casted.
 *
 * <p>Can be used to detect any GuildMemberUpdateEvent.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, these events require the {@link lonter.jfa.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Fluxer does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public abstract class GenericGuildMemberUpdateEvent<T> extends GenericGuildMemberEvent
        implements UpdateEvent<Member, T> {
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericGuildMemberUpdateEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull Member member,
            @Nullable T previous,
            @Nullable T next,
            @NotNull String identifier) {
        super(api, responseNumber, member);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @NotNull
    @Override
    public String getPropertyIdentifier() {
        return identifier;
    }

    @NotNull
    @Override
    public Member getEntity() {
        return getMember();
    }

    @Nullable
    @Override
    public T getOldValue() {
        return previous;
    }

    @Nullable
    @Override
    public T getNewValue() {
        return next;
    }
}
