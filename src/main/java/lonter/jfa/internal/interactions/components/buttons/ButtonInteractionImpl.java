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

package lonter.jfa.internal.interactions.components.buttons;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.buttons.Button;
import lonter.jfa.api.interactions.components.buttons.ButtonInteraction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.interactions.components.ComponentInteractionImpl;

import org.jetbrains.annotations.NotNull;

public class ButtonInteractionImpl extends ComponentInteractionImpl implements ButtonInteraction {
    private final Button button;

    public ButtonInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, data);
        if (message != null) {
            button = message.getComponentTree()
                    .find(Button.class, b -> customId.equals(b.getCustomId()))
                    .orElse(null);
        } else {
            button = null;
        }
    }

    @NotNull
    @Override
    public Component.Type getComponentType() {
        return Component.Type.BUTTON;
    }

    @NotNull
    @Override
    public Button getButton() {
        return button;
    }
}
