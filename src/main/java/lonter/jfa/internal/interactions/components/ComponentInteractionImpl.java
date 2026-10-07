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

package lonter.jfa.internal.interactions.components;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.interactions.components.ComponentInteraction;
import lonter.jfa.api.modals.Modal;
import lonter.jfa.api.requests.restaction.interactions.ModalCallbackAction;
import lonter.jfa.api.requests.restaction.interactions.ReplyCallbackAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.ReceivedMessage;
import lonter.jfa.internal.interactions.DeferrableInteractionImpl;
import lonter.jfa.internal.requests.restaction.interactions.MessageEditCallbackActionImpl;
import lonter.jfa.internal.requests.restaction.interactions.ModalCallbackActionImpl;
import lonter.jfa.internal.requests.restaction.interactions.ReplyCallbackActionImpl;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

public abstract class ComponentInteractionImpl extends DeferrableInteractionImpl implements ComponentInteraction {
    protected final String customId;
    protected final Message message;
    protected final long messageId;

    public ComponentInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, data);
        this.customId = data.getObject("data").getString("custom_id");
        // message might be just id and flags for ephemeral messages
        // in which case our "message" is null
        DataObject messageJson = data.getObject("message");
        messageId = messageJson.getUnsignedLong("id");

        if (messageJson.isNull("type")) {
            message = null;
        } else {
            Guild guild = getGuild();
            MessageChannel channel = getChannel();
            message = jfa.getEntityBuilder().createMessageBestEffort(messageJson, channel, guild);
            // We assume that component interactions come from messages the bot sent
            ((ReceivedMessage) message).withHook(getHook());
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannelUnion getChannel() {
        return (MessageChannelUnion) super.getChannel();
    }

    @NotNull
    @Override
    public String getComponentId() {
        return customId;
    }

    @NotNull
    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public long getMessageIdLong() {
        return messageId;
    }

    @NotNull
    @Override
    public MessageEditCallbackActionImpl deferEdit() {
        return new MessageEditCallbackActionImpl(this.hook);
    }

    @NotNull
    @Override
    public ReplyCallbackAction deferReply() {
        return new ReplyCallbackActionImpl(this.hook);
    }

    @NotNull
    @Override
    public ModalCallbackAction replyModal(@NotNull Modal modal) {
        Checks.notNull(modal, "Modal");

        return new ModalCallbackActionImpl(this, modal);
    }
}
