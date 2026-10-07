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

package lonter.jfa.api.events.interaction.command;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.interactions.callbacks.IReplyCallback;
import lonter.jfa.api.interactions.commands.context.MessageContextInteraction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a message context command was used.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://fluxer.com/developers/applications" target="_blank">Fluxer Developers Portal</a>.
 *
 * @see MessageContextInteraction
 * @see IReplyCallback
 */
public class MessageContextInteractionEvent extends GenericContextInteractionEvent<Message>
        implements MessageContextInteraction {
    public MessageContextInteractionEvent(
            @NotNull JFA api, long responseNumber, @NotNull MessageContextInteraction interaction) {
        super(api, responseNumber, interaction);
    }

    @NotNull
    @Override
    public MessageContextInteraction getInteraction() {
        return (MessageContextInteraction) super.getInteraction();
    }

    @Nullable
    @Override
    public MessageChannelUnion getChannel() {
        return getInteraction().getChannel();
    }
}
