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

package lonter.jfa.internal.interactions.components.selections;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.selections.SelectMenu;
import lonter.jfa.api.interactions.components.selections.SelectMenuInteraction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.interactions.components.ComponentInteractionImpl;

import org.jetbrains.annotations.NotNull;

public abstract class SelectMenuInteractionImpl<T, S extends SelectMenu> extends ComponentInteractionImpl
        implements SelectMenuInteraction<T, S> {
    private final S menu;

    public SelectMenuInteractionImpl(JFAImpl jfa, Class<S> type, DataObject data) {
        super(jfa, data);
        if (message != null) {
            menu = message.getComponentTree()
                    .find(type, s -> customId.equals(s.getCustomId()))
                    .orElse(null);
        } else {
            menu = null;
        }
    }

    @NotNull
    @Override
    public S getComponent() {
        return menu;
    }

    @NotNull
    @Override
    public Component.Type getComponentType() {
        return menu.getType();
    }
}
