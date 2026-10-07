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

package lonter.jfa.internal.entities.channel.concrete;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.unions.DefaultGuildChannelUnion;
import lonter.jfa.api.managers.channel.concrete.TextChannelManager;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.channel.middleman.AbstractStandardGuildMessageChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.concrete.TextChannelMixin;
import lonter.jfa.internal.managers.channel.concrete.TextChannelManagerImpl;
import lonter.jfa.internal.utils.Helpers;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public class TextChannelImpl extends AbstractStandardGuildMessageChannelImpl<TextChannelImpl>
        implements TextChannel, DefaultGuildChannelUnion, TextChannelMixin<TextChannelImpl> {
    private int slowmode;

    public TextChannelImpl(long id, GuildImpl guild) {
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
        return ChannelType.TEXT;
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        return getGuild().getMembersView().stream()
                .filter(m -> m.hasPermission(this, Permission.VIEW_CHANNEL))
                .collect(Helpers.toUnmodifiableList());
    }

    @Override
    public int getSlowmode() {
        return slowmode;
    }

    @NotNull
    @Override
    public TextChannelManager getManager() {
        return new TextChannelManagerImpl(this);
    }

    @Override
    public TextChannelImpl setSlowmode(int slowmode) {
        this.slowmode = slowmode;
        return this;
    }
}
