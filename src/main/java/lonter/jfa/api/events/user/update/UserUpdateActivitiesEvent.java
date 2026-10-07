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
import lonter.jfa.api.utils.cache.CacheFlag;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that the activities of a guild member changed.
 *
 * <p>This is fired after a sequence of {@link lonter.jfa.api.events.user.UserActivityStartEvent UserActivityStartEvents} and {@link lonter.jfa.api.events.user.UserActivityEndEvent UserActivityEndEvents}
 * are fired and can be used to handle the resulting list of activities for the member.
 *
 * <p>Identifier: {@code activities}
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
 *
 * <p>This also requires {@link lonter.jfa.api.utils.cache.CacheFlag#ACTIVITY CacheFlag.ACTIVITY} to be enabled.
 * You can enable the cache flag with {@link lonter.jfa.api.JFABuilder#enableCache(CacheFlag, CacheFlag...) enableCache(CacheFlag.ACTIVITY)}.
 */
public class UserUpdateActivitiesEvent extends GenericUserUpdateEvent<List<Activity>>
        implements GenericUserPresenceEvent {
    public static final String IDENTIFIER = "activities";
    private final Member member;

    public UserUpdateActivitiesEvent(
            @NotNull JFA api, long responseNumber, @NotNull Member member, @Nullable List<Activity> previous) {
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
}
