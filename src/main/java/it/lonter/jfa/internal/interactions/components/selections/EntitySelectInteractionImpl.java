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

package it.lonter.jfa.internal.interactions.components.selections;

import it.lonter.jfa.api.components.selections.EntitySelectMenu;
import it.lonter.jfa.api.entities.IMentionable;
import it.lonter.jfa.api.entities.Mentions;
import it.lonter.jfa.api.interactions.components.selections.EntitySelectInteraction;
import it.lonter.jfa.api.utils.data.DataArray;
import it.lonter.jfa.api.utils.data.DataObject;
import it.lonter.jfa.internal.JFAImpl;
import it.lonter.jfa.internal.entities.SelectMenuMentions;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public class EntitySelectInteractionImpl extends SelectMenuInteractionImpl<IMentionable, EntitySelectMenu>
        implements EntitySelectInteraction {
    private final Mentions mentions;

    public EntitySelectInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, EntitySelectMenu.class, data);
        DataObject content = data.getObject("data");
        this.mentions = new SelectMenuMentions(
                jfa,
                interactionEntityBuilder,
                getGuild(),
                content.optObject("resolved").orElseGet(DataObject::empty),
                content.optArray("values").orElseGet(DataArray::empty));
    }

    @NotNull
    @Override
    public Mentions getMentions() {
        return mentions;
    }

    @NotNull
    @Override
    public List<IMentionable> getValues() {
        return mentions.getMentions();
    }
}
