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

package lonter.jfa.internal.components.textdisplay;

import lonter.jfa.api.components.MessageTopLevelComponentUnion;
import lonter.jfa.api.components.ModalTopLevelComponentUnion;
import lonter.jfa.api.components.container.ContainerChildComponentUnion;
import lonter.jfa.api.components.section.SectionContentComponentUnion;
import lonter.jfa.api.components.textdisplay.TextDisplay;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.components.AbstractComponentImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class TextDisplayImpl extends AbstractComponentImpl
        implements TextDisplay,
                MessageTopLevelComponentUnion,
                ModalTopLevelComponentUnion,
                ContainerChildComponentUnion,
                SectionContentComponentUnion {
    private final int uniqueId;
    private final String content;

    public TextDisplayImpl(DataObject data) {
        this(data.getInt("id", -1), data.getString("content"));
    }

    public TextDisplayImpl(String content) {
        this(-1, content);
    }

    private TextDisplayImpl(int uniqueId, String content) {
        this.content = content;
        this.uniqueId = uniqueId;
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.TEXT_DISPLAY;
    }

    @NotNull
    @Override
    public TextDisplayImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new TextDisplayImpl(uniqueId, content);
    }

    @NotNull
    @Override
    public TextDisplay withContent(@NotNull String content) {
        Checks.notBlank(content, "Content");
        return new TextDisplayImpl(uniqueId, content);
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @NotNull
    @Override
    public String getContent() {
        return content;
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject json = DataObject.empty().put("type", getType().getKey()).put("content", content);
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
        if (!(o instanceof TextDisplayImpl)) {
            return false;
        }
        TextDisplayImpl that = (TextDisplayImpl) o;
        return uniqueId == that.uniqueId && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, content);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("content", content)
                .toString();
    }
}
