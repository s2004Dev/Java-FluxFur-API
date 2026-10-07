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

import lonter.jfa.api.components.ActionComponent;
import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.MessageTopLevelComponent;
import lonter.jfa.api.components.attribute.IDisableable;
import lonter.jfa.api.components.buttons.Button;
import lonter.jfa.api.components.container.ContainerChildComponent;
import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.api.components.replacer.IReplaceable;
import lonter.jfa.internal.components.actionrow.ActionRowImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * One row of action components.
 *
 * @see ActionRowChildComponent
 */
public interface ActionRow extends MessageTopLevelComponent, ContainerChildComponent, IReplaceable, IDisableable {
    /**
     * Create one row of {@link ActionRowChildComponent components}.
     * <br>You cannot currently mix different types of components and each type has its own maximum defined by {@link #getMaxAllowed(Type)}.
     *
     * @param  components
     *         The components for this action row
     *
     * @throws IllegalArgumentException
     *         If anything is null, empty, or an invalid number of components are provided
     *
     * @return The action row
     */
    @NotNull
    static ActionRow of(@NotNull Collection<? extends ActionRowChildComponent> components) {
        return ActionRowImpl.validated(components);
    }

    /**
     * Create one row of {@link ActionRowChildComponent components}.
     * <br>You cannot currently mix different types of components and each type has its own maximum defined by {@link #getMaxAllowed(Type)}.
     *
     * @param  component
     *         The first component for this action row
     * @param  components
     *         Additional components for this action row
     *
     * @throws IllegalArgumentException
     *         If anything is null, empty, or an invalid number of components are provided
     *
     * @return The action row
     */
    @NotNull
    static ActionRow of(@NotNull ActionRowChildComponent component, @NotNull ActionRowChildComponent... components) {
        Checks.notNull(component, "Component");
        Checks.notNull(components, "Components");
        return of(Helpers.mergeVararg(component, components));
    }

    /**
     * Partitions the provided {@link ActionRowChildComponent components} into a list of ActionRow instances.
     * <br>This will split the provided components by {@link #getMaxAllowed(Type)} and create homogeneously typed rows,
     * meaning they will not have mixed component types.
     *
     * <p><b>Example</b>
     * {@snippet lang="java":
     * List<ActionRowChildComponent> components = Arrays.asList(
     *   Button.primary("id1", "Hello"),
     *   Button.secondary("id2", "World"),
     *   SelectMenu.create("menu:id").build()
     * );
     *
     * List<ActionRow> partitioned = ActionRow.partition(components);
     * // partitioned[0] = ActionRow(button, button)
     * // partitioned[1] = ActionRow(selectMenu)
     * }
     *
     * @param  components
     *         The components to partition
     *
     * @throws IllegalArgumentException
     *         If null is provided or there is no components
     *
     * @return {@link List} of {@link ActionRow}
     */
    @NotNull
    static List<ActionRow> partitionOf(@NotNull Collection<? extends ActionRowChildComponent> components) {
        return ActionRowImpl.partitionOf(components);
    }

    /**
     * Partitions the provided {@link ActionRowChildComponent components} into a list of ActionRow instances.
     * <br>This will split the provided components by {@link #getMaxAllowed(Type)} and create homogeneously typed rows,
     * meaning they will not have mixed component types.
     *
     * <p><b>Example</b>
     * {@snippet lang="java":
     * List<ActionRowChildComponent> components = Arrays.asList(
     *   Button.primary("id1", "Hello"),
     *   Button.secondary("id2", "World"),
     *   SelectMenu.create("menu:id").build()
     * );
     *
     * List<ActionRow> partitioned = ActionRow.partition(components);
     * // partitioned[0] = ActionRow(button, button)
     * // partitioned[1] = ActionRow(selectMenu)
     * }
     *
     * @param  component
     *         The first component to partition
     * @param  components
     *         Additional components to partition
     *
     * @throws IllegalArgumentException
     *         If null is provided or there is no components
     *
     * @return {@link List} of {@link ActionRow}
     */
    @NotNull
    static List<ActionRow> partitionOf(
            @NotNull ActionRowChildComponent component, @NotNull ActionRowChildComponent... components) {
        Checks.notNull(component, "Component");
        Checks.notNull(components, "Components");
        return partitionOf(Helpers.mergeVararg(component, components));
    }

    /**
     * How many of components of the provided type can be added to a single {@link ActionRow}.
     *
     * @return The maximum amount an action row can contain
     */
    static int getMaxAllowed(@NotNull Component.Type type) {
        switch (type) {
            case BUTTON:
                return 5;
            case STRING_SELECT:
            case USER_SELECT:
            case ROLE_SELECT:
            case MENTIONABLE_SELECT:
            case CHANNEL_SELECT:
                return 1;
            default:
                return 0;
        }
    }

    @NotNull
    @Override
    @CheckReturnValue
    ActionRow withUniqueId(int uniqueId);

    /**
     * Returns an unmodifiable list of the components contained in this action row.
     *
     * @return Unmodifiable {@link List} of {@link ActionRowChildComponentUnion} contained in this action row
     */
    @NotNull
    @Unmodifiable
    List<ActionRowChildComponentUnion> getComponents();

    /**
     * Returns an immutable list of {@link ActionComponent ActionComponents} in this row.
     *
     * @return Immutable {@link List} copy of {@link ActionComponent ActionComponents} in this row
     */
    @NotNull
    @Unmodifiable
    default List<ActionComponent> getActionComponents() {
        return getComponents().stream()
                .filter(ActionComponent.class::isInstance)
                .map(ActionComponent.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Returns an immutable list of {@link Button Buttons} in this row.
     *
     * @return Immutable {@link List} of {@link Button Buttons}
     */
    @NotNull
    @Unmodifiable
    default List<Button> getButtons() {
        return getComponents().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .collect(Helpers.toUnmodifiableList());
    }

    @Override
    default boolean isMessageCompatible() {
        if (!getType().isMessageCompatible()) {
            return false;
        }

        return getComponents().stream().allMatch(Component::isMessageCompatible);
    }

    @Override
    default boolean isModalCompatible() {
        if (!getType().isModalCompatible()) {
            return false;
        }

        return getComponents().stream().allMatch(Component::isModalCompatible);
    }

    @NotNull
    @Override
    @CheckReturnValue
    ActionRow replace(@NotNull ComponentReplacer replacer);

    @Override
    default boolean isDisabled() {
        return getActionComponents().stream().allMatch(ActionComponent::isDisabled);
    }

    @Override
    default boolean isEnabled() {
        return getActionComponents().stream().noneMatch(ActionComponent::isDisabled);
    }

    @NotNull
    @Override
    @CheckReturnValue
    default ActionRow withDisabled(boolean disabled) {
        return replace(ComponentReplacer.of(IDisableable.class, c -> true, c -> c.withDisabled(disabled)));
    }

    @NotNull
    @Override
    default ActionRow asDisabled() {
        return (ActionRow) IDisableable.super.asDisabled();
    }

    @NotNull
    @Override
    default ActionRow asEnabled() {
        return (ActionRow) IDisableable.super.asEnabled();
    }

    /**
     * Creates a new {@link ActionRow} with the specified components.
     *
     * @param  components
     *         The new components
     *
     * @throws IllegalArgumentException
     *         If the provided components are {@code null} or contains {@code null}
     *
     * @return The new {@link ActionRow}
     */
    @NotNull
    @CheckReturnValue
    ActionRow withComponents(@NotNull Collection<? extends ActionRowChildComponent> components);

    /**
     * Creates a new {@link ActionRow} with the specified components.
     *
     * @param  component
     *         The first new component
     * @param  components
     *         Additional new components
     *
     * @throws IllegalArgumentException
     *         If the provided components are {@code null} or contains {@code null}
     *
     * @return The new {@link ActionRow}
     */
    @NotNull
    @CheckReturnValue
    default ActionRow withComponents(
            @NotNull ActionRowChildComponent component, @NotNull ActionRowChildComponent... components) {
        Checks.notNull(component, "Component");
        Checks.notNull(components, "Components");
        return withComponents(Helpers.mergeVararg(component, components));
    }
}
