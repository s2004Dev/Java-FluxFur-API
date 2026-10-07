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

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.actionrow.ActionRow;
import lonter.jfa.api.components.attachmentupload.AttachmentUpload;
import lonter.jfa.api.components.buttons.Button;
import lonter.jfa.api.components.container.Container;
import lonter.jfa.api.components.filedisplay.FileDisplay;
import lonter.jfa.api.components.label.Label;
import lonter.jfa.api.components.mediagallery.MediaGallery;
import lonter.jfa.api.components.section.Section;
import lonter.jfa.api.components.selections.EntitySelectMenu;
import lonter.jfa.api.components.selections.StringSelectMenu;
import lonter.jfa.api.components.separator.Separator;
import lonter.jfa.api.components.textdisplay.TextDisplay;
import lonter.jfa.api.components.textinput.TextInput;
import lonter.jfa.api.components.thumbnail.Thumbnail;
import lonter.jfa.api.utils.data.SerializableData;
import lonter.jfa.internal.utils.UnionUtil;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractComponentImpl implements SerializableData {

    // -- Union hooks --

    @NotNull
    public ActionRow asActionRow() {
        return toComponentType(ActionRow.class);
    }

    @NotNull
    public Button asButton() {
        return toComponentType(Button.class);
    }

    @NotNull
    public StringSelectMenu asStringSelectMenu() {
        return toComponentType(StringSelectMenu.class);
    }

    @NotNull
    public EntitySelectMenu asEntitySelectMenu() {
        return toComponentType(EntitySelectMenu.class);
    }

    @NotNull
    public TextInput asTextInput() {
        return toComponentType(TextInput.class);
    }

    @NotNull
    public Section asSection() {
        return toComponentType(Section.class);
    }

    @NotNull
    public TextDisplay asTextDisplay() {
        return toComponentType(TextDisplay.class);
    }

    @NotNull
    public MediaGallery asMediaGallery() {
        return toComponentType(MediaGallery.class);
    }

    @NotNull
    public Thumbnail asThumbnail() {
        return toComponentType(Thumbnail.class);
    }

    @NotNull
    public Separator asSeparator() {
        return toComponentType(Separator.class);
    }

    @NotNull
    public FileDisplay asFileDisplay() {
        return toComponentType(FileDisplay.class);
    }

    @NotNull
    public Container asContainer() {
        return toComponentType(Container.class);
    }

    @NotNull
    public Label asLabel() {
        return toComponentType(Label.class);
    }

    @NotNull
    public AttachmentUpload asAttachmentUpload() {
        return toComponentType(AttachmentUpload.class);
    }

    protected <T extends Component> T toComponentType(Class<T> type) {
        return UnionUtil.safeUnionCast("component", this, type);
    }
}
