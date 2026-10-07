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
import lonter.jfa.api.entities.Activity;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.events.user.update.GenericUserPresenceEvent;
import lonter.jfa.api.utils.cache.CacheFlag;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link lonter.jfa.api.entities.User User} has started an {@link Activity}
 * in a {@link Guild}.
 *
 * <p>This is fired for every {@link Guild} the user is part of. If the title of a stream
 * changes a start event is fired before an end event which will replace the activity.
 *
 * <p>The activities of the {@link Member} are updated before all the start/end events are fired.
 * This means you can check {@link Member#getActivities()} when handling this event and it will already
 * contain all new activities, even ones that have not yet fired the start event.
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
public class UserActivityStartEvent extends GenericUserEvent implements GenericUserPresenceEvent {
    private final Activity newActivity;
    private final Member member;

    public UserActivityStartEvent(
            @NotNull JFA api, long responseNumber, @NotNull Member member, @NotNull Activity newActivity) {
        super(api, responseNumber, member.getUser());
        this.newActivity = newActivity;
        this.member = member;
    }

    /**
     * The new activity
     *
     * @return The activity
     */
    public Activity getNewActivity() {
        return newActivity;
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
