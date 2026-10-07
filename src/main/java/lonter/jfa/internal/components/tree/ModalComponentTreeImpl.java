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

import lonter.jfa.api.components.ModalTopLevelComponent;
import lonter.jfa.api.components.ModalTopLevelComponentUnion;
import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.api.components.tree.ModalComponentTree;
import lonter.jfa.internal.components.utils.ComponentsUtil;
import lonter.jfa.internal.utils.Checks;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

public class ModalComponentTreeImpl extends AbstractComponentTree<ModalTopLevelComponentUnion>
        implements ModalComponentTree {
    private ModalComponentTreeImpl(Collection<ModalTopLevelComponentUnion> components) {
        super(components);
    }

    @NotNull
    public static ModalComponentTree of(@NotNull Collection<? extends ModalTopLevelComponent> components) {
        Checks.notEmpty(components, "Components");
        Checks.noneNull(components, "Components");

        // Allow unknown components so [[Modal#getComponentTree]] works
        Collection<ModalTopLevelComponentUnion> componentUnions =
                ComponentsUtil.membersToUnionWithUnknownType(components, ModalTopLevelComponentUnion.class);
        return new ModalComponentTreeImpl(componentUnions);
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.MODAL;
    }

    @NotNull
    @Override
    public ModalComponentTree replace(@NotNull ComponentReplacer replacer) {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
                ModalTopLevelComponent.class, components, replacer, ModalComponentTreeImpl::new);
    }

    @NotNull
    @Override
    public ModalComponentTree withDisabled(boolean disabled) {
        return (ModalComponentTree) super.withDisabled(disabled);
    }
}
