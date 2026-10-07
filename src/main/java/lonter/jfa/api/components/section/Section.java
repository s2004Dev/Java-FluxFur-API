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

package lonter.jfa.api.components.section;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.IComponentUnion;
import lonter.jfa.api.components.MessageTopLevelComponent;
import lonter.jfa.api.components.attribute.IDisableable;
import lonter.jfa.api.components.container.ContainerChildComponent;
import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.api.components.replacer.IReplaceable;
import lonter.jfa.api.components.utils.ComponentIterator;
import lonter.jfa.api.utils.messages.MessageRequest;
import lonter.jfa.internal.components.section.SectionImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Component which contains the main content on the left and an accessory on the right.
 *
 * <p>This can contain up to {@value #MAX_COMPONENTS} {@link SectionContentComponent}.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} needs to be enabled!
 *
 * @see SectionContentComponent
 * @see SectionContentComponentUnion
 * @see SectionAccessoryComponent
 * @see SectionAccessoryComponentUnion
 */
public interface Section extends MessageTopLevelComponent, ContainerChildComponent, IReplaceable, IDisableable {
    /**
     * How many {@link SectionContentComponent} can be in this section. ({@value})
     */
    int MAX_COMPONENTS = 3;

    /**
     * Constructs a new {@link Section} from the given accessory and components.
     *
     * @param  accessory
     *         The accessory of this section
     * @param  components
     *         The components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value #MAX_COMPONENTS} components are provided</li>
     *             <li>If one of the components is {@linkplain IComponentUnion#isUnknownComponent() unknown}</li>
     *         </ul>
     *
     * @return The new {@link Section}
     */
    @NotNull
    static Section of(
            @NotNull SectionAccessoryComponent accessory,
            @NotNull Collection<? extends SectionContentComponent> components) {
        return SectionImpl.validated(accessory, components);
    }

    /**
     * Constructs a new {@link Section} from the given accessory and components.
     *
     * @param  accessory
     *         The accessory of this section
     * @param  component
     *         The component to add
     * @param  components
     *         Additional components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value #MAX_COMPONENTS} components are provided</li>
     *             <li>If one of the components is {@linkplain IComponentUnion#isUnknownComponent() unknown}</li>
     *         </ul>
     *
     * @return The new {@link Section}
     */
    @NotNull
    static Section of(
            @NotNull SectionAccessoryComponent accessory,
            @NotNull SectionContentComponent component,
            @NotNull SectionContentComponent... components) {
        Checks.notNull(component, "Component");
        Checks.noneNull(components, "Components");
        return of(accessory, Helpers.mergeVararg(component, components));
    }

    @Override
    default boolean isMessageCompatible() {
        if (!getType().isMessageCompatible()) {
            return false;
        }

        return getContentComponents().stream().allMatch(Component::isMessageCompatible)
                && getAccessory().isMessageCompatible();
    }

    @Override
    default boolean isModalCompatible() {
        if (!getType().isModalCompatible()) {
            return false;
        }

        return getContentComponents().stream().allMatch(Component::isModalCompatible)
                && getAccessory().isModalCompatible();
    }

    @NotNull
    @Override
    Section replace(@NotNull ComponentReplacer replacer);

    @NotNull
    @Override
    @CheckReturnValue
    Section withUniqueId(int uniqueId);

    @NotNull
    @Override
    @CheckReturnValue
    default Section withDisabled(boolean disabled) {
        return replace(ComponentReplacer.of(IDisableable.class, c -> true, c -> c.withDisabled(disabled)));
    }

    @NotNull
    @Override
    @CheckReturnValue
    default Section asDisabled() {
        return (Section) IDisableable.super.asDisabled();
    }

    @NotNull
    @Override
    @CheckReturnValue
    default Section asEnabled() {
        return (Section) IDisableable.super.asEnabled();
    }

    /**
     * Creates a new {@link Section} with the specified content components.
     *
     * @param  components
     *         The new content components
     *
     * @throws IllegalArgumentException
     *         If the provided components are {@code null} or contains {@code null}
     *
     * @return The new {@link Section}
     */
    @NotNull
    @CheckReturnValue
    Section withContentComponents(@NotNull Collection<? extends SectionContentComponent> components);

    /**
     * Creates a new {@link Section} with the specified content components.
     *
     * @param  component
     *         The first new content component
     * @param  components
     *         Additional new content components
     *
     * @throws IllegalArgumentException
     *         If the provided components are {@code null} or contains {@code null}
     *
     * @return The new {@link Section}
     */
    @NotNull
    @CheckReturnValue
    default Section withContentComponents(
            @NotNull SectionContentComponent component, @NotNull SectionContentComponent... components) {
        Checks.notNull(component, "Component");
        Checks.notNull(components, "Components");
        return withContentComponents(Helpers.mergeVararg(component, components));
    }

    /**
     * Creates a new {@link Section} with the specified accessory.
     *
     * @param  accessory
     *         The new accessory
     *
     * @throws IllegalArgumentException
     *         If the provided accessory is {@code null}
     *
     * @return The new {@link Section}
     */
    @NotNull
    @CheckReturnValue
    Section withAccessory(@NotNull SectionAccessoryComponent accessory);

    /**
     * Returns an immutable list with the components contained by this section.
     *
     * @return {@link List} of {@link SectionContentComponentUnion} in this section
     */
    @NotNull
    @Unmodifiable
    List<SectionContentComponentUnion> getContentComponents();

    /**
     * The accessory of this section.
     *
     * @return Accessory of this section
     */
    @NotNull
    SectionAccessoryComponentUnion getAccessory();

    @Override
    default boolean isDisabled() {
        SectionAccessoryComponentUnion accessory = getAccessory();
        if (accessory instanceof IDisableable && ((IDisableable) accessory).isEnabled()) {
            return false;
        }

        return ComponentIterator.createStream(getContentComponents())
                .filter(IDisableable.class::isInstance)
                .map(IDisableable.class::cast)
                .allMatch(IDisableable::isDisabled);
    }

    @Override
    default boolean isEnabled() {
        SectionAccessoryComponentUnion accessory = getAccessory();
        if (accessory instanceof IDisableable && ((IDisableable) accessory).isDisabled()) {
            return false;
        }

        return ComponentIterator.createStream(getContentComponents())
                .filter(IDisableable.class::isInstance)
                .map(IDisableable.class::cast)
                .allMatch(IDisableable::isEnabled);
    }
}
