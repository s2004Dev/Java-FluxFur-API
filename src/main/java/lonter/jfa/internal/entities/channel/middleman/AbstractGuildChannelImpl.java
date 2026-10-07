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

package lonter.jfa.internal.entities.channel.middleman;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.channel.AbstractChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.middleman.GuildChannelMixin;
import lonter.jfa.internal.utils.ChannelUtil;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractGuildChannelImpl<T extends AbstractGuildChannelImpl<T>> extends AbstractChannelImpl<T>
        implements GuildChannelMixin<T> {
    private Guild guild;

    public AbstractGuildChannelImpl(long id, Guild guild) {
        super(id, guild.getJFA());
        this.guild = guild;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        Guild cachedGuild = getJFA().getGuildById(id);
        if (cachedGuild instanceof GuildImpl) {
            return this.guild = cachedGuild;
        }
        return guild;
    }

    @Override
    public int compareTo(@NotNull GuildChannel o) {
        return ChannelUtil.compare(this, o);
    }
}
