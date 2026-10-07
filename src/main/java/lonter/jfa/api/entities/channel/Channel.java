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

package lonter.jfa.api.entities.channel;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.IMentionable;
import lonter.jfa.api.entities.detached.IDetachableEntity;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.utils.MiscUtil;

import java.util.EnumSet;
import java.util.FormattableFlags;
import java.util.Formatter;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract Channel interface for all {@link ChannelType ChannelTypes}.
 */
public interface Channel extends IMentionable, IDetachableEntity {
    /**
     * The maximum length a channel name can be. ({@value #MAX_NAME_LENGTH})
     */
    int MAX_NAME_LENGTH = 100;

    /**
     * The flags configured for this channel.
     * <br>This feature is currently primarily used for {@link lonter.jfa.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * @return {@link EnumSet} of the configured {@link ChannelFlag ChannelFlags}, changes to this enum set are not reflected in the API.
     */
    @NotNull
    default EnumSet<ChannelFlag> getFlags() {
        return EnumSet.noneOf(ChannelFlag.class);
    }

    /**
     * The human readable name of this channel.
     *
     * <p>May be an empty string for {@link lonter.jfa.api.entities.channel.concrete.GroupChannel GroupChannels}
     * with no name (Group DMs with no name displays the recipients on the Fluxer client).
     *
     * @return The name of this channel
     */
    @NotNull
    String getName();

    /**
     * The {@link ChannelType ChannelType} for this channel
     *
     * @return The channel type
     */
    @NotNull
    ChannelType getType();

    /**
     * Returns the {@link lonter.jfa.api.JFA JFA} instance of this channel
     *
     * @return the corresponding JFA instance
     */
    @NotNull
    JFA getJFA();

    /**
     * Deletes this Channel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     * </ul>
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction}
     */
    @NotNull
    @CheckReturnValue
    RestAction<Void> delete();

    @NotNull
    @Override
    default String getAsMention() {
        return "<#" + getId() + '>';
    }

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision) {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        String out;

        if (alt) {
            out = "#" + (upper ? getName().toUpperCase(formatter.locale()) : getName());
        } else {
            out = getAsMention();
        }

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}
