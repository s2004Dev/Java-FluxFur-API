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

package lonter.jfa.api.components.actionrow;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.IComponentUnion;
import lonter.jfa.api.components.UnknownComponent;
import lonter.jfa.api.components.buttons.Button;
import lonter.jfa.api.components.selections.EntitySelectMenu;
import lonter.jfa.api.components.selections.StringSelectMenu;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a union of {@link ActionRowChildComponent ActionRowChildComponents} that can be one of:
 * <ul>
 *     <li>{@link Button}</li>
 *     <li>{@link StringSelectMenu}</li>
 *     <li>{@link EntitySelectMenu}</li>
 *     <li>{@link UnknownComponent}, detectable via {@link #isUnknownComponent()}</li>
 * </ul>
 */
public interface ActionRowChildComponentUnion extends ActionRowChildComponent, IComponentUnion {
    /**
     * Casts this union to a {@link Button}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang="java":
     * //These are the same!
     * Button button = union.asButton();
     * Button button2 = (Button) union;
     * }
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#BUTTON} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof Button</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link Button}.
     *
     * @return The component as a {@link Button}
     */
    @NotNull
    Button asButton();

    /**
     * Casts this union to a {@link StringSelectMenu}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang="java":
     * //These are the same!
     * StringSelectMenu stringSelectMenu = union.asStringSelectMenu();
     * StringSelectMenu stringSelectMenu2 = (Button) union;
     * }
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#STRING_SELECT} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof StringSelectMenu</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link StringSelectMenu}.
     *
     * @return The component as a {@link StringSelectMenu}
     */
    @NotNull
    StringSelectMenu asStringSelectMenu();

    /**
     * Casts this union to a {@link StringSelectMenu}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang="java":
     * //These are the same!
     * EntitySelectMenu entitySelectMenu = union.asEntitySelectMenu();
     * EntitySelectMenu entitySelectMenu2 = (Button) union;
     * }
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#MENTIONABLE_SELECT} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof EntitySelectMenu</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link EntitySelectMenu}.
     *
     * @return The component as a {@link EntitySelectMenu}
     */
    @NotNull
    EntitySelectMenu asEntitySelectMenu();

    @NotNull
    @Override
    ActionRowChildComponentUnion withUniqueId(int uniqueId);
}
