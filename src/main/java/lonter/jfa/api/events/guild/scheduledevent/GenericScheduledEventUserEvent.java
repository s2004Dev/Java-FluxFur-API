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

package lonter.jfa.api.events.guild.scheduledevent;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.ScheduledEvent;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.restaction.CacheRestAction;
import lonter.jfa.api.utils.cache.CacheFlag;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a {@link User User} has subscribed or unsubscribed to a {@link ScheduledEvent ScheduledEvent}.
 *
 * <p>Can be used to detect when someone has indicated that they have subscribed or unsubscribed to an event and also retrieve their
 * {@link User User} object as well as the {@link ScheduledEvent}.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 */
public abstract class GenericScheduledEventUserEvent extends GenericScheduledEventGatewayEvent {
    private final long userId;

    public GenericScheduledEventUserEvent(
            @NotNull JFA api, long responseNumber, @NotNull ScheduledEvent scheduledEvent, long userId) {
        super(api, responseNumber, scheduledEvent);
        this.userId = userId;
    }
    /**
     * The id of the user that subscribed or unsubscribed to the {@link ScheduledEvent ScheduledEvent}.
     *
     * @return The long user id
     */
    public long getUserIdLong() {
        return userId;
    }

    /**
     * The id of the user that subscribed or unsubscribed to the {@link ScheduledEvent ScheduledEvent}.
     *
     * @return The string user id
     */
    @NotNull
    public String getUserId() {
        return Long.toUnsignedString(userId);
    }

    /**
     * The {@link User User} who subscribed or unsubscribed to the {@link ScheduledEvent ScheduledEvent}.
     * <br>This might be missing if the user was not cached.
     * Use {@link #retrieveUser()} to load the user.
     *
     * @return The added user or null if this information is missing
     */
    @Nullable
    public User getUser() {
        return api.getUserById(userId);
    }

    /**
     * The {@link Member Member} instance for the user
     * or {@code null} if the user is not in this guild.
     * <br>This will also be {@code null} if the member is not available in the cache.
     * Use {@link #retrieveMember()} to load the member.
     *
     * @return Member of the added user or null if they are no longer member of this guild
     */
    @Nullable
    public Member getMember() {
        return guild.getMemberById(userId);
    }

    /**
     * Retrieves the {@link User} that subscribed or unsubscribed to the {@link ScheduledEvent ScheduledEvent}.
     * <br>If a user is known, this will return {@link #getUser()}.
     *
     * @return {@link RestAction} - Type: {@link User}
     */
    @NotNull
    @CheckReturnValue
    public CacheRestAction<User> retrieveUser() {
        return getJFA().retrieveUserById(getUserIdLong());
    }

    /**
     * Retrieves the {@link Member} that subscribed or unsubscribed to the {@link ScheduledEvent ScheduledEvent}.
     * <br>If a member is known, this will return {@link #getMember()}.
     *
     * @return {@link RestAction} - Type: {@link Member}
     */
    @NotNull
    @CheckReturnValue
    public CacheRestAction<Member> retrieveMember() {
        return getGuild().retrieveMemberById(getUserIdLong());
    }
}
