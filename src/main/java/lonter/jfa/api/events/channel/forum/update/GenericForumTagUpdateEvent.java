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

package lonter.jfa.api.events.channel.forum.update;

import lonter.jfa.annotations.UnknownNullability;
import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.channel.attribute.IPostContainer;
import lonter.jfa.api.entities.channel.forums.ForumTag;
import lonter.jfa.api.events.UpdateEvent;
import lonter.jfa.api.events.channel.forum.GenericForumTagEvent;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

/**
 * Abstraction of all {@link ForumTag} updates.
 *
 * <p><b>Requirements</b><br>
 * This requires {@link lonter.jfa.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
 * {@link lonter.jfa.api.JFABuilder#createLight(String, Collection) JFABuilder.createLight(...)} disables this by default.
 *
 * @param <T>
 *        The type of the updated field
 */
public abstract class GenericForumTagUpdateEvent<T> extends GenericForumTagEvent implements UpdateEvent<ForumTag, T> {
    private final T previous;
    private final T next;
    private final String identifier;

    public GenericForumTagUpdateEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull IPostContainer channel,
            @NotNull ForumTag tag,
            T previous,
            T next,
            @NotNull String identifier) {
        super(api, responseNumber, channel, tag);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @NotNull
    @Override
    public ForumTag getEntity() {
        return getTag();
    }

    @Override
    @UnknownNullability
    public T getOldValue() {
        return previous;
    }

    @Override
    @UnknownNullability
    public T getNewValue() {
        return next;
    }

    @NotNull
    @Override
    public String getPropertyIdentifier() {
        return identifier;
    }
}
