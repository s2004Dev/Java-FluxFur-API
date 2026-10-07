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
import lonter.jfa.api.entities.channel.middleman.AudioChannel;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link Channel Channel's} bitrate has been updated.
 *
 * <p>Limited to {@link AudioChannel Audio Channels}.
 *
 * @see AudioChannel#getBitrate()
 * @see ChannelField#BITRATE
 */
public class ChannelUpdateBitrateEvent extends GenericChannelUpdateEvent<Integer> {
    public static final ChannelField FIELD = ChannelField.BITRATE;
    public static final String IDENTIFIER = FIELD.getFieldName();

    public ChannelUpdateBitrateEvent(
            @NotNull JFA api, long responseNumber, Channel channel, Integer oldValue, Integer newValue) {
        super(api, responseNumber, channel, FIELD, oldValue, newValue);
    }
}
