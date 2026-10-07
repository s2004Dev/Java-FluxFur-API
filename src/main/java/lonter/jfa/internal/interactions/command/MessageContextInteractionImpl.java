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

package lonter.jfa.internal.interactions.command;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.interactions.commands.context.MessageContextInteraction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;

public class MessageContextInteractionImpl extends ContextInteractionImpl<Message>
        implements MessageContextInteraction {
    public MessageContextInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, data);
    }

    @Override
    protected Message parse(DataObject interaction, DataObject resolved) {
        DataObject messages = resolved.getObject("messages");
        DataObject message = messages.getObject(messages.keys().iterator().next());

        Guild guild = getGuild();
        MessageChannel channel = getChannel();

        return api.getEntityBuilder().createMessageBestEffort(message, channel, guild);
    }

    @Override
    public MessageChannelUnion getChannel() {
        return (MessageChannelUnion) super.getChannel();
    }
}
