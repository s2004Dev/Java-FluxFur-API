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

package lonter.jfa.api.entities.channel.forums;

import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a Fluxer Forum Tag.
 * <br>These tags can be applied to forum posts to help categorize them.
 */
public interface ForumTag extends ForumTagSnowflake, Comparable<ForumTag>, BaseForumTag {
    /**
     * The maximum length of a forum tag name ({@value #MAX_NAME_LENGTH})
     */
    int MAX_NAME_LENGTH = 20;

    /**
     * The tag position, used for sorting.
     *
     * @return The tag position.
     */
    int getPosition();

    @Override
    default int compareTo(@NotNull ForumTag o) {
        Checks.notNull(o, "ForumTag");
        return Integer.compare(getPosition(), o.getPosition());
    }

    @NotNull
    @Override
    default DataObject toData() {
        return BaseForumTag.super.toData().put("id", getId());
    }
}
