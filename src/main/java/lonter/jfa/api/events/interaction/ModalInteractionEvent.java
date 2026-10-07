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

package lonter.jfa.api.events.interaction;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.interactions.InteractionHook;
import lonter.jfa.api.interactions.modals.ModalInteraction;
import lonter.jfa.api.interactions.modals.ModalMapping;
import lonter.jfa.api.modals.Modal;
import lonter.jfa.api.requests.restaction.interactions.MessageEditCallbackAction;
import lonter.jfa.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a {@link Modal} was submitted.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://fluxer.com/developers/applications" target="_blank">Fluxer Developers Portal</a>.
 *
 * @see    ModalInteraction
 */
public class ModalInteractionEvent extends GenericInteractionCreateEvent implements ModalInteraction {
    private final ModalInteraction interaction;

    public ModalInteractionEvent(@NotNull JFA api, long responseNumber, @NotNull ModalInteraction interaction) {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @NotNull
    @Override
    public ModalInteraction getInteraction() {
        return interaction;
    }

    @NotNull
    @Override
    public String getModalId() {
        return interaction.getModalId();
    }

    @NotNull
    @Override
    public List<ModalMapping> getValues() {
        return interaction.getValues();
    }

    @Nullable
    @Override
    public Message getMessage() {
        return interaction.getMessage();
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ReplyCallbackAction deferReply() {
        return interaction.deferReply();
    }

    @NotNull
    @Override
    public InteractionHook getHook() {
        return interaction.getHook();
    }

    @NotNull
    @Override
    @CheckReturnValue
    public MessageEditCallbackAction deferEdit() {
        return interaction.deferEdit();
    }

    @NotNull
    @Override
    public MessageChannelUnion getChannel() {
        return interaction.getChannel();
    }
}
