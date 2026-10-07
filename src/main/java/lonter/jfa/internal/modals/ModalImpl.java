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

package lonter.jfa.internal.modals;

import lonter.jfa.api.components.ModalTopLevelComponentUnion;
import lonter.jfa.api.modals.Modal;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.components.AbstractComponentImpl;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.Helpers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import static lonter.jfa.internal.entities.EntityBuilder.DEFAULT_COMPONENT_DESERIALIZER;

public class ModalImpl implements Modal {
    private final String id;
    private final String title;
    private final List<ModalTopLevelComponentUnion> components;

    public ModalImpl(DataObject object) {
        this.id = object.getString("custom_id");
        this.title = object.getString("title");
        this.components = object.optArray("components")
                .map(arr -> DEFAULT_COMPONENT_DESERIALIZER
                        .deserializeAs(ModalTopLevelComponentUnion.class, arr)
                        .collect(Helpers.toUnmodifiableList()))
                .orElseGet(Collections::emptyList);
    }

    public ModalImpl(String id, String title, List<ModalTopLevelComponentUnion> components) {
        this.id = id;
        this.title = title;
        this.components = Collections.unmodifiableList(components);
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @NotNull
    @Override
    public String getTitle() {
        return title;
    }

    @NotNull
    @Override
    public List<ModalTopLevelComponentUnion> getComponents() {
        return components;
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject object = DataObject.empty().put("custom_id", id).put("title", title);

        object.put(
                "components",
                DataArray.fromCollection(components.stream()
                        .map(AbstractComponentImpl.class::cast)
                        .map(AbstractComponentImpl::toData)
                        .collect(Collectors.toList())));
        return object;
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", id)
                .addMetadata("title", title)
                .toString();
    }
}
