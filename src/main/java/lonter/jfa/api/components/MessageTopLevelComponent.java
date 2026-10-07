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

package lonter.jfa.api.components;

import lonter.jfa.api.components.actionrow.ActionRow;
import lonter.jfa.api.components.container.Container;
import lonter.jfa.api.components.filedisplay.FileDisplay;
import lonter.jfa.api.components.mediagallery.MediaGallery;
import lonter.jfa.api.components.section.Section;
import lonter.jfa.api.components.separator.Separator;
import lonter.jfa.api.components.textdisplay.TextDisplay;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a component that can be added directly to a message, this includes:
 * <ul>
 *     <li>{@link ActionRow}</li>
 *     <li>{@link Section}</li>
 *     <li>{@link TextDisplay}</li>
 *     <li>{@link MediaGallery}</li>
 *     <li>{@link Separator}</li>
 *     <li>{@link FileDisplay}</li>
 *     <li>{@link Container}</li>
 * </ul>
 */
public interface MessageTopLevelComponent extends Component {
    @NotNull
    @Override
    MessageTopLevelComponent withUniqueId(int uniqueId);
}
