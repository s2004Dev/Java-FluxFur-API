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

package lonter.jfa.internal.components.tree;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.attribute.IDisableable;
import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.api.components.tree.ComponentTree;
import lonter.jfa.internal.utils.Helpers;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractComponentTree<E extends Component> implements ComponentTree<E> {
    protected final List<E> components;

    protected AbstractComponentTree(Collection<E> components) {
        this.components = Helpers.copyAsUnmodifiableList(components);
    }

    @NotNull
    @Override
    public List<E> getComponents() {
        return components;
    }

    @NotNull
    @Override
    public ComponentTree<E> withDisabled(boolean disabled) {
        return replace(ComponentReplacer.of(IDisableable.class, c -> true, c -> c.withDisabled(disabled)));
    }
}
