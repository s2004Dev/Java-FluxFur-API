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

package lonter.jfa.internal.components;

import lonter.jfa.api.components.MessageTopLevelComponentUnion;
import lonter.jfa.api.components.ModalTopLevelComponentUnion;
import lonter.jfa.api.components.UnknownComponent;
import lonter.jfa.api.components.actionrow.ActionRowChildComponentUnion;
import lonter.jfa.api.components.container.ContainerChildComponentUnion;
import lonter.jfa.api.components.section.SectionAccessoryComponentUnion;
import lonter.jfa.api.components.section.SectionContentComponentUnion;
import lonter.jfa.api.utils.data.DataObject;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class UnknownComponentImpl extends AbstractComponentImpl
        implements UnknownComponent,
                MessageTopLevelComponentUnion,
                ModalTopLevelComponentUnion,
                ActionRowChildComponentUnion,
                SectionContentComponentUnion,
                SectionAccessoryComponentUnion,
                ContainerChildComponentUnion {
    private final DataObject data;

    public UnknownComponentImpl(DataObject data) {
        this.data = data;
    }

    @NotNull
    @Override
    public UnknownComponentImpl withUniqueId(int uniqueId) {
        throw new UnsupportedOperationException("Cannot modify an unknown component");
    }

    @Override
    public int getUniqueId() {
        return data.getInt("id");
    }

    @Override
    @NotNull
    public DataObject toData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UnknownComponentImpl)) {
            return false;
        }
        UnknownComponentImpl that = (UnknownComponentImpl) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data);
    }

    @Override
    public String toString() {
        return "UnknownComponent(data=" + data + ")";
    }
}
