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

package lonter.jfa.api.components;

import lonter.jfa.api.components.attribute.ICustomId;
import lonter.jfa.api.components.attribute.IDisableable;
import lonter.jfa.api.interactions.components.ComponentInteraction;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Component which supports interactions via {@link ComponentInteraction}.
 */
public interface ActionComponent extends IDisableable, ICustomId {
    @NotNull
    @Override
    ActionComponent withUniqueId(int uniqueId);

    /**
     * Whether this action component is disabled.
     *
     * <p>You can use {@link #asDisabled()} or {@link #asEnabled()} to create enabled/disabled instances.
     *
     * @return True, if this button is disabled
     */
    boolean isDisabled();

    /**
     * Returns a copy of this component with {@link #isDisabled()} set to true.
     *
     * <p><b>Note:</b> Components in modals cannot be disabled.
     *
     * @return New disabled component instance
     */
    @NotNull
    @CheckReturnValue
    default ActionComponent asDisabled() {
        return (ActionComponent) IDisableable.super.asDisabled();
    }

    /**
     * Returns a copy of this component with {@link #isDisabled()} set to false.
     *
     * @return New enabled component instance
     */
    @NotNull
    @CheckReturnValue
    default ActionComponent asEnabled() {
        return (ActionComponent) IDisableable.super.asEnabled();
    }

    /**
     * Returns a copy of this component with {@link #isDisabled()} set to the provided value.
     *
     * <p><b>Note:</b> Components in modals cannot be disabled.
     *
     * @param  disabled
     *         True, if this component should be disabled
     *
     * @throws UnsupportedOperationException
     *         If this component type cannot be disabled
     *
     * @return New enabled/disabled component instance
     */
    @NotNull
    @CheckReturnValue
    ActionComponent withDisabled(boolean disabled);
}
