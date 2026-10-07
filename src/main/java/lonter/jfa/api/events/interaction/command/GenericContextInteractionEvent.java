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
import lonter.jfa.api.interactions.commands.context.ContextInteraction;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link ContextInteraction} was used.
 *
 * <p><b>Requirements</b><br>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://fluxer.com/developers/applications" target="_blank">Fluxer Developers Portal</a>.
 */
public class GenericContextInteractionEvent<T> extends GenericCommandInteractionEvent implements ContextInteraction<T> {
    public GenericContextInteractionEvent(
            @NotNull JFA api, long responseNumber, @NotNull ContextInteraction<T> interaction) {
        super(api, responseNumber, interaction);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public ContextInteraction<T> getInteraction() {
        return (ContextInteraction<T>) super.getInteraction();
    }

    @NotNull
    @Override
    public ContextTarget getTargetType() {
        return getInteraction().getTargetType();
    }

    @NotNull
    @Override
    public T getTarget() {
        return getInteraction().getTarget();
    }
}
