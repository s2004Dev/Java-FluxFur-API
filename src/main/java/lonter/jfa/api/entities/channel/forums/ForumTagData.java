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

package lonter.jfa.api.entities.channel.forums;

import lonter.jfa.api.entities.emoji.Emoji;
import lonter.jfa.api.entities.emoji.EmojiUnion;
import lonter.jfa.api.managers.channel.concrete.ForumChannelManager;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data class used to create or update existing forum tags.
 *
 * @see ForumChannelManager#setAvailableTags(List)
 */
public class ForumTagData implements BaseForumTag {
    private String name;
    private Emoji emoji;
    private boolean moderated;
    private long id;

    /**
     * Create a new {@link ForumTagData} instance.
     *
     * @param name
     *        The tag name (1-{@value ForumTag#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the provided name is null or not between 1 and  {@value ForumTag#MAX_NAME_LENGTH} characters long
     */
    public ForumTagData(@NotNull String name) {
        setName(name);
    }

    /**
     * Creates a new {@link ForumTagData} instance based on the provided {@link BaseForumTag}.
     * <br>This also binds to the id of the provided tag, if available.
     *
     * @param  tag
     *         The base tag to use
     *
     * @throws IllegalArgumentException
     *         If null is provided or the tag has an invalid name
     *
     * @return The new {@link ForumTagData} instance
     */
    @NotNull
    public static ForumTagData from(@NotNull BaseForumTag tag) {
        Checks.notNull(tag, "Tag");
        ForumTagData data =
                new ForumTagData(tag.getName()).setEmoji(tag.getEmoji()).setModerated(tag.isModerated());
        if (tag instanceof ForumTagSnowflake) {
            data.id = ((ForumTagSnowflake) tag).getIdLong();
        }
        return data;
    }

    /**
     * Set the new tag name to use.
     *
     * @param  name
     *         The new tag name (1-{@value ForumTag#MAX_NAME_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the provided name is null or not between 1 and  {@value ForumTag#MAX_NAME_LENGTH} characters long
     *
     * @return The updated ForumTagData instance
     */
    @NotNull
    public ForumTagData setName(@NotNull String name) {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, ForumTag.MAX_NAME_LENGTH, "Name");
        this.name = name;
        return this;
    }

    /**
     * Set whether the tag can only be applied by forum moderators.
     *
     * @param  moderated
     *         True, if the tag is restricted to moderators
     *
     * @return The updated ForumTagData instance
     *
     * @see    #isModerated()
     */
    @NotNull
    public ForumTagData setModerated(boolean moderated) {
        this.moderated = moderated;
        return this;
    }

    /**
     * Set the emoji to use for this tag.
     * <br>This emoji is displayed as an icon attached to the tag.
     *
     * @param  emoji
     *         The emoji icon of the tag
     *
     * @return The updated ForumTagData instance
     */
    @NotNull
    public ForumTagData setEmoji(@Nullable Emoji emoji) {
        this.emoji = emoji;
        return this;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isModerated() {
        return moderated;
    }

    @Nullable
    @Override
    public EmojiUnion getEmoji() {
        return (EmojiUnion) emoji;
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject json = BaseForumTag.super.toData();
        if (id != 0) {
            json.put("id", Long.toUnsignedString(id));
        }
        return json;
    }

    @Override
    public String toString() {
        return toData().toString();
    }
}
