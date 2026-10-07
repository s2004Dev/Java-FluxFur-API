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

import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.api.components.selections.SelectMenu;
import lonter.jfa.api.components.tree.MessageComponentTree;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import lonter.jfa.api.interactions.components.ComponentInteraction;
import lonter.jfa.api.requests.RestAction;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Component Interaction for a {@link SelectMenu}.
 *
 * @param <T>
 *        The select menu value type
 * @param <S>
 *        The type of select menu
 *
 * @see GenericSelectMenuInteractionEvent
 * @see EntitySelectInteraction
 * @see StringSelectInteraction
 */
public interface SelectMenuInteraction<T, S extends SelectMenu> extends ComponentInteraction {
    @NotNull
    @Override
    S getComponent();

    /**
     * The {@link SelectMenu} this interaction belongs to.
     *
     * @return The {@link SelectMenu}
     *
     * @see    #getComponentId()
     */
    @NotNull
    default S getSelectMenu() {
        return getComponent();
    }

    /**
     * The provided selection.
     *
     * @return {@link List} of {@link T}
     */
    @NotNull
    List<T> getValues();

    /**
     * Update the select menu with a new select menu instance.
     *
     * <p>If this interaction is already acknowledged this will use {@link #getHook()}
     * and otherwise {@link #editComponents(Collection)} directly to acknowledge the interaction.
     *
     * @param  newMenu
     *         The new select menu to use, or null to remove this menu from the message entirely
     *
     * @return {@link RestAction}
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Void> editSelectMenu(@Nullable SelectMenu newMenu) {
        Message message = getMessage();
        MessageComponentTree newTree =
                message.getComponentTree().replace(ComponentReplacer.byUniqueId(getSelectMenu(), newMenu));

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
