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

package it.lonter.jfa.internal.interactions.command;

import it.lonter.jfa.api.interactions.commands.CommandInteraction;
import it.lonter.jfa.api.interactions.commands.CommandInteractionPayload;
import it.lonter.jfa.api.modals.Modal;
import it.lonter.jfa.api.requests.restaction.interactions.ModalCallbackAction;
import it.lonter.jfa.api.requests.restaction.interactions.ReplyCallbackAction;
import it.lonter.jfa.api.utils.data.DataObject;
import it.lonter.jfa.internal.JFAImpl;
import it.lonter.jfa.internal.interactions.DeferrableInteractionImpl;
import it.lonter.jfa.internal.requests.restaction.interactions.ModalCallbackActionImpl;
import it.lonter.jfa.internal.requests.restaction.interactions.ReplyCallbackActionImpl;
import it.lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

public class CommandInteractionImpl extends DeferrableInteractionImpl
        implements CommandInteraction, CommandInteractionPayloadMixin {
    private final CommandInteractionPayloadImpl payload;

    public CommandInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, data);
        this.payload = new CommandInteractionPayloadImpl(jfa, data);
    }

    @Override
    public CommandInteractionPayload getCommandPayload() {
        return payload;
    }

    @NotNull
    @Override
    public ReplyCallbackAction deferReply() {
        return new ReplyCallbackActionImpl(hook);
    }

    @NotNull
    @Override
    public ModalCallbackAction replyModal(@NotNull Modal modal) {
        Checks.notNull(modal, "Modal");
        return new ModalCallbackActionImpl(this, modal);
    }
}
