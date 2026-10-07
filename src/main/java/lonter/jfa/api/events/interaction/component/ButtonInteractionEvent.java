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
import lonter.jfa.api.components.buttons.Button;
import lonter.jfa.api.interactions.components.buttons.ButtonInteraction;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a custom {@link Button} on one of the bots messages was clicked by a user.
 *
 * <p>This fires when a user clicks one of the custom buttons attached to a bot or webhook message.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://fluxer.com/developers/applications" target="_blank">Fluxer Developers Portal</a>.
 */
public class ButtonInteractionEvent extends GenericComponentInteractionCreateEvent implements ButtonInteraction {
    private final ButtonInteraction interaction;

    public ButtonInteractionEvent(@NotNull JFA api, long responseNumber, @NotNull ButtonInteraction interaction) {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @NotNull
    @Override
    public ButtonInteraction getInteraction() {
        return interaction;
    }

    @NotNull
    @Override
    public Button getComponent() {
        return interaction.getComponent();
    }

    @NotNull
    @Override
    public Button getButton() {
        return interaction.getButton();
    }
}
