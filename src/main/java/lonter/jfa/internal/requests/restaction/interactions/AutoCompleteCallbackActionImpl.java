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

package lonter.jfa.internal.requests.restaction.interactions;

import lonter.jfa.api.interactions.callbacks.IAutoCompleteCallback;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.OptionType;
import lonter.jfa.api.interactions.commands.build.OptionData;
import lonter.jfa.api.requests.restaction.interactions.AutoCompleteCallbackAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.interactions.InteractionImpl;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

public class AutoCompleteCallbackActionImpl extends InteractionCallbackImpl<Void>
        implements AutoCompleteCallbackAction {
    private final OptionType type;
    private final List<Command.Choice> choices = new ArrayList<>(26);

    public AutoCompleteCallbackActionImpl(IAutoCompleteCallback interaction, OptionType type) {
        super((InteractionImpl) interaction);
        this.type = type;
    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return type;
    }

    @NotNull
    @Override
    public AutoCompleteCallbackAction addChoices(@NotNull Collection<Command.Choice> choices) {
        Checks.noneNull(choices, "Choices");
        Checks.check(
                choices.size() + this.choices.size() <= OptionData.MAX_CHOICES,
                "Can only reply with up to %d choices. Limit your suggestions!",
                OptionData.MAX_CHOICES);
        for (Command.Choice choice : choices) {
            Checks.inRange(choice.getName(), 1, OptionData.MAX_CHOICE_NAME_LENGTH, "Choice name");

            switch (type) {
                case INTEGER:
                    Checks.check(
                            choice.getType() == OptionType.INTEGER,
                            "Choice of type %s cannot be converted to INTEGER",
                            choice.getType());
                    long valueLong = choice.getAsLong();
                    Checks.check(
                            valueLong <= OptionData.MAX_POSITIVE_NUMBER,
                            "Choice value cannot be larger than %f Provided: %d",
                            OptionData.MAX_POSITIVE_NUMBER,
                            valueLong);
                    Checks.check(
                            valueLong >= OptionData.MIN_NEGATIVE_NUMBER,
                            "Choice value cannot be smaller than %f. Provided: %d",
                            OptionData.MIN_NEGATIVE_NUMBER,
                            valueLong);
                    break;
                case NUMBER:
                    Checks.check(
                            choice.getType() == OptionType.NUMBER || choice.getType() == OptionType.INTEGER,
                            "Choice of type %s cannot be converted to NUMBER",
                            choice.getType());
                    double valueDouble = choice.getAsDouble();
                    Checks.check(
                            valueDouble <= OptionData.MAX_POSITIVE_NUMBER,
                            "Choice value cannot be larger than %f Provided: %f",
                            OptionData.MAX_POSITIVE_NUMBER,
                            valueDouble);
                    Checks.check(
                            valueDouble >= OptionData.MIN_NEGATIVE_NUMBER,
                            "Choice value cannot be smaller than %f. Provided: %f",
                            OptionData.MIN_NEGATIVE_NUMBER,
                            valueDouble);
                    break;
                case STRING:
                    // String can be any type, we just toString it
                    String valueString = choice.getAsString();
                    Checks.inRange(valueString, 1, OptionData.MAX_CHOICE_VALUE_LENGTH, "Choice value");
                    break;
            }
        }
        this.choices.addAll(choices);
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject data = DataObject.empty();
        DataArray array = DataArray.empty();
        choices.forEach(choice -> array.add(choice.toData(type)));
        data.put("choices", array);
        return getRequestBody(DataObject.empty()
                .put("type", ResponseType.COMMAND_AUTOCOMPLETE_CHOICES.getRaw())
                .put("data", data));
    }

    @NotNull
    @Override
    public AutoCompleteCallbackAction setCheck(BooleanSupplier checks) {
        return (AutoCompleteCallbackAction) super.setCheck(checks);
    }

    @NotNull
    @Override
    public AutoCompleteCallbackAction deadline(long timestamp) {
        return (AutoCompleteCallbackAction) super.deadline(timestamp);
    }
}
