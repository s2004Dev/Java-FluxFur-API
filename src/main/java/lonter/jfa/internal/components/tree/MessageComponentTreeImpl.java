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

import lonter.jfa.api.components.MessageTopLevelComponent;
import lonter.jfa.api.components.MessageTopLevelComponentUnion;
import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.api.components.tree.MessageComponentTree;
import lonter.jfa.internal.components.utils.ComponentsUtil;
import lonter.jfa.internal.utils.Checks;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

public class MessageComponentTreeImpl extends AbstractComponentTree<MessageTopLevelComponentUnion>
        implements MessageComponentTree {
    private MessageComponentTreeImpl(Collection<MessageTopLevelComponentUnion> components) {
        super(components);
    }

    @NotNull
    public static MessageComponentTree of(@NotNull Collection<? extends MessageTopLevelComponent> components) {
        // Empty trees are allowed (messages can contain no components)
        Checks.noneNull(components, "Components");

        // Allow unknown components so [[Message#getComponentTree]] works
        Collection<MessageTopLevelComponentUnion> componentUnions =
                ComponentsUtil.membersToUnionWithUnknownType(components, MessageTopLevelComponentUnion.class);
        return new MessageComponentTreeImpl(componentUnions);
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.MESSAGE;
    }

    @NotNull
    @Override
    public MessageComponentTree replace(@NotNull ComponentReplacer replacer) {
        Checks.notNull(replacer, "ComponentReplacer");
        return ComponentsUtil.doReplace(
                MessageTopLevelComponent.class, components, replacer, MessageComponentTreeImpl::new);
    }

    @NotNull
    @Override
    public MessageComponentTree withDisabled(boolean disabled) {
        return (MessageComponentTree) super.withDisabled(disabled);
    }
}
