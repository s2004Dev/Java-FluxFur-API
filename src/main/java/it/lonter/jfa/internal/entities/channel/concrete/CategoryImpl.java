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

package it.lonter.jfa.internal.entities.channel.concrete;

import gnu.trove.map.TLongObjectMap;
import it.lonter.jfa.api.entities.Member;
import it.lonter.jfa.api.entities.PermissionOverride;
import it.lonter.jfa.api.entities.channel.ChannelType;
import it.lonter.jfa.api.entities.channel.concrete.*;
import it.lonter.jfa.api.entities.channel.middleman.GuildChannel;
import it.lonter.jfa.api.managers.channel.concrete.CategoryManager;
import it.lonter.jfa.api.requests.restaction.ChannelAction;
import it.lonter.jfa.api.requests.restaction.order.CategoryOrderAction;
import it.lonter.jfa.api.utils.MiscUtil;
import it.lonter.jfa.internal.entities.GuildImpl;
import it.lonter.jfa.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import it.lonter.jfa.internal.entities.channel.mixin.concrete.CategoryMixin;
import it.lonter.jfa.internal.managers.channel.concrete.CategoryManagerImpl;
import it.lonter.jfa.internal.utils.PermissionUtil;

import org.jetbrains.annotations.NotNull;

public class CategoryImpl extends AbstractGuildChannelImpl<CategoryImpl>
        implements Category, CategoryMixin<CategoryImpl> {
    private final TLongObjectMap<PermissionOverride> overrides = MiscUtil.newLongMap();

    private int position;

    public CategoryImpl(long id, GuildImpl guild) {
        super(id, guild);
    }

    @Override
    public boolean isDetached() {
        return false;
    }

    @NotNull
    @Override
    public GuildImpl getGuild() {
        return (GuildImpl) super.getGuild();
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return ChannelType.CATEGORY;
    }

    @Override
    public int getPositionRaw() {
        return position;
    }

    @NotNull
    @Override
    public ChannelAction<TextChannel> createTextChannel(@NotNull String name) {
        ChannelAction<TextChannel> action = getGuild().createTextChannel(name, this);
        return trySync(action);
    }

    @NotNull
    @Override
    public ChannelAction<NewsChannel> createNewsChannel(@NotNull String name) {
        ChannelAction<NewsChannel> action = getGuild().createNewsChannel(name, this);
        return trySync(action);
    }

    @NotNull
    @Override
    public ChannelAction<VoiceChannel> createVoiceChannel(@NotNull String name) {
        ChannelAction<VoiceChannel> action = getGuild().createVoiceChannel(name, this);
        return trySync(action);
    }

    @NotNull
    @Override
    public ChannelAction<StageChannel> createStageChannel(@NotNull String name) {
        ChannelAction<StageChannel> action = getGuild().createStageChannel(name, this);
        return trySync(action);
    }

    @NotNull
    @Override
    public ChannelAction<ForumChannel> createForumChannel(@NotNull String name) {
        ChannelAction<ForumChannel> action = getGuild().createForumChannel(name, this);
        return trySync(action);
    }

    @NotNull
    @Override
    public ChannelAction<MediaChannel> createMediaChannel(@NotNull String name) {
        ChannelAction<MediaChannel> action = getGuild().createMediaChannel(name, this);
        return trySync(action);
    }

    @NotNull
    @Override
    public CategoryOrderAction modifyTextChannelPositions() {
        return getGuild().modifyTextChannelPositions(this);
    }

    @NotNull
    @Override
    public CategoryOrderAction modifyVoiceChannelPositions() {
        return getGuild().modifyVoiceChannelPositions(this);
    }

    @NotNull
    @Override
    public ChannelAction<Category> createCopy() {
        return createCopy(getGuild());
    }

    @NotNull
    @Override
    public CategoryManager getManager() {
        return new CategoryManagerImpl(this);
    }

    @Override
    public TLongObjectMap<PermissionOverride> getPermissionOverrideMap() {
        return overrides;
    }

    @Override
    public CategoryImpl setPosition(int position) {
        this.position = position;
        return this;
    }

    private <T extends GuildChannel> ChannelAction<T> trySync(ChannelAction<T> action) {
        Member selfMember = getGuild().getSelfMember();
        if (!selfMember.canSync(this)) {
            long botPerms = PermissionUtil.getEffectivePermission(this, selfMember);
            for (PermissionOverride override : getPermissionOverrides()) {
                long perms = override.getDeniedRaw() | override.getAllowedRaw();
                if ((perms & ~botPerms) != 0) {
                    return action;
                }
            }
        }
        return action.syncPermissionOverrides();
    }
}
