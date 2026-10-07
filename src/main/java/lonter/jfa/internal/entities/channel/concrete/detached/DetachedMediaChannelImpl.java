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

package lonter.jfa.internal.entities.channel.concrete.detached;

import gnu.trove.map.TLongObjectMap;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.ChannelFlag;
import lonter.jfa.api.entities.channel.concrete.MediaChannel;
import lonter.jfa.api.entities.channel.forums.ForumTag;
import lonter.jfa.api.entities.channel.unions.GuildChannelUnion;
import lonter.jfa.api.entities.emoji.Emoji;
import lonter.jfa.api.entities.emoji.EmojiUnion;
import lonter.jfa.api.managers.channel.concrete.MediaChannelManager;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import lonter.jfa.internal.entities.channel.mixin.concrete.MediaChannelMixin;
import lonter.jfa.internal.entities.detached.DetachedGuildImpl;
import lonter.jfa.internal.entities.emoji.CustomEmojiImpl;
import lonter.jfa.internal.interactions.ChannelInteractionPermissions;
import lonter.jfa.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class DetachedMediaChannelImpl extends AbstractGuildChannelImpl<DetachedMediaChannelImpl>
        implements MediaChannel,
                GuildChannelUnion,
                MediaChannelMixin<DetachedMediaChannelImpl>,
                IInteractionPermissionMixin<DetachedMediaChannelImpl> {
    private ChannelInteractionPermissions interactionPermissions;
    private final SortedSnowflakeCacheViewImpl<ForumTag> tagCache =
            new SortedSnowflakeCacheViewImpl<>(ForumTag.class, ForumTag::getName, Comparator.naturalOrder());

    private Emoji defaultReaction;
    private String topic;
    private long parentCategoryId;
    private boolean nsfw = false;
    private int position;
    private int flags;
    private int slowmode;
    private int defaultSortOrder;
    protected int defaultThreadSlowmode;

    public DetachedMediaChannelImpl(long id, DetachedGuildImpl guild) {
        super(id, guild);
    }

    @Override
    public boolean isDetached() {
        return true;
    }

    @NotNull
    @Override
    public MediaChannelManager getManager() {
        throw detachedException();
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        throw detachedException();
    }

    @NotNull
    @Override
    public EnumSet<ChannelFlag> getFlags() {
        return ChannelFlag.fromRaw(flags);
    }

    @NotNull
    @Override
    public SortedSnowflakeCacheViewImpl<ForumTag> getAvailableTagCache() {
        return tagCache;
    }

    @Override
    public TLongObjectMap<PermissionOverride> getPermissionOverrideMap() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelInteractionPermissions getInteractionPermissions() {
        return interactionPermissions;
    }

    @Override
    public boolean isNSFW() {
        return nsfw;
    }

    @Override
    public int getPositionRaw() {
        return position;
    }

    @Override
    public long getParentCategoryIdLong() {
        return parentCategoryId;
    }

    @Override
    public int getSlowmode() {
        return slowmode;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public EmojiUnion getDefaultReaction() {
        return (EmojiUnion) defaultReaction;
    }

    @Override
    public int getDefaultThreadSlowmode() {
        return defaultThreadSlowmode;
    }

    @NotNull
    @Override
    public SortOrder getDefaultSortOrder() {
        return SortOrder.fromKey(defaultSortOrder);
    }

    public int getRawFlags() {
        return flags;
    }

    public int getRawSortOrder() {
        return defaultSortOrder;
    }

    // Setters

    @Override
    public DetachedMediaChannelImpl setParentCategory(long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
        return this;
    }

    @Override
    public DetachedMediaChannelImpl setPosition(int position) {
        this.position = position;
        return this;
    }

    @Override
    public DetachedMediaChannelImpl setDefaultThreadSlowmode(int defaultThreadSlowmode) {
        this.defaultThreadSlowmode = defaultThreadSlowmode;
        return this;
    }

    @Override
    public DetachedMediaChannelImpl setNSFW(boolean nsfw) {
        this.nsfw = nsfw;
        return this;
    }

    @Override
    public DetachedMediaChannelImpl setSlowmode(int slowmode) {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public DetachedMediaChannelImpl setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    public DetachedMediaChannelImpl setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    @Override
    public DetachedMediaChannelImpl setDefaultReaction(DataObject emoji) {
        if (emoji != null && !emoji.isNull("emoji_id")) {
            this.defaultReaction = new CustomEmojiImpl("", emoji.getUnsignedLong("emoji_id"), false);
        } else if (emoji != null && !emoji.isNull("emoji_name")) {
            this.defaultReaction = Emoji.fromUnicode(emoji.getString("emoji_name"));
        } else {
            this.defaultReaction = null;
        }
        return this;
    }

    @Override
    public DetachedMediaChannelImpl setDefaultSortOrder(int defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
        return this;
    }

    @NotNull
    @Override
    public DetachedMediaChannelImpl setInteractionPermissions(
            @NotNull ChannelInteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
