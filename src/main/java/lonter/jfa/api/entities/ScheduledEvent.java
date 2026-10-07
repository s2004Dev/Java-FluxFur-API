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

package lonter.jfa.api.entities;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.unions.GuildChannelUnion;
import lonter.jfa.api.managers.ScheduledEventManager;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.requests.restaction.pagination.PaginationAction;
import lonter.jfa.api.requests.restaction.pagination.ScheduledEventMembersPaginationAction;
import lonter.jfa.api.utils.FluxerAssets;
import lonter.jfa.api.utils.ImageFormat;
import lonter.jfa.api.utils.ImageProxy;

import java.time.OffsetDateTime;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class representing a {@link ScheduledEvent} (The events that show up under the events tab in the Official Fluxer Client).
 * These events should not be confused with {@link lonter.jfa.api.events Gateway Events},
 * which are fired by Fluxer whenever something interesting happens
 * (ie., a {@link lonter.jfa.api.events.message.MessageDeleteEvent MessageDeleteEvent} gets fired whenever a message gets deleted).
 */
public interface ScheduledEvent extends ISnowflake, Comparable<ScheduledEvent> {

    /**
     * Template for {@link #getJumpUrl()}. Args: .../guild_id/event_id
     */
    String JUMP_URL = "https://fluxer.com/events/%s/%s";

    /**
     * The maximum allowed length for an event's name.
     */
    int MAX_NAME_LENGTH = 100;

    /**
     * The maximum allowed length for an event's description.
     */
    int MAX_DESCRIPTION_LENGTH = 1000;

    /**
     * The maximum allowed length for an event's location.
     */
    int MAX_LOCATION_LENGTH = 100;

    /**
     * Template for {@link #getImageUrl()}
     *
     * @deprecated Replaced by {@link lonter.jfa.api.utils.FluxerAssets#scheduledEventCoverImage(ImageFormat, String, String)}
     */
    @Deprecated
    String IMAGE_URL = "https://cdn.fluxerapp.com/guild-events/%s/%s.%s";

    /**
     * The name of the event.
     *
     * @return The event's name
     */
    @NotNull
    String getName();

    /**
     * The description of the event.
     *
     * @return The description, or {@code null} if none is specified
     */
    @Nullable
    String getDescription();

    /**
     * The ID of the cover image of the event.
     *
     * @return The cover image ID, or {@code null} if none is specified.
     */
    @Nullable
    String getCoverImageId();

    /**
     * The cover image url of the event.
     * <p>Links to a potentially heavily compressed image. You can append a {@code size} query parameter to the URL if needed.
     *
     * @return The image url, or {@code null} if none is specified
     */
    @Nullable
    String getImageUrl();

    /**
     * The cover image url of the event.
     * <p>Links to a potentially heavily compressed image. You can append a {@code size} query parameter to the URL if needed.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The image url, or {@code null} if none is specified
     *
     * @see    FluxerAssets#scheduledEventCoverImage(ImageFormat, String, String)
     */
    @Nullable
    default String getCoverImageUrl(@NotNull ImageFormat format) {
        ImageProxy proxy = getCoverImage(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this events cover image.
     *
     * @return The {@link ImageProxy} for this events cover image or null if no image is defined
     *
     * @see    #getImageUrl()
     */
    @Nullable
    default ImageProxy getImage() {
        String imageUrl = getImageUrl();
        return imageUrl == null ? null : new ImageProxy(imageUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this events cover image.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The {@link ImageProxy} for this events cover image or null if no image is defined
     *
     * @see    #getCoverImageUrl(ImageFormat)
     * @see    FluxerAssets#scheduledEventCoverImage(ImageFormat, String, String)
     */
    @Nullable
    default ImageProxy getCoverImage(@NotNull ImageFormat format) {
        return FluxerAssets.scheduledEventCoverImage(format, getId(), getCoverImageId());
    }

    /**
     * The user who originally created the event.
     * <p> May return {@code null} if user has deleted their account, the {@link User} object is not cached
     * or the event was created before Fluxer started keeping track of event creators on October 21st, 2021.
     *
     * @return {@link User} object representing the event's creator or {@code null}.
     *
     * @see    #getCreatorId()
     * @see    #getCreatorIdLong()
     */
    @Nullable
    User getCreator();

    /**
     * The ID of the user who originally created this event.
     * <p> This method may return 0 if the event was created before Fluxer started keeping track of event creators on October 21st, 2021.
     *
     * @return The ID of the user who created this event, or 0 if no user is associated with creating this event.
     *
     * @see    #getCreatorId()
     * @see    #getCreator()
     */
    long getCreatorIdLong();

    /**
     * The ID of the user who originally created this event.
     * <br>This method may return {@code null} if the event was created before Fluxer started keeping track of event creators on October 21st, 2021.
     *
     * @return The Id of the user who created this event, or {@code null} if no user is associated with creating this event.
     *
     * @see    #getCreatorIdLong()
     * @see    #getCreator()
     */
    @Nullable
    default String getCreatorId() {
        return getCreatorIdLong() == 0 ? null : Long.toUnsignedString(getCreatorIdLong());
    }

    /**
     * The {@link Status status} of the scheduled event.
     *
     * @return The status, or {@link Status#UNKNOWN} if the status is unknown to JFA.
     */
    @NotNull
    Status getStatus();

    /**
     * The {@link Type type} of the scheduled event.
     *
     * @return The type, or {@link Type#UNKNOWN} if the type is unknown to JFA.
     */
    @NotNull
    Type getType();

    /**
     * The time the event is set to start at.
     *
     * @return The time the event is set to start at
     *
     * @see    #getEndTime()
     */
    @NotNull
    OffsetDateTime getStartTime();

    /**
     * The time the event is set to end at.
     * <br>The end time is only required for external events,
     * which are events that are not associated with a stage or voice channel.
     *
     * @return The time the event is set to end at. This can't be {@code null} for events of
     *         {@link Type#EXTERNAL}, but can be null for other types.
     *
     * @see    #getType()
     * @see    #getStartTime()
     */
    @Nullable
    OffsetDateTime getEndTime();

    /**
     * The guild channel the event is set to take place in.
     * <br>Note that this method is only applicable to events which are not of {@link Type#STAGE_INSTANCE} or {@link Type#VOICE}.
     *
     * @return The guild channel, or {@code null} if the guild channel was deleted
     *         or if the event is of {@link Type#EXTERNAL}
     *
     * @see    #getType()
     * @see    #getLocation()
     */
    @Nullable
    GuildChannelUnion getChannel();

    /**
     * The location the event is set to take place in.
     * This will return the channel id for {@link Type#STAGE_INSTANCE} and {@link Type#VOICE}.
     *
     * @return The channel id or the external location of the event
     *
     * @see    #getType()
     * @see    #getChannel()
     */
    @NotNull
    String getLocation();

    /**
     * Returns the jump-to URL of the event. Clicking this URL in the Fluxer client will open the event.
     *
     * @return A String representing the jump-to URL of the event.
     */
    @NotNull
    String getJumpUrl();

    /**
     * Deletes this Scheduled Event.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_SCHEDULED_EVENT UNKNOWN_SCHEDULED_EVENT}
     *     <br>If the the event was already deleted.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link lonter.jfa.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS} in the guild.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If we don't have the permission to {@link lonter.jfa.api.Permission#MANAGE_EVENTS MANAGE_EVENTS}
     *
     * @return {@link AuditableRestAction}
     */
    @NotNull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * A {@link PaginationAction PaginationAction} implementation
     * that allows to {@link Iterable iterate} over all {@link lonter.jfa.api.entities.Member Members} interested in this Event.
     *
     * <br>This iterates in ascending order by member id.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_SCHEDULED_EVENT}
     *     <br>If the the event was already deleted.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild or can't view the events channel (Location)</li>
     * </ul>
     *
     * @return {@link ScheduledEventMembersPaginationAction}
     */
    @NotNull
    @CheckReturnValue
    ScheduledEventMembersPaginationAction retrieveInterestedMembers();

    /**
     * The amount of users who are interested in attending the event.
     * <p>This method only returns the cached count, and may not be consistent with the live count. Fluxer may additionally not
     * provide an interested user count for some {@link ScheduledEvent} objects returned from the Guild's or JFA's
     * cache, and this method may return -1 as a result. However, event's retrieved using {@link Guild#retrieveScheduledEventById(long)}
     * will always contain an interested user count.
     *
     * @return The amount of users who are interested in attending the event
     *
     * @see    Guild#retrieveScheduledEventById(long)
     * @see    Guild#retrieveScheduledEventById(String)
     */
    int getInterestedUserCount();

    /**
     * The guild that this event was created in
     *
     * @return The guild
     */
    @NotNull
    Guild getGuild();

    /**
     * The JFA instance associated with this event object
     *
     * @return The JFA instance
     */
    @NotNull
    default JFA getJFA() {
        return getGuild().getJFA();
    }

    /**
     * The {@link ScheduledEventManager} for this event.
     * <br>In the ScheduledEventManager, you can modify all values and also start, end, or cancel events.
     * <br>You can modify multiple fields in one request by chaining setters before calling {@link lonter.jfa.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link lonter.jfa.api.Permission#MANAGE_EVENTS Permission.MANAGE_EVENTS}
     *
     * @return The ScheduledEventManager of this event
     */
    @NotNull
    @CheckReturnValue
    ScheduledEventManager getManager();

    /**
     * Compares two {@link ScheduledEvent} objects based on their scheduled start times.
     * <br>If two events are set to start at the same time, the comparison will be made based on their snowflake ID.
     *
     * @param  scheduledEvent
     *         The provided scheduled event
     *
     * @throws IllegalArgumentException
     *         If the provided scheduled event is {@code null}, from a different {@link Guild}, or is not a valid
     *         scheduled event provided by JFA.
     *
     * @return A negative number if the original event (which is the event that the {@link #compareTo(ScheduledEvent) compareTo}
     *         method is called upon) starts sooner than the provided event, or positive if it will start later than
     *         the provided event. If both events are set to start at the same time, then the result will be negative if the original
     *         event's snowflake ID is less than the provided event's ID, positive if it is greater than, or 0 if they
     *         are the same.
     *
     * @see    Comparable#compareTo(Object)
     * @see    #getStartTime()
     * @see    #getIdLong()
     */
    @Override
    int compareTo(@NotNull ScheduledEvent scheduledEvent);

    /**
     * Represents the status of a scheduled event.
     *
     * @see    ScheduledEvent#getStatus
     */
    enum Status {
        UNKNOWN(-1),
        SCHEDULED(1),
        ACTIVE(2),
        COMPLETED(3),
        CANCELED(4);

        private final int key;

        Status(int key) {
            this.key = key;
        }

        /**
         * The Fluxer id key for this Status.
         *
         * @return The id key for this Status
         */
        public int getKey() {
            return key;
        }

        /**
         * Used to retrieve a Status based on a Fluxer id key.
         *
         * @param  key
         *         The Fluxer id key representing the requested Status.
         *
         * @return The Status related to the provided key, or {@link #UNKNOWN Status.UNKNOWN} if the key is not recognized.
         */
        @NotNull
        public static Status fromKey(int key) {
            for (Status status : Status.values()) {
                if (status.getKey() == key) {
                    return status;
                }
            }

            return UNKNOWN;
        }
    }

    /**
     * Represents what type of event an event is, or where the event will be taking place at.
     */
    enum Type {
        /**
         * Unknown future types that may be added by Fluxer which aren't represented in JFA yet.
         */
        UNKNOWN(-1),
        /**
         * An event with it's own {@link lonter.jfa.api.entities.StageInstance StageInstance}
         */
        STAGE_INSTANCE(1),
        /**
         * An event inside a {@link lonter.jfa.api.entities.channel.concrete.VoiceChannel VoiceChannel}
         */
        VOICE(2),
        /**
         * An event held externally.
         */
        EXTERNAL(3);

        private final int key;

        Type(int key) {
            this.key = key;
        }

        /**
         * The Fluxer id key used to represent the scheduled event type.
         *
         * @return The id key used by fluxer for this scheduled event type.
         */
        public int getKey() {
            return key;
        }

        /**
         * Whether the event is scheduled to be held in a {@link GuildChannel}.
         *
         * @return True, if the event is scheduled to be held in a {@link GuildChannel}
         */
        public boolean isChannel() {
            return this == STAGE_INSTANCE || this == VOICE;
        }

        /**
         * Used to retrieve a Type based on a Fluxer id key.
         *
         * @param  key
         *         The Fluxer id key representing the requested Type.
         *
         * @return The Type related to the provided key, or {@link #UNKNOWN Type.UNKNOWN} if the key is not recognized.
         */
        @NotNull
        public static Type fromKey(int key) {
            for (Type type : Type.values()) {
                if (type.getKey() == key) {
                    return type;
                }
            }

            return UNKNOWN;
        }
    }
}
