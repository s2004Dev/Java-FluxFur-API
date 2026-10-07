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
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.events.interaction.GenericAutoCompleteInteractionEvent;
import lonter.jfa.api.interactions.AutoCompleteQuery;
import lonter.jfa.api.interactions.callbacks.IAutoCompleteCallback;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.CommandAutoCompleteInteraction;
import lonter.jfa.api.interactions.commands.OptionMapping;
import lonter.jfa.api.requests.restaction.interactions.AutoCompleteCallbackAction;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a user is typing in an {@link lonter.jfa.api.interactions.commands.build.OptionData option} which
 * supports {@link lonter.jfa.api.interactions.commands.build.OptionData#setAutoComplete(boolean) auto-complete}.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://fluxer.com/developers/applications" target="_blank">Fluxer Developers Portal</a>.
 *
 * @see CommandAutoCompleteInteraction
 * @see IAutoCompleteCallback
 */
public class CommandAutoCompleteInteractionEvent extends GenericAutoCompleteInteractionEvent
        implements CommandAutoCompleteInteraction {
    private final CommandAutoCompleteInteraction interaction;

    public CommandAutoCompleteInteractionEvent(
            @NotNull JFA api, long responseNumber, @NotNull CommandAutoCompleteInteraction interaction) {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @NotNull
    @Override
    public CommandAutoCompleteInteraction getInteraction() {
        return interaction;
    }

    @NotNull
    @Override
    public AutoCompleteQuery getFocusedOption() {
        return interaction.getFocusedOption();
    }

    @NotNull
    @Override
    public Command.Type getCommandType() {
        return interaction.getCommandType();
    }

    @NotNull
    @Override
    public String getName() {
        return interaction.getName();
    }

    @Nullable
    @Override
    public String getSubcommandName() {
        return interaction.getSubcommandName();
    }

    @Nullable
    @Override
    public String getSubcommandGroup() {
        return interaction.getSubcommandGroup();
    }

    @Override
    public long getCommandIdLong() {
        return interaction.getCommandIdLong();
    }

    @Override
    public boolean isGuildCommand() {
        return interaction.isGuildCommand();
    }

    @NotNull
    @Override
    public List<OptionMapping> getOptions() {
        return interaction.getOptions();
    }

    @NotNull
    @Override
    @CheckReturnValue
    public AutoCompleteCallbackAction replyChoices(@NotNull Collection<Command.Choice> choices) {
        return interaction.replyChoices(choices);
    }

    @NotNull
    @Override
    public MessageChannelUnion getChannel() {
        return interaction.getChannel();
    }
}
