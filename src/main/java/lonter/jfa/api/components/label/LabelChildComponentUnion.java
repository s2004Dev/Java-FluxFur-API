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

package lonter.jfa.api.components.label;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.IComponentUnion;
import lonter.jfa.api.components.attachmentupload.AttachmentUpload;
import lonter.jfa.api.components.selections.EntitySelectMenu;
import lonter.jfa.api.components.selections.StringSelectMenu;
import lonter.jfa.api.components.textinput.TextInput;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a union of {@link LabelChildComponent} that can be one of:
 * <ul>
 *     <li>{@link TextInput}</li>
 *     <li>{@link StringSelectMenu}</li>
 *     <li>{@link EntitySelectMenu}</li>
 *     <li>{@link AttachmentUpload}</li>
 * </ul>
 */
public interface LabelChildComponentUnion extends LabelChildComponent, IComponentUnion {
    /**
     * Casts this union to a {@link TextInput}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang="java":
     * //These are the same!
     * TextInput input = union.asTextInput();
     * TextInput input2 = (TextInput) union;
     * }
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#TEXT_INPUT} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof TextInput</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link TextInput}.
     *
     * @return The component as a {@link TextInput}
     */
    @NotNull
    TextInput asTextInput();

    /**
     * Casts this union to a {@link StringSelectMenu}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang="java":
     * //These are the same!
     * StringSelectMenu menu = union.asStringSelectMenu();
     * StringSelectMenu menu2 = (StringSelectMenu) union;
     * }
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#STRING_SELECT} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof StringSelectMenu</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link StringSelectMenu}.
     *
     * @return The component as a {@link StringSelectMenu}
     */
    @NotNull
    StringSelectMenu asStringSelectMenu();

    /**
     * Casts this union to a {@link EntitySelectMenu}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang="java":
     * //These are the same!
     * EntitySelectMenu menu = union.asEntitySelectMenu();
     * EntitySelectMenu menu2 = (EntitySelectMenu) union;
     * }
     *
     * You can use {@link #getType()} to see if the component is one of:
     * <ul>
     *     <li>{@link Component.Type#USER_SELECT USER_SELECT}</li>
     *     <li>{@link Component.Type#ROLE_SELECT ROLE_SELECT}</li>
     *     <li>{@link Component.Type#MENTIONABLE_SELECT MENTIONABLE_SELECT}</li>
     *     <li>{@link Component.Type#CHANNEL_SELECT CHANNEL_SELECT}</li>
     * </ul>
     * to validate whether you can call this method in addition to normal instanceof checks: <code>component instanceof EntitySelectMenu</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link EntitySelectMenu}.
     *
     * @return The component as a {@link EntitySelectMenu}
     */
    @NotNull
    EntitySelectMenu asEntitySelectMenu();

    /**
     * Casts this union to a {@link AttachmentUpload}.
     * This method exists for developer discoverability.
     *
     * <p>Note: This is effectively equivalent to using the cast operator:
     * {@snippet lang="java":
     * //These are the same!
     * AttachmentUpload menu = union.asAttachmentUpload();
     * AttachmentUpload menu2 = (AttachmentUpload) union;
     * }
     *
     * You can use {@link #getType()} to see if the component is of type {@link Component.Type#FILE_UPLOAD} to validate
     * whether you can call this method in addition to normal instanceof checks: <code>component instanceof AttachmentUpload</code>
     *
     * @throws IllegalStateException
     *         If the component represented by this union is not actually a {@link AttachmentUpload}.
     *
     * @return The component as a {@link AttachmentUpload}
     */
    @NotNull
    AttachmentUpload asAttachmentUpload();

    @NotNull
    @Override
    LabelChildComponentUnion withUniqueId(int uniqueId);
}
