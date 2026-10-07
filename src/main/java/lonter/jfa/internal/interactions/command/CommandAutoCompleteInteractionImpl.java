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

import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.interactions.AutoCompleteQuery;
import lonter.jfa.api.interactions.commands.*;
import lonter.jfa.api.requests.restaction.interactions.AutoCompleteCallbackAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.interactions.InteractionImpl;
import lonter.jfa.internal.requests.restaction.interactions.AutoCompleteCallbackActionImpl;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

public class CommandAutoCompleteInteractionImpl extends InteractionImpl
        implements CommandInteractionPayloadMixin, CommandAutoCompleteInteraction {
    private final CommandInteractionPayload payload;
    private AutoCompleteQuery focused;

    public CommandAutoCompleteInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, data);
        this.payload = new CommandInteractionPayloadImpl(jfa, data);

        DataArray options = data.getObject("data").getArray("options");
        findFocused(options);

        if (focused == null) {
            throw new IllegalStateException("Failed to get focused option for auto complete interaction");
        }
    }

    private void findFocused(DataArray options) {
        for (int i = 0; i < options.length(); i++) {
            DataObject option = options.getObject(i);
            switch (OptionType.fromKey(option.getInt("type"))) {
                case SUB_COMMAND:
                case SUB_COMMAND_GROUP:
                    findFocused(option.getArray("options"));
                    break;
                default:
                    if (option.getBoolean("focused")) {
                        OptionMapping opt = getOption(option.getString("name"));
                        focused = new AutoCompleteQuery(opt);
                        break;
                    }
            }
        }
    }

    @NotNull
    @Override
    public AutoCompleteQuery getFocusedOption() {
        return focused;
    }

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannelUnion getChannel() {
        return (MessageChannelUnion) super.getChannel();
    }

    @Override
    public CommandInteractionPayload getCommandPayload() {
        return payload;
    }

    @NotNull
    @Override
    public AutoCompleteCallbackAction replyChoices(@NotNull Collection<Command.Choice> choices) {
        return new AutoCompleteCallbackActionImpl(this, focused.getType()).addChoices(choices);
    }
}
