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

package lonter.jfa.internal.entities.channel;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.channel.attribute.IThreadContainer;
import lonter.jfa.api.entities.channel.concrete.*;
import lonter.jfa.api.entities.channel.middleman.*;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.channel.mixin.ChannelMixin;
import lonter.jfa.internal.utils.ChannelUtil;
import lonter.jfa.internal.utils.EntityString;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractChannelImpl<T extends AbstractChannelImpl<T>> implements ChannelMixin<T> {
    protected final long id;
    protected final JFAImpl api;

    protected String name;

    public AbstractChannelImpl(long id, JFA api) {
        this.id = id;
        this.api = (JFAImpl) api;
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    // -- Union Hooks --

    @NotNull
    public PrivateChannel asPrivateChannel() {
        return ChannelUtil.safeChannelCast(this, PrivateChannel.class);
    }

    @NotNull
    @Override
    public GroupChannel asGroupChannel() {
        return ChannelUtil.safeChannelCast(this, GroupChannel.class);
    }

    @NotNull
    public TextChannel asTextChannel() {
        return ChannelUtil.safeChannelCast(this, TextChannel.class);
    }

    @NotNull
    public NewsChannel asNewsChannel() {
        return ChannelUtil.safeChannelCast(this, NewsChannel.class);
    }

    @NotNull
    public VoiceChannel asVoiceChannel() {
        return ChannelUtil.safeChannelCast(this, VoiceChannel.class);
    }

    @NotNull
    public StageChannel asStageChannel() {
        return ChannelUtil.safeChannelCast(this, StageChannel.class);
    }

    @NotNull
    public ThreadChannel asThreadChannel() {
        return ChannelUtil.safeChannelCast(this, ThreadChannel.class);
    }

    @NotNull
    public Category asCategory() {
        return ChannelUtil.safeChannelCast(this, Category.class);
    }

    @NotNull
    @Override
    public ForumChannel asForumChannel() {
        return ChannelUtil.safeChannelCast(this, ForumChannel.class);
    }

    @NotNull
    @Override
    public MediaChannel asMediaChannel() {
        return ChannelUtil.safeChannelCast(this, MediaChannel.class);
    }

    @NotNull
    public MessageChannel asMessageChannel() {
        return ChannelUtil.safeChannelCast(this, MessageChannel.class);
    }

    @NotNull
    public AudioChannel asAudioChannel() {
        return ChannelUtil.safeChannelCast(this, AudioChannel.class);
    }

    @NotNull
    public IThreadContainer asThreadContainer() {
        return ChannelUtil.safeChannelCast(this, IThreadContainer.class);
    }

    @NotNull
    public GuildChannel asGuildChannel() {
        return ChannelUtil.safeChannelCast(this, GuildChannel.class);
    }

    @NotNull
    public GuildMessageChannel asGuildMessageChannel() {
        return ChannelUtil.safeChannelCast(this, GuildMessageChannel.class);
    }

    @NotNull
    public StandardGuildChannel asStandardGuildChannel() {
        return ChannelUtil.safeChannelCast(this, StandardGuildChannel.class);
    }

    @NotNull
    public StandardGuildMessageChannel asStandardGuildMessageChannel() {
        return ChannelUtil.safeChannelCast(this, StandardGuildMessageChannel.class);
    }

    @Override
    public String toString() {
        return new EntityString(this).setName(name).toString();
    }
}
