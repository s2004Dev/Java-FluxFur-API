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

package it.lonter.jfa.api.components.utils;

import it.lonter.jfa.api.components.Component;
import it.lonter.jfa.api.components.IComponentUnion;
import it.lonter.jfa.api.components.MessageTopLevelComponent;
import it.lonter.jfa.api.components.ModalTopLevelComponent;
import it.lonter.jfa.api.components.filedisplay.FileDisplay;
import it.lonter.jfa.api.components.mediagallery.MediaGallery;
import it.lonter.jfa.api.components.mediagallery.MediaGalleryItem;
import it.lonter.jfa.api.components.thumbnail.Thumbnail;
import it.lonter.jfa.api.components.tree.ComponentTree;
import it.lonter.jfa.api.components.tree.MessageComponentTree;
import it.lonter.jfa.api.components.tree.ModalComponentTree;
import it.lonter.jfa.api.exceptions.ParsingException;
import it.lonter.jfa.api.utils.FileUpload;
import it.lonter.jfa.api.utils.data.DataArray;
import it.lonter.jfa.api.utils.data.DataObject;
import it.lonter.jfa.internal.components.UnknownComponentImpl;
import it.lonter.jfa.internal.components.actionrow.ActionRowImpl;
import it.lonter.jfa.internal.components.attachmentupload.AttachmentUploadImpl;
import it.lonter.jfa.internal.components.buttons.ButtonImpl;
import it.lonter.jfa.internal.components.container.ContainerImpl;
import it.lonter.jfa.internal.components.filedisplay.FileDisplayFileUpload;
import it.lonter.jfa.internal.components.filedisplay.FileDisplayImpl;
import it.lonter.jfa.internal.components.label.LabelImpl;
import it.lonter.jfa.internal.components.mediagallery.MediaGalleryImpl;
import it.lonter.jfa.internal.components.mediagallery.MediaGalleryItemFileUpload;
import it.lonter.jfa.internal.components.mediagallery.MediaGalleryItemImpl;
import it.lonter.jfa.internal.components.section.SectionImpl;
import it.lonter.jfa.internal.components.selections.EntitySelectMenuImpl;
import it.lonter.jfa.internal.components.selections.StringSelectMenuImpl;
import it.lonter.jfa.internal.components.separator.SeparatorImpl;
import it.lonter.jfa.internal.components.textdisplay.TextDisplayImpl;
import it.lonter.jfa.internal.components.textinput.TextInputImpl;
import it.lonter.jfa.internal.components.thumbnail.ThumbnailFileUpload;
import it.lonter.jfa.internal.components.thumbnail.ThumbnailImpl;
import it.lonter.jfa.internal.components.utils.ComponentsUtil;
import it.lonter.jfa.internal.utils.Checks;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class to deserialize components that were previously serialized by {@link ComponentSerializer}.
 *
 * @see ComponentSerializer
 */
public class ComponentDeserializer {
    private static final String ATTACHMENT_SCHEMA = "attachment://";
    private final Map<String, FileUpload> files;

    /**
     * Create a new deserializer instance with the provided files.
     *
     * @param files
     *        The implicit file uploads used by the components (see {@link ComponentSerializer#getFileUploads(Collection)})
     */
    public ComponentDeserializer(@NotNull Collection<? extends FileUpload> files) {
        this.files = new LinkedHashMap<>(files.size());
        for (FileUpload file : files) {
            this.files.put(file.getName(), file);
        }
    }

    /**
     * Deserializes all the provided components.
     *
     * @param  components
     *         The list of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components have an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The deserialized components as {@link IComponentUnion}
     *
     * @see    #deserializeAs(Class, List)
     */
    @NotNull
    public List<IComponentUnion> deserializeAll(@NotNull List<DataObject> components) {
        Checks.noneNull(components, "Components");
        return components.stream().map(this::parseComponent).collect(Collectors.toList());
    }

    /**
     * Deserializes all the provided components to the provided component type.
     *
     * @param  type
     *         The target component type
     * @param  components
     *         The list of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components have an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components
     *
     * @see    #deserializeAs(Class, DataObject)
     */
    @NotNull
    public <T extends Component> Stream<T> deserializeAs(@NotNull Class<T> type, @NotNull List<DataObject> components) {
        Checks.notNull(type, "Type");
        Checks.noneNull(components, "Components");
        return components.stream()
                .map(this::parseComponent)
                .map(component -> ComponentsUtil.safeUnionCastWithUnknownType("component", component, type));
    }

    /**
     * Deserializes all the provided components to the provided component type.
     *
     * @param  type
     *         The target component type
     * @param  components
     *         The array of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components have an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components
     *
     * @see    #deserializeAs(Class, List)
     */
    @NotNull
    public <T extends Component> Stream<T> deserializeAs(@NotNull Class<T> type, @NotNull DataArray components) {
        Checks.notNull(type, "Type");
        Checks.notNull(components, "Components");
        return components.stream(DataArray::getObject)
                .map(this::parseComponent)
                .map(component -> ComponentsUtil.safeUnionCastWithUnknownType("component", component, type));
    }

    /**
     * Deserializes the provided components to the provided component type.
     *
     * @param  type
     *         The target component type
     * @param  component
     *         The component to deserialize
     *
     * @throws ParsingException
     *         If the component has an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components
     *
     * @see    #deserializeAs(Class, List)
     */
    @NotNull
    public <T extends Component> T deserializeAs(@NotNull Class<T> type, @NotNull DataObject component) {
        Checks.notNull(type, "Type");
        Checks.notNull(component, "Component");
        IComponentUnion componentUnion = parseComponent(component);
        return ComponentsUtil.safeUnionCastWithUnknownType("component", componentUnion, type);
    }

    /**
     * Deserializes the provided components to the provided component type.
     *
     * @param  treeType
     *         The target component tree type (for instance {@link MessageComponentTree})
     * @param  components
     *         The list of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components has an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components as a component tree
     *
     * @see    #deserializeAs(Class, List)
     */
    @NotNull
    public <T extends ComponentTree<?>> T deserializeAsTree(
            @NotNull Class<T> treeType, @NotNull List<DataObject> components) {
        Checks.notNull(components, "Components");
        return deserializeAsTree(treeType, DataArray.fromCollection(components));
    }

    /**
     * Deserializes the provided components to the provided component type.
     *
     * @param  treeType
     *         The target component tree type (for instance {@link MessageComponentTree})
     * @param  components
     *         The list of components to deserialize
     *
     * @throws ParsingException
     *         If any of the components has an invalid format
     * @throws IllegalArgumentException
     *         If {@code null} is provided or the resulting component cannot be cast to the target type
     *
     * @return The deserialized components as a component tree
     *
     * @see    #deserializeAs(Class, List)
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends ComponentTree<?>> T deserializeAsTree(@NotNull Class<T> treeType, @NotNull DataArray components) {
        Checks.notNull(treeType, "Tree type");
        Checks.notNull(components, "Components");

        if (MessageComponentTree.class.isAssignableFrom(treeType)) {
            return (T) MessageComponentTree.of(
                    deserializeAs(MessageTopLevelComponent.class, components).collect(Collectors.toList()));
        } else if (ModalComponentTree.class.isAssignableFrom(treeType)) {
            return (T) ModalComponentTree.of(
                    deserializeAs(ModalTopLevelComponent.class, components).collect(Collectors.toList()));
        } else if (ComponentTree.class.isAssignableFrom(treeType)) {
            return (T)
                    ComponentTree.of(deserializeAs(Component.class, components).collect(Collectors.toList()));
        } else {
            throw new UnsupportedOperationException("Cannot deserialize to tree of type " + treeType.getName());
        }
    }

    @NotNull
    private IComponentUnion parseComponent(@NotNull DataObject data) {
      return switch (Component.Type.fromKey(data.getInt("type"))) {
        case ACTION_ROW -> new ActionRowImpl(this, data);
        case BUTTON -> new ButtonImpl(data);
        case STRING_SELECT -> new StringSelectMenuImpl(data);
        case TEXT_INPUT -> new TextInputImpl(data);
        case USER_SELECT, ROLE_SELECT, MENTIONABLE_SELECT, CHANNEL_SELECT -> new EntitySelectMenuImpl(data);
        case SECTION -> new SectionImpl(this, data);
        case TEXT_DISPLAY -> new TextDisplayImpl(data);
        case THUMBNAIL -> (IComponentUnion) toThumbnail(data);
        case MEDIA_GALLERY -> (IComponentUnion) toMediaGallery(data);
        case FILE_DISPLAY -> (IComponentUnion) toFileDisplay(data);
        case SEPARATOR -> new SeparatorImpl(data);
        case CONTAINER -> new ContainerImpl(this, data);
        case LABEL -> new LabelImpl(this, data);
        case FILE_UPLOAD -> new AttachmentUploadImpl(data);
        default -> new UnknownComponentImpl(data);
      };
    }

    @NotNull
    private Thumbnail toThumbnail(@NotNull DataObject data) {
        String url = data.getObject("media").getString("url");
        if (url.startsWith(ATTACHMENT_SCHEMA)) {
            return new ThumbnailFileUpload(
                    data.getInt("id", -1),
                    getFileByUri(url),
                    data.getString("description", null),
                    data.getBoolean("spoiler"));
        }

        return new ThumbnailImpl(data);
    }

    @NotNull
    private FileDisplay toFileDisplay(@NotNull DataObject data) {
        String url = data.getObject("file").getString("url");
        if (url.startsWith(ATTACHMENT_SCHEMA)) {
            return new FileDisplayFileUpload(data.getInt("id", -1), getFileByUri(url), data.getBoolean("spoiler"));
        }

        return new FileDisplayImpl(data);
    }

    @NotNull
    private MediaGallery toMediaGallery(@NotNull DataObject data) {
        return new MediaGalleryImpl(
                data.getInt("id", -1),
                data.getArray("items").stream(DataArray::getObject)
                        .map(this::toMediaGalleryItem)
                        .collect(Collectors.toList()));
    }

    @NotNull
    private MediaGalleryItem toMediaGalleryItem(@NotNull DataObject data) {
        String url = data.getObject("media").getString("url");
        if (url.startsWith(ATTACHMENT_SCHEMA)) {
            return new MediaGalleryItemFileUpload(
                    getFileByUri(url), data.getString("description", null), data.getBoolean("spoiler"));
        }

        return new MediaGalleryItemImpl(data);
    }

    @NotNull
    private FileUpload getFileByUri(@NotNull String uri) {
        String name = uri.substring(ATTACHMENT_SCHEMA.length());
        FileUpload file = files.get(name);
        Checks.check(file != null, "File for URI %s is missing", uri);
        return file;
    }
}
