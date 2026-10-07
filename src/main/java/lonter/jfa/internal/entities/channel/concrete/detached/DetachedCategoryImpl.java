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
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.*;
import lonter.jfa.api.managers.channel.concrete.CategoryManager;
import lonter.jfa.api.requests.restaction.ChannelAction;
import lonter.jfa.api.requests.restaction.order.CategoryOrderAction;
import lonter.jfa.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import lonter.jfa.internal.entities.channel.mixin.concrete.CategoryMixin;
import lonter.jfa.internal.entities.detached.DetachedGuildImpl;
import lonter.jfa.internal.interactions.ChannelInteractionPermissions;

import org.jetbrains.annotations.NotNull;

public class DetachedCategoryImpl extends AbstractGuildChannelImpl<DetachedCategoryImpl>
        implements Category, CategoryMixin<DetachedCategoryImpl>, IInteractionPermissionMixin<DetachedCategoryImpl> {
    private ChannelInteractionPermissions interactionPermissions;

    private int position;

    public DetachedCategoryImpl(long id, DetachedGuildImpl guild) {
        super(id, guild);
    }

    @Override
    public boolean isDetached() {
        return true;
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
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<NewsChannel> createNewsChannel(@NotNull String name) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<VoiceChannel> createVoiceChannel(@NotNull String name) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<StageChannel> createStageChannel(@NotNull String name) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<ForumChannel> createForumChannel(@NotNull String name) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<MediaChannel> createMediaChannel(@NotNull String name) {
        throw detachedException();
    }

    @NotNull
    @Override
    public CategoryOrderAction modifyTextChannelPositions() {
        throw detachedException();
    }

    @NotNull
    @Override
    public CategoryOrderAction modifyVoiceChannelPositions() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelAction<Category> createCopy() {
        throw detachedException();
    }

    @NotNull
    @Override
    public CategoryManager getManager() {
        throw detachedException();
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
    public DetachedCategoryImpl setPosition(int position) {
        this.position = position;
        return this;
    }

    @NotNull
    @Override
    public DetachedCategoryImpl setInteractionPermissions(
            @NotNull ChannelInteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
