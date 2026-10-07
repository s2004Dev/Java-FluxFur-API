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

package lonter.jfa.api.managers.channel.concrete;

import lonter.jfa.api.entities.channel.concrete.ForumChannel;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.entities.channel.forums.ForumTagSnowflake;
import lonter.jfa.api.managers.channel.ChannelManager;
import lonter.jfa.api.managers.channel.attribute.ISlowmodeChannelManager;
import lonter.jfa.internal.utils.Checks;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Manager providing functionality common for all {@link ThreadChannel ThreadChannels}.
 *
 * <p><b>Example</b>
 * {@snippet lang="java":
 * manager.setSlowmode(10)
 *        .setArchived(false)
 *        .queue();
 * manager.reset(ChannelManager.SLOWMODE | ChannelManager.NAME)
 *        .setName("Java is to Javascript as car is to carpet")
 *        .setLocked(false)
 *        .setSlowmode(120)
 *        .queue();
 * }
 *
 * @see ThreadChannel#getManager()
 * @see ThreadChannel
 */
public interface ThreadChannelManager
        extends ChannelManager<ThreadChannel, ThreadChannelManager>,
                ISlowmodeChannelManager<ThreadChannel, ThreadChannelManager> {
    /**
     * Sets the inactive time before autoarchiving of this ThreadChannel.
     *
     * <p>This is limited to the choices offered in {@link ThreadChannel.AutoArchiveDuration}
     *
     * @param  autoArchiveDuration
     *         The new duration before an inactive channel will be autoarchived.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel#getAutoArchiveDuration()
     */
    @NotNull
    @CheckReturnValue
    ThreadChannelManager setAutoArchiveDuration(@NotNull ThreadChannel.AutoArchiveDuration autoArchiveDuration);

    /**
     * Sets the archived state of this ThreadChannel.
     *
     * @param  archived
     *         The new archived state for the selected {@link ThreadChannel}
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account is not the thread owner or does not have the {@link lonter.jfa.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *         Or if the thread is locked (archived by a moderator) and the current account does not have the {@link lonter.jfa.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @return this ThreadChannelManager for chaining convenience
     *
     * @see ThreadChannel#isArchived()
     */
    @NotNull
    @CheckReturnValue
    ThreadChannelManager setArchived(boolean archived);

    /**
     * Sets the locked state of this ThreadChannel.
     *
     * <p>This is the equivalent of archiving as a moderator.
     *
     * @param  locked
     *         The new locked state for the selected {@link ThreadChannel}
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account is not the thread owner or does not have the {@link lonter.jfa.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel#isLocked()
     */
    @NotNull
    @CheckReturnValue
    ThreadChannelManager setLocked(boolean locked);

    /**
     * Sets the invitable state of this ThreadChannel.
     *
     * <p>This property can only be set on private ThreadChannels.
     *
     * @param  invitable
     *         The new invitable state for the selected {@link ThreadChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link ThreadChannel} is not a private ThreadChannel
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account is not the thread owner or does not have the {@link lonter.jfa.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel#isInvitable()
     * @see ThreadChannel#isPublic()
     */
    @NotNull
    @CheckReturnValue
    ThreadChannelManager setInvitable(boolean invitable);

    /**
     * Sets the pinned state of this ThreadChannel.
     *
     * <p>This property can only be set on forum post threads.
     *
     * @param  pinned
     *         The new pinned state for the selected {@link ThreadChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link ThreadChannel} is not a forum post thread
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account is not the thread owner or does not have the {@link lonter.jfa.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission.
     *
     * @return this ThreadChannelManager for chaining convenience.
     *
     * @see ThreadChannel#isPinned()
     */
    @NotNull
    @CheckReturnValue
    ThreadChannelManager setPinned(boolean pinned);

    /**
     * Sets the applied {@link lonter.jfa.api.entities.channel.forums.ForumTag ForumTags} for this forum post thread.
     * <br>This is only applicable to public threads inside forum channels. The tags must be from the forum channel.
     * You can get the list of available tags with {@link ForumChannel#getAvailableTags()}.
     *
     * @param  tags
     *         The new tags for the thread
     *
     * @throws IllegalStateException
     *         If the thread is not a forum post
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If null is provided</li>
     *             <li>If more than {@value ForumChannel#MAX_POST_TAGS} tags are provided</li>
     *             <li>If at least one tag is {@link ForumChannel#isTagRequired() required} and none were provided</li>
     *         </ul>
     *
     * @return this ThreadChannelManager for chaining convenience.
     */
    @NotNull
    @CheckReturnValue
    ThreadChannelManager setAppliedTags(@NotNull Collection<? extends ForumTagSnowflake> tags);

    /**
     * Sets the applied {@link lonter.jfa.api.entities.channel.forums.ForumTag ForumTags} for this forum post thread.
     * <br>This is only applicable to public threads inside forum channels. The tags must be from the forum channel.
     * You can get the list of available tags with {@link ForumChannel#getAvailableTags()}.
     *
     * @param  tags
     *         The new tags for the thread
     *
     * @throws IllegalStateException
     *         If the thread is not a forum post
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If null is provided</li>
     *             <li>If more than {@value ForumChannel#MAX_POST_TAGS} tags are provided</li>
     *             <li>If at least one tag is {@link ForumChannel#isTagRequired() required} and none were provided</li>
     *         </ul>
     *
     * @return this ThreadChannelManager for chaining convenience.
     */
    @NotNull
    @CheckReturnValue
    default ThreadChannelManager setAppliedTags(@NotNull ForumTagSnowflake... tags) {
        Checks.noneNull(tags, "Tags");
        return setAppliedTags(Arrays.asList(tags));
    }
}
