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

import lonter.jfa.api.components.selections.StringSelectMenu;
import lonter.jfa.api.interactions.components.selections.StringSelectInteraction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class StringSelectInteractionImpl extends SelectMenuInteractionImpl<String, StringSelectMenu>
        implements StringSelectInteraction {
    private final List<String> values;

    public StringSelectInteractionImpl(JFAImpl jfa, DataObject data) {
        super(jfa, StringSelectMenu.class, data);
        this.values = Collections.unmodifiableList(parseValues(data.getObject("data")));
    }

    protected List<String> parseValues(DataObject data) {
        return data.optArray("values")
                .map(arr -> arr.stream(DataArray::getString).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @NotNull
    @Override
    public List<String> getValues() {
        return values;
    }
}
