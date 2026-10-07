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

package lonter.jfa.api.events.channel.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelField;
import lonter.jfa.api.entities.channel.ChannelFlag;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the {@link Channel#getFlags() flags} of a {@link Channel} changed.
 *
 * <p>Can be used to retrieve the old flags and the new ones.
 *
 * @see ChannelField#FLAGS
 */
public class ChannelUpdateFlagsEvent extends GenericChannelUpdateEvent<EnumSet<ChannelFlag>> {
    public static final ChannelField FIELD = ChannelField.FLAGS;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateFlagsEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull Channel channel,
            @NotNull EnumSet<ChannelFlag> oldValue,
            @NotNull EnumSet<ChannelFlag> newValue) {
        super(api, responseNumber, channel, FIELD, oldValue, newValue);
    }

    @NotNull
    @Override
    public EnumSet<ChannelFlag> getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    public EnumSet<ChannelFlag> getNewValue() {
        return super.getNewValue();
    }
}
