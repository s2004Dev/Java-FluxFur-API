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

package lonter.jfa.api.components.tree;

import lonter.jfa.api.components.ModalTopLevelComponent;
import lonter.jfa.api.components.ModalTopLevelComponentUnion;
import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.internal.components.tree.ModalComponentTreeImpl;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Specialization of {@link ComponentTree} for {@link ModalTopLevelComponentUnion top-level modal components}
 *
 * <p>
 * Use the static methods to construct a {@link ModalComponentTree}.
 *
 * @see ComponentTree
 */
public interface ModalComponentTree extends ComponentTree<ModalTopLevelComponentUnion> {
    @NotNull
    @Override
    ModalComponentTree replace(@NotNull ComponentReplacer replacer);

    @NotNull
    @Override
    @CheckReturnValue
    ModalComponentTree withDisabled(boolean disabled);

    @NotNull
    @Override
    @CheckReturnValue
    default ModalComponentTree asDisabled() {
        return (ModalComponentTree) ComponentTree.super.asDisabled();
    }

    @NotNull
    @Override
    @CheckReturnValue
    default ModalComponentTree asEnabled() {
        return (ModalComponentTree) ComponentTree.super.asEnabled();
    }

    /**
     * Creates a {@link ModalComponentTree} from the given top-level modal components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the collection is empty
     *
     * @return A {@link ModalComponentTree} containing the given components
     */
    @NotNull
    static ModalComponentTree of(@NotNull Collection<? extends ModalTopLevelComponent> components) {
        return ModalComponentTreeImpl.of(components);
    }

    /**
     * Creates a {@link ModalComponentTree} from the given top-level modal components.
     *
     * @param  components
     *         List of components to construct the tree from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return A {@link ModalComponentTree} containing the given components
     */
    @NotNull
    static ModalComponentTree of(@NotNull ModalTopLevelComponent... components) {
        return of(Arrays.asList(components));
    }
}
