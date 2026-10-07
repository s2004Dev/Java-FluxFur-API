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

package lonter.jfa.api.interactions.components.buttons;

import lonter.jfa.api.components.buttons.Button;
import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.api.components.tree.MessageComponentTree;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.events.interaction.component.ButtonInteractionEvent;
import lonter.jfa.api.interactions.components.ComponentInteraction;
import lonter.jfa.api.requests.RestAction;

import java.util.Collection;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interaction on a {@link Button} component.
 *
 * @see ButtonInteractionEvent
 */
public interface ButtonInteraction extends ComponentInteraction {
    @NotNull
    @Override
    default Button getComponent() {
        return getButton();
    }

    /**
     * The {@link Button} this interaction belongs to.
     *
     * @return The {@link Button}
     *
     * @see    #getComponentId()
     */
    @NotNull
    Button getButton();

    /**
     * Update the button with a new button instance.
     *
     * <p>If this interaction is already acknowledged this will use {@link #getHook()}
     * and otherwise {@link #editComponents(Collection)} directly to acknowledge the interaction.
     *
     * @param  newButton
     *         The new button to use, or null to remove this button from the message entirely
     *
     * @return {@link RestAction}
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Void> editButton(@Nullable Button newButton) {
        Message message = getMessage();
        MessageComponentTree newTree =
                message.getComponentTree().replace(ComponentReplacer.byUniqueId(getButton(), newButton));

        if (isAcknowledged()) {
            return getHook()
                    .editMessageComponentsById(message.getId(), newTree.getComponents())
                    .useComponentsV2(message.isUsingComponentsV2())
                    .map(it -> null);
        } else {
            return editComponents(newTree.getComponents())
                    .useComponentsV2(message.isUsingComponentsV2())
                    .map(it -> null);
        }
    }
}
