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
import lonter.jfa.api.components.selections.StringSelectMenu;
import lonter.jfa.api.interactions.components.selections.StringSelectInteraction;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a custom {@link StringSelectMenu} on one of the bots messages was used by a user.
 *
 * <p>This fires when a user selects the options on one of the custom select menus attached to a bot or webhook message.
 * Use {@link #getValues()} or {@link #getSelectedOptions()} to handle the selected options.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://fluxer.com/developers/applications" target="_blank">Fluxer Developers Portal</a>.
 *
 * @see EntitySelectInteractionEvent
 */
public class StringSelectInteractionEvent extends GenericSelectMenuInteractionEvent<String, StringSelectMenu>
        implements StringSelectInteraction {
    private final StringSelectInteraction interaction;

    public StringSelectInteractionEvent(
            @NotNull JFA api, long responseNumber, @NotNull StringSelectInteraction interaction) {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @NotNull
    @Override
    public StringSelectInteraction getInteraction() {
        return interaction;
    }
}
