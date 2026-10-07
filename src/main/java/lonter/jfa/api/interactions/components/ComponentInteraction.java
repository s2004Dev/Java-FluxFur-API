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

package lonter.jfa.api.interactions.components;

import lonter.jfa.api.components.ActionComponent;
import lonter.jfa.api.components.Component;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.unions.GuildMessageChannelUnion;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.interactions.ICustomIdInteraction;
import lonter.jfa.api.interactions.callbacks.IMessageEditCallback;
import lonter.jfa.api.interactions.callbacks.IModalCallback;
import lonter.jfa.api.interactions.callbacks.IReplyCallback;

import org.jetbrains.annotations.NotNull;

/**
 * Interaction on a message {@link ActionComponent}.
 *
 * <p>Instead of {@link #deferReply()} and {@link #reply(String)} you can use {@link #deferEdit()} and {@link #editMessage(String)} with these interactions!
 * <b>You can only acknowledge an interaction once!</b>
 */
public interface ComponentInteraction
        extends IReplyCallback, IMessageEditCallback, IModalCallback, ICustomIdInteraction {

    @Override
    @NotNull
    default String getCustomId() {
        return getComponentId();
    }

    /**
     * The custom component ID provided to the component when it was originally created.
     * <br>This value should be used to determine what action to take in regard to this interaction.
     *
     * <br>This id does not have to be numerical.
     *
     * @return The component ID
     *
     * @see    ActionComponent#getCustomId()
     */
    @NotNull
    String getComponentId();

    /**
     * The numeric component ID provided to the component when it was originally created.
     * <br>This value is typically used to uniquely identify the component.
     *
     * @return The unique, numeric component ID
     *
     * @see    ActionComponent#getUniqueId()
     */
    default int getUniqueId() {
        return getComponent().getUniqueId();
    }

    /**
     * The {@link ActionComponent} instance.
     *
     * @return The {@link ActionComponent}
     */
    @NotNull
    ActionComponent getComponent();

    /**
     * The {@link Message} instance.
     *
     * @return The {@link Message}
     */
    @NotNull
    Message getMessage();

    /**
     * The id of the message.
     *
     * @return The message id
     */
    long getMessageIdLong();

    /**
     * The id of the message.
     *
     * @return The message id
     */
    @NotNull
    default String getMessageId() {
        return Long.toUnsignedString(getMessageIdLong());
    }

    /**
     * The {@link Component.Type}
     *
     * @return The {@link Component.Type}
     */
    @NotNull
    Component.Type getComponentType();

    /**
     * The respective {@link MessageChannelUnion} for this interaction.
     *
     * @return The {@link MessageChannelUnion}
     */
    @NotNull
    @Override
    MessageChannelUnion getChannel();

    @NotNull
    @Override
    default GuildMessageChannelUnion getGuildChannel() {
        return (GuildMessageChannelUnion) IReplyCallback.super.getGuildChannel();
    }
}
