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

package lonter.jfa.api.interactions.callbacks;

import lonter.jfa.api.interactions.Interaction;
import lonter.jfa.api.interactions.commands.Command;
import lonter.jfa.api.interactions.commands.OptionType;
import lonter.jfa.api.interactions.commands.build.OptionData;
import lonter.jfa.api.requests.restaction.interactions.AutoCompleteCallbackAction;
import lonter.jfa.internal.utils.Checks;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Interactions which allow auto-complete suggestion replies.
 *
 * <p>Auto-complete must be enabled on {@link OptionData options} via {@link OptionData#setAutoComplete(boolean)}.
 * When a user inputs auto-complete options, you will receive these interactions and can provide up to {@value OptionData#MAX_CHOICES} suggestions for the user to pick from.
 * You can also use {@link #replyChoices(Command.Choice...)} without parameters to acknowledge the interaction with 0 choices.
 */
public interface IAutoCompleteCallback extends Interaction {
    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  choices
     *         The choice suggestions to present to the user, 0-{@value OptionData#MAX_CHOICES} choices
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If any of the choice names are empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>If the option type is incompatible with the choice type</li>
     *             <li>If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *             <li>If the string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    AutoCompleteCallbackAction replyChoices(@NotNull Collection<Command.Choice> choices);

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  choices
     *         The choice suggestions to present to the user, 0-{@value OptionData#MAX_CHOICES} choices
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If any of the choice names are empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>If the option type is incompatible with the choice type</li>
     *             <li>If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *             <li>If the string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoices(@NotNull Command.Choice... choices) {
        Checks.noneNull(choices, "Choice");
        return replyChoices(Arrays.asList(choices));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  name
     *         The choice name to show to the user, 1-{@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     * @param  value
     *         The choice value, 1-{@value OptionData#MAX_CHOICE_VALUE_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If the choice name is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>If the option type is not {@link OptionType#STRING}</li>
     *             <li>If the string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoice(@NotNull String name, @NotNull String value) {
        return replyChoices(new Command.Choice(name, value));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  name
     *         The choice name to show to the user, 1-{@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     * @param  value
     *         The choice value, must be between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If the choice name is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>If the option type is incompatible with the choice type</li>
     *             <li>If the value of is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoice(@NotNull String name, long value) {
        return replyChoices(new Command.Choice(name, value));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  name
     *         The choice name to show to the user, 1-{@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     * @param  value
     *         The choice value, must be between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If the choice name is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>If the option type is incompatible with the choice type</li>
     *             <li>If the value of is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoice(@NotNull String name, double value) {
        return replyChoices(new Command.Choice(name, value));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The provided strings will be used as value and name for the {@link lonter.jfa.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user, each limited to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If any of the choice names are empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>If the string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceStrings(@NotNull String... choices) {
        return replyChoices(
                Arrays.stream(choices).map(it -> new Command.Choice(it, it)).collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The provided strings will be used as value and name for the {@link lonter.jfa.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user, each limited to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If any of the choice names are empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>If the string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceStrings(@NotNull Collection<String> choices) {
        return replyChoices(
                choices.stream().map(it -> new Command.Choice(it, it)).collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The string values of the provided longs will be used as value and name for the {@link lonter.jfa.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If the option type is incompatible with the choice type</li>
     *             <li>If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceLongs(@NotNull long... choices) {
        return replyChoices(Arrays.stream(choices)
                .mapToObj(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The string values of the provided longs will be used as value and name for the {@link lonter.jfa.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If the option type is incompatible with the choice type</li>
     *             <li>If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceLongs(@NotNull Collection<Long> choices) {
        return replyChoices(choices.stream()
                .map(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The string values of the provided doubles will be used as value and name for the {@link lonter.jfa.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If the option type is incompatible with the choice type</li>
     *             <li>If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceDoubles(@NotNull double... choices) {
        return replyChoices(Arrays.stream(choices)
                .mapToObj(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The string values of the provided doubles will be used as value and name for the {@link lonter.jfa.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value OptionData#MAX_CHOICES} choices are added</li>
     *             <li>If the option type is incompatible with the choice type</li>
     *             <li>If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @NotNull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceDoubles(@NotNull Collection<Double> choices) {
        return replyChoices(choices.stream()
                .map(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }
}
