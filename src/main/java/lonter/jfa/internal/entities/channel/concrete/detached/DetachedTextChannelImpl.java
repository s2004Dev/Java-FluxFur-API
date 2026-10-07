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

import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.unions.DefaultGuildChannelUnion;
import lonter.jfa.api.managers.channel.concrete.TextChannelManager;
import lonter.jfa.internal.entities.channel.middleman.AbstractStandardGuildMessageChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import lonter.jfa.internal.entities.channel.mixin.concrete.TextChannelMixin;
import lonter.jfa.internal.entities.detached.DetachedGuildImpl;
import lonter.jfa.internal.interactions.ChannelInteractionPermissions;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public class DetachedTextChannelImpl extends AbstractStandardGuildMessageChannelImpl<DetachedTextChannelImpl>
        implements TextChannel,
                DefaultGuildChannelUnion,
                TextChannelMixin<DetachedTextChannelImpl>,
                IInteractionPermissionMixin<DetachedTextChannelImpl> {
    private int slowmode;
    private ChannelInteractionPermissions interactionPermissions;

    public DetachedTextChannelImpl(long id, DetachedGuildImpl guild) {
        super(id, guild);
    }

    @Override
    public boolean isDetached() {
        return true;
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return ChannelType.TEXT;
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        throw detachedException();
    }

    @Override
    public int getSlowmode() {
        return slowmode;
    }

    @NotNull
    @Override
    public TextChannelManager getManager() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelInteractionPermissions getInteractionPermissions() {
        return interactionPermissions;
    }

    @Override
    public DetachedTextChannelImpl setSlowmode(int slowmode) {
        this.slowmode = slowmode;
        return this;
    }

    @NotNull
    @Override
    public DetachedTextChannelImpl setInteractionPermissions(
            @NotNull ChannelInteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
