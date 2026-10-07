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

package lonter.jfa.internal.components.separator;

import lonter.jfa.api.components.MessageTopLevelComponentUnion;
import lonter.jfa.api.components.container.ContainerChildComponentUnion;
import lonter.jfa.api.components.separator.Separator;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.components.AbstractComponentImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class SeparatorImpl extends AbstractComponentImpl
        implements Separator, MessageTopLevelComponentUnion, ContainerChildComponentUnion {
    private final int uniqueId;
    private final Spacing spacing;
    private final boolean isDivider;

    public SeparatorImpl(DataObject obj) {
        this(obj.getInt("id", -1), Spacing.fromKey(obj.getInt("spacing", 1)), obj.getBoolean("divider", true));
    }

    public SeparatorImpl(Spacing spacing, boolean isDivider) {
        this(-1, spacing, isDivider);
        Checks.check(spacing != Spacing.UNKNOWN, "Spacing cannot be unknown");
    }

    private SeparatorImpl(int uniqueId, Spacing spacing, boolean isDivider) {
        this.uniqueId = uniqueId;
        this.spacing = spacing;
        this.isDivider = isDivider;
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.SEPARATOR;
    }

    @NotNull
    @Override
    public SeparatorImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new SeparatorImpl(uniqueId, spacing, isDivider);
    }

    @NotNull
    @Override
    public Separator withDivider(boolean divider) {
        return new SeparatorImpl(uniqueId, spacing, divider);
    }

    @NotNull
    @Override
    public Separator withSpacing(@NotNull Spacing spacing) {
        Checks.notNull(spacing, "Spacing");
        Checks.check(spacing != Spacing.UNKNOWN, "Spacing cannot be unknown");
        return new SeparatorImpl(uniqueId, spacing, isDivider);
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Override
    public boolean isDivider() {
        return isDivider;
    }

    @NotNull
    @Override
    public Spacing getSpacing() {
        return spacing;
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("divider", isDivider)
                .put("spacing", spacing.getKey());
        if (uniqueId >= 0) {
            json.put("id", uniqueId);
        }
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SeparatorImpl)) {
            return false;
        }
        SeparatorImpl separator = (SeparatorImpl) o;
        return uniqueId == separator.uniqueId && isDivider == separator.isDivider && spacing == separator.spacing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, spacing, isDivider);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("divider", isDivider)
                .addMetadata("spacing", spacing)
                .toString();
    }
}
