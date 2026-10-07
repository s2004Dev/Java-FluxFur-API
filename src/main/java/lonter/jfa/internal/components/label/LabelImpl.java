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

package lonter.jfa.internal.components.label;

import lonter.jfa.api.components.ModalTopLevelComponentUnion;
import lonter.jfa.api.components.label.Label;
import lonter.jfa.api.components.label.LabelChildComponent;
import lonter.jfa.api.components.label.LabelChildComponentUnion;
import lonter.jfa.api.components.utils.ComponentDeserializer;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.components.AbstractComponentImpl;
import lonter.jfa.internal.components.utils.ComponentsUtil;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LabelImpl extends AbstractComponentImpl implements Label, ModalTopLevelComponentUnion {
    private final int uniqueId;
    private final String label;
    private final String description;
    private final LabelChildComponentUnion child;

    public LabelImpl(@NotNull ComponentDeserializer deserializer, @NotNull DataObject object) {
        this(
                object.getInt("id", -1),
                object.getString("label"),
                object.getString("description", null),
                deserializer.deserializeAs(LabelChildComponentUnion.class, object.getObject("component")));
    }

    public LabelImpl(@NotNull String label, @Nullable String description, @NotNull LabelChildComponentUnion child) {
        this(-1, label, description, child);
    }

    private LabelImpl(
            int uniqueId,
            @NotNull String label,
            @Nullable String description,
            @NotNull LabelChildComponentUnion child) {
        this.uniqueId = uniqueId;
        this.label = label;
        this.description = description;
        this.child = child;
    }

    public static Label validated(
            @NotNull String label, @Nullable String description, @NotNull LabelChildComponent child) {
        Checks.notBlank(label, "Label");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        Checks.notNull(child, "Child");
        if (description != null) {
            Checks.notBlank(description, "Description");
            Checks.notLonger(description, DESCRIPTION_MAX_LENGTH, "Description");
        }

        LabelChildComponentUnion childUnion =
                ComponentsUtil.safeUnionCast("child", child, LabelChildComponentUnion.class);
        return new LabelImpl(label, description, childUnion);
    }

    @NotNull
    @Override
    public Label withLabel(@NotNull String label) {
        return validated(label, this.description, this.child);
    }

    @NotNull
    @Override
    public Label withDescription(@Nullable String description) {
        return validated(this.label, description, this.child);
    }

    @NotNull
    @Override
    public Label withChild(@NotNull LabelChildComponent child) {
        return validated(this.label, this.description, child);
    }

    @NotNull
    @Override
    public LabelImpl withUniqueId(int uniqueId) {
        return new LabelImpl(uniqueId, label, description, child);
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.LABEL;
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @NotNull
    @Override
    public String getLabel() {
        return label;
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    @NotNull
    @Override
    public LabelChildComponentUnion getChild() {
        return child;
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject obj = DataObject.empty()
                .put("type", getType().getKey())
                .put("label", label)
                .put("description", description)
                .put("component", child);
        if (uniqueId >= 0) {
            obj.put("id", uniqueId);
        }

        return obj;
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("label", label)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LabelImpl)) {
            return false;
        }
        LabelImpl that = (LabelImpl) o;
        return uniqueId == that.uniqueId
                && Objects.equals(label, that.label)
                && Objects.equals(description, that.description)
                && Objects.equals(child, that.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, label, description, child);
    }
}
