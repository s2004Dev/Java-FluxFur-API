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

package lonter.jfa.internal.components.textinput;

import lonter.jfa.api.components.label.LabelChildComponentUnion;
import lonter.jfa.api.components.textinput.TextInput;
import lonter.jfa.api.components.textinput.TextInputStyle;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.components.AbstractComponentImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextInputImpl extends AbstractComponentImpl implements TextInput, LabelChildComponentUnion {
    private final String id;
    private final int uniqueId;
    private final TextInputStyle style;
    private final int minLength;
    private final int maxLength;
    private final boolean required;
    private final String value;
    private final String placeholder;

    public TextInputImpl(DataObject object) {
        this(
                object.getString("custom_id"),
                object.getInt("id", -1),
                TextInputStyle.fromKey(object.getInt("style", -1)),
                object.getInt("min_length", -1),
                object.getInt("max_length", -1),
                object.getBoolean("required", true),
                object.getString("value", null),
                object.getString("placeholder", null));
    }

    public TextInputImpl(
            String id,
            int uniqueId,
            TextInputStyle style,
            int minLength,
            int maxLength,
            boolean required,
            String value,
            String placeholder) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.style = style;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.required = required;
        this.value = value;
        this.placeholder = placeholder;
    }

    @NotNull
    @Override
    public TextInputImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new TextInputImpl(id, uniqueId, style, minLength, maxLength, required, value, placeholder);
    }

    @NotNull
    @Override
    public TextInputStyle getStyle() {
        return style;
    }

    @NotNull
    @Override
    public String getCustomId() {
        return id;
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Override
    public int getMinLength() {
        return minLength;
    }

    @Override
    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Nullable
    @Override
    public String getValue() {
        return value;
    }

    @Nullable
    @Override
    public String getPlaceHolder() {
        return placeholder;
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject obj = DataObject.empty()
                .put("type", getType().getKey())
                .put("custom_id", id)
                .put("style", style.getRaw())
                .put("required", required);
        if (uniqueId >= 0) {
            obj.put("id", uniqueId);
        }
        if (minLength != -1) {
            obj.put("min_length", minLength);
        }
        if (maxLength != -1) {
            obj.put("max_length", maxLength);
        }
        if (value != null) {
            obj.put("value", value);
        }
        if (placeholder != null) {
            obj.put("placeholder", placeholder);
        }
        return obj;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TextInputImpl)) {
            return false;
        }
        TextInputImpl that = (TextInputImpl) o;
        return uniqueId == that.uniqueId
                && minLength == that.minLength
                && maxLength == that.maxLength
                && required == that.required
                && id.equals(that.id)
                && style == that.style
                && Objects.equals(value, that.value)
                && Objects.equals(placeholder, that.placeholder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uniqueId, style, minLength, maxLength, required, value, placeholder);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .setType(style)
                .addMetadata("id", id)
                .addMetadata("value", value)
                .toString();
    }
}
