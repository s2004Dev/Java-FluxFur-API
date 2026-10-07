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

package lonter.jfa.api.exceptions;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the user is missing the {@link Permission#VIEW_CHANNEL VIEW_CHANNEL},
 * in addition to {@link Permission#VOICE_CONNECT VOICE_CONNECT} permission if {@link Channel#getType()} is an {@link ChannelType#isAudio() audio} type.
 *
 * @see   lonter.jfa.api.entities.IPermissionHolder#hasAccess(GuildChannel)
 */
public class MissingAccessException extends InsufficientPermissionException {
    public MissingAccessException(@NotNull GuildChannel channel, @NotNull Permission permission) {
        super(channel, permission);
    }

    public MissingAccessException(
            @NotNull GuildChannel channel, @NotNull Permission permission, @NotNull String reason) {
        super(channel, permission, reason);
    }
}
