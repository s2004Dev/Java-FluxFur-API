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

package lonter.jfa.api.events.channel.forum;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.channel.attribute.IPostContainer;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.entities.channel.forums.ForumTag;
import lonter.jfa.api.events.Event;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

/**
 * Abstraction of all tags relating to {@link ForumTag} changes (excluding {@link ThreadChannel#getAppliedTags()}).
 *
 * <p><b>Requirements</b><br>
 * This requires {@link lonter.jfa.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
 * {@link lonter.jfa.api.JFABuilder#createLight(String, Collection) JFABuilder.createLight(...)} disables this by default.
 */
public abstract class GenericForumTagEvent extends Event {
    protected final IPostContainer channel;
    protected final ForumTag tag;

    public GenericForumTagEvent(
            @NotNull JFA api, long responseNumber, @NotNull IPostContainer channel, @NotNull ForumTag tag) {
        super(api, responseNumber);
        this.channel = channel;
        this.tag = tag;
    }

    /**
     * The {@link IPostContainer} which has been updated.
     *
     * @return The {@link IPostContainer}
     */
    @NotNull
    public IPostContainer getChannel() {
        return channel;
    }

    /**
     * The {@link ForumTag} that was affected by this event
     *
     * @return The {@link ForumTag}
     */
    @NotNull
    public ForumTag getTag() {
        return tag;
    }
}
