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
import lonter.jfa.api.events.interaction.GenericInteractionCreateEvent;
import lonter.jfa.api.interactions.InteractionHook;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.CommandInteraction;
import lonter.jfa.api.interactions.commands.OptionMapping;
import lonter.jfa.api.modals.Modal;
import lonter.jfa.api.requests.restaction.interactions.ModalCallbackAction;
import lonter.jfa.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a {@link CommandInteraction} was used.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://fluxer.com/developers/applications" target="_blank">Fluxer Developers Portal</a>.
 */
public class GenericCommandInteractionEvent extends GenericInteractionCreateEvent implements CommandInteraction {
    public GenericCommandInteractionEvent(
            @NotNull JFA api, long responseNumber, @NotNull CommandInteraction interaction) {
        super(api, responseNumber, interaction);
    }

    @NotNull
    @Override
    public CommandInteraction getInteraction() {
        return (CommandInteraction) super.getInteraction();
    }

    @NotNull
    @Override
    public Command.Type getCommandType() {
        return getInteraction().getCommandType();
    }

    @NotNull
    @Override
    public String getName() {
        return getInteraction().getName();
    }

    @Nullable
    @Override
    public String getSubcommandName() {
        return getInteraction().getSubcommandName();
    }

    @Nullable
    @Override
    public String getSubcommandGroup() {
        return getInteraction().getSubcommandGroup();
    }

    @Override
    public long getCommandIdLong() {
        return getInteraction().getCommandIdLong();
    }

    @Override
    public boolean isGuildCommand() {
        return getInteraction().isGuildCommand();
    }

    @NotNull
    @Override
    public List<OptionMapping> getOptions() {
        return getInteraction().getOptions();
    }

    @NotNull
    @Override
    public InteractionHook getHook() {
        return getInteraction().getHook();
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ReplyCallbackAction deferReply() {
        return getInteraction().deferReply();
    }

    @NotNull
    @Override
    @CheckReturnValue
    public ModalCallbackAction replyModal(@NotNull Modal modal) {
        return getInteraction().replyModal(modal);
    }
}
