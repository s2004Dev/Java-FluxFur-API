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

package lonter.jfa.api.interactions.components.selections;

import lonter.jfa.api.components.selections.SelectOption;
import lonter.jfa.api.components.selections.StringSelectMenu;
import lonter.jfa.api.events.interaction.component.StringSelectInteractionEvent;
import lonter.jfa.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Component Interaction for a {@link StringSelectMenu}.
 *
 * @see StringSelectInteractionEvent
 */
public interface StringSelectInteraction extends SelectMenuInteraction<String, StringSelectMenu> {
    /**
     * The selected values.
     * <br>These are defined in the individual {@link SelectOption SelectOptions}.
     *
     * @return {@link List} of {@link SelectOption#getValue()}
     */
    @NotNull
    @Unmodifiable
    List<String> getValues();

    /**
     * This resolves the selected {@link #getValues() values} to the representative {@link SelectOption SelectOption} instances.
     * <br>It is recommended to check {@link #getValues()} directly instead of using the options.
     *
     * @return Immutable {@link List} of the selected options
     */
    @NotNull
    @Unmodifiable
    default List<SelectOption> getSelectedOptions() {
        StringSelectMenu menu = getComponent();
        List<String> values = getValues();
        return menu.getOptions().stream()
                .filter(it -> values.contains(it.getValue()))
                .collect(Helpers.toUnmodifiableList());
    }
}
