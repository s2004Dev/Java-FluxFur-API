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

package lonter.jfa.api.interactions;

import lonter.jfa.api.interactions.commands.CommandAutoCompleteInteraction;
import lonter.jfa.api.interactions.commands.OptionMapping;
import lonter.jfa.api.interactions.commands.OptionType;
import lonter.jfa.internal.utils.EntityString;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * The query input for an {@link lonter.jfa.api.interactions.callbacks.IAutoCompleteCallback auto-complete interaction}.
 *
 * <p>The {@link #getValue() value} of such a query may not be a valid instance of the expected {@link #getType() type}.
 * Fluxer does not do any validation for auto-complete queries. However, you are required to reply with the correct type.
 */
public class AutoCompleteQuery {
    private final String name;
    private final String value;
    private final OptionType type;

    public AutoCompleteQuery(@NotNull OptionMapping option) {
        this.name = option.getName();
        this.value = option.getAsString();
        this.type = option.getType();
    }

    /**
     * The name of the input field, usually an option name in {@link CommandAutoCompleteInteraction}.
     *
     * @return The option name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * The query value that the user is currently typing.
     *
     * <p>This is not validated and may not be a valid value for an actual command.
     * For instance, a user may input invalid numbers for {@link OptionType#NUMBER}.
     *
     * @return The current auto-completable query value
     */
    @NotNull
    public String getValue() {
        return value;
    }

    /**
     * The expected option type for this query.
     *
     * @return The option type expected from this auto-complete response
     */
    @NotNull
    public OptionType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AutoCompleteQuery)) {
            return false;
        }
        AutoCompleteQuery query = (AutoCompleteQuery) obj;
        return type == query.type && name.equals(query.name) && value.equals(query.value);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .setType(getType())
                .addMetadata("name", getName())
                .addMetadata("value", getValue())
                .toString();
    }
}
