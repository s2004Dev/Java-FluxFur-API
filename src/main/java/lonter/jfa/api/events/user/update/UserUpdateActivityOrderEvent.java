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

package lonter.jfa.api.events.user.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Activity;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the {@link lonter.jfa.api.entities.Activity Activity} order of a {@link lonter.jfa.api.entities.User User} changes.
 * <br>As with any presence updates this happened for a {@link lonter.jfa.api.entities.Member Member} in a Guild!
 * <p>Can be used to retrieve the User who changed their Activities and their previous Activities.
 *
 * <p>Identifier: {@code activity_order}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link lonter.jfa.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Fluxer does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateActivityOrderEvent extends GenericUserUpdateEvent<List<Activity>>
        implements GenericUserPresenceEvent {
    public static final String IDENTIFIER = "activity_order";

    private final Member member;

    public UserUpdateActivityOrderEvent(
            @NotNull JFA api, long responseNumber, @NotNull List<Activity> previous, @NotNull Member member) {
        super(api, responseNumber, member.getUser(), previous, member.getActivities(), IDENTIFIER);
        this.member = member;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return member.getGuild();
    }

    @NotNull
    @Override
    public Member getMember() {
        return member;
    }

    @NotNull
    @Override
    public List<Activity> getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    public List<Activity> getNewValue() {
        return super.getNewValue();
    }
}
