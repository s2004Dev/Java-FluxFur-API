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

package lonter.jfa.api.components.label;

import lonter.jfa.api.components.ModalTopLevelComponent;
import lonter.jfa.internal.components.label.LabelImpl;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Component that contains a label, an optional description, and a {@linkplain LabelChildComponent child component}.
 *
 * <p>Labels are used as top-level components inside {@link lonter.jfa.api.modals.Modal Modals},
 * and cannot be used in Messages.
 *
 * @see LabelChildComponent
 * @see LabelChildComponentUnion
 */
public interface Label extends ModalTopLevelComponent {
    /**
     * The maximum length a label can have. ({@value})
     */
    int LABEL_MAX_LENGTH = 45;

    /**
     * The maximum length a label description can have. ({@value})
     */
    int DESCRIPTION_MAX_LENGTH = 100;

    /**
     * Constructs a new {@link Label} using the provided label, description and child component.
     *
     * @param  label
     *         The label of the Label
     * @param  description
     *         The description of the Label. May be {@code null}
     * @param  child
     *         The {@link LabelChildComponent} that should be contained by the Label
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code label} is {@code null}, blank, or longer than {@link #LABEL_MAX_LENGTH}</li>
     *             <li>If {@code description} is not {@code null} and is blank or longer than {@link #DESCRIPTION_MAX_LENGTH}</li>
     *             <li>If {@code child} is {@code null}</li>
     *         </ul>
     *
     * @return The new {@link Label}
     */
    @NotNull
    static Label of(@NotNull String label, @Nullable String description, @NotNull LabelChildComponent child) {
        return LabelImpl.validated(label, description, child);
    }

    /**
     * Constructs a new {@link Label} using the provided label and child component.
     *
     * @param  label
     *         The label of the Label
     * @param  child
     *         The {@link LabelChildComponent} that should be contained by the Label
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code label} is {@code null}, blank, or longer than {@link #LABEL_MAX_LENGTH}</li>
     *             <li>If {@code child} is {@code null}</li>
     *         </ul>
     *
     * @return The new {@link Label}
     */
    @NotNull
    static Label of(@NotNull String label, @NotNull LabelChildComponent child) {
        return of(label, null, child);
    }

    /**
     * Returns a copy of this Label with the provided label.
     *
     * @param  label
     *         The label
     *
     * @throws IllegalArgumentException
     *         If {@code label} is {@code null}, blank, or longer than {@link #LABEL_MAX_LENGTH}
     *
     * @return The new Label
     */
    @NotNull
    Label withLabel(@NotNull String label);

    /**
     * Returns a copy of this Label with the provided description.
     *
     * @param  description
     *         The description
     *
     * @throws IllegalArgumentException
     *         If {@code description} is not {@code null} and is blank or longer than {@link #DESCRIPTION_MAX_LENGTH}
     *
     * @return The new Label
     */
    @NotNull
    Label withDescription(@Nullable String description);

    /**
     * Returns a copy of this Label with the provided {@linkplain LabelChildComponent child component}.
     *
     * @param  child
     *         The child component
     *
     * @throws IllegalArgumentException
     *         If {@code child} is {@code null}
     *
     * @return The new Label
     */
    @NotNull
    Label withChild(@NotNull LabelChildComponent child);

    /**
     * The label.
     *
     * @return The label
     */
    @NotNull
    String getLabel();

    /**
     * The description of the Label. May be {@code null}.
     *
     * @return The description
     */
    @Nullable
    String getDescription();

    /**
     * The {@linkplain LabelChildComponentUnion child component} contained by this Label.
     *
     * @return The child component
     */
    @NotNull
    LabelChildComponentUnion getChild();

    @NotNull
    @Override
    @CheckReturnValue
    Label withUniqueId(int uniqueId);
}
