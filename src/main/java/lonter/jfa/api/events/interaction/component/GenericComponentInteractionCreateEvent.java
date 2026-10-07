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

package lonter.jfa.api.events.interaction.component;

import lonter.jfa.api.JFA;
import lonter.jfa.api.components.ActionComponent;
import lonter.jfa.api.components.Component;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.events.interaction.GenericInteractionCreateEvent;
import lonter.jfa.api.interactions.InteractionHook;
import lonter.jfa.api.interactions.components.ComponentInteraction;
import lonter.jfa.api.modals.Modal;
import lonter.jfa.api.requests.restaction.interactions.MessageEditCallbackAction;
import lonter.jfa.api.requests.restaction.interactions.ModalCallbackAction;
import lonter.jfa.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link ComponentInteraction} was created in a channel.
 * <br>Every component interaction event is derived from this event.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://fluxer.com/developers/applications" target="_blank">Fluxer Developers Portal</a>.
 */
public class GenericComponentInteractionCreateEvent extends GenericInteractionCreateEvent
        implements ComponentInteraction {
    private final ComponentInteraction interaction;

    public GenericComponentInteractionCreateEvent(
            @NotNull JFA api, long responseNumber, @NotNull ComponentInteraction interaction) {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @NotNull
    @Override
    public ComponentInteraction getInteraction() {
        return interaction;
    }

    @NotNull
    @Override
    public MessageChannelUnion getChannel() {
        return interaction.getChannel();
    }

    @NotNull
    @Override
    public String getComponentId() {
        return interaction.getComponentId();
    }

    @NotNull
    @Override
    public ActionComponent getComponent() {
        return interaction.getComponent();
    }

    @NotNull
    @Override
    public Message getMessage() {
        return interaction.getMessage();
    }

    @Override
    public long getMessageIdLong() {
        return interaction.getMessageIdLong();
    }

    @NotNull
    @Override
    public Component.Type getComponentType() {
        return interaction.getComponentType();
    }

    @NotNull
    @Override
    @CheckReturnValue
    public MessageEditCallbackAction deferEdit() {
        return interaction.deferEdit();
    }

    @NotNull
    @Override
    public InteractionHook getHook() {
        return interaction.getHook();
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ReplyCallbackAction deferReply() {
        return interaction.deferReply();
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ModalCallbackAction replyModal(@NotNull Modal modal) {
        return interaction.replyModal(modal);
    }
}
