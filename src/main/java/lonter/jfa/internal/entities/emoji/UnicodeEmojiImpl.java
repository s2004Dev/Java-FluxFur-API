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

package lonter.jfa.internal.entities.emoji;

import lonter.jfa.api.entities.emoji.*;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.EncodingUtil;
import lonter.jfa.internal.utils.EntityString;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class UnicodeEmojiImpl implements UnicodeEmoji, EmojiUnion {
    private final String name;

    public UnicodeEmojiImpl(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String getAsReactionCode() {
        return name;
    }

    @NotNull
    @Override
    public String getAsCodepoints() {
        return EncodingUtil.encodeCodepoints(name);
    }

    @NotNull
    @Override
    public DataObject toData() {
        return DataObject.empty().put("name", name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof UnicodeEmoji)) {
            return false;
        }
        return name.equals(((UnicodeEmoji) obj).getName());
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("codepoints", getAsCodepoints())
                .toString();
    }

    @NotNull
    @Override
    public UnicodeEmoji asUnicode() {
        return this;
    }

    @NotNull
    @Override
    public CustomEmoji asCustom() {
        throw new IllegalStateException("Cannot convert UnicodeEmoji into CustomEmoji!");
    }

    @NotNull
    @Override
    public RichCustomEmoji asRich() {
        throw new IllegalStateException("Cannot convert UnicodeEmoji into RichCustomEmoji!");
    }

    @NotNull
    @Override
    public ApplicationEmoji asApplication() {
        throw new IllegalStateException("Cannot convert UnicodeEmoji to ApplicationEmoji!");
    }
}
