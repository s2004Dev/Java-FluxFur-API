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
import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.api.components.tree.ComponentTree;
import lonter.jfa.internal.components.utils.ComponentsUtil;
import lonter.jfa.internal.utils.Checks;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

public final class ComponentTreeImpl<E extends Component> extends AbstractComponentTree<E> {
    private final Class<? extends Component> componentType;

    public ComponentTreeImpl(Class<? extends Component> componentType, Collection<E> components) {
        super(components);
        this.componentType = componentType;
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.ANY;
    }

    @NotNull
    @Override
    public ComponentTree<E> replace(@NotNull ComponentReplacer replacer) {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
                componentType,
                components,
                replacer,
                (newComponents) -> new ComponentTreeImpl<>(componentType, newComponents));
    }
}
