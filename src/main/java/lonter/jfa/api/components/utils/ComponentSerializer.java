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

package lonter.jfa.api.components.utils;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.filedisplay.FileDisplay;
import lonter.jfa.api.components.thumbnail.Thumbnail;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.data.SerializableData;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.message.MessageUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class to serialize a list of {@link Component Components} into {@link DataObject}.
 *
 * <p>Since some components include implicit file uploads, such as {@link FileDisplay} and {@link Thumbnail},
 * the included {@link FileUpload} instances can be accessed using {@link #getFileUploads(Collection)}.
 * <br>Each uploaded file is referenced in the respective components using {@code attachment://filename}.
 *
 * <p>This separation is done to simplify persistence of these components in preferred formats.
 * For instance, you might want to store the components as JSON Blobs but the files in an object storage.
 *
 * <p>You can use {@link ComponentDeserializer} to deserialize the output again,
 * make sure you also provide any implicit {@link FileUpload} instances.
 *
 * @see ComponentDeserializer
 */
public class ComponentSerializer {
    /**
     * Serializes the provided component into a {@link DataObject} instance.
     *
     * <p>Some components that would implicitly upload a file, for instance {@link Thumbnail},
     * will reference the file using a URI with this format {@code attachment://filename}.
     * The {@code filename} refers to a {@link FileUpload} provided by {@link #getFileUploads(Component)},
     * with a corresponding {@link FileUpload#getName() name}.
     *
     * @param  component
     *         The component to serialized into {@link DataObject}
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return Serialized {@link DataObject} for the provided component
     */
    @NotNull
    public DataObject serialize(@NotNull Component component) {
        Checks.notNull(component, "Component");
        Checks.check(component instanceof SerializableData, "Component is not serializable");
        return ((SerializableData) component).toData();
    }

    /**
     * Serializes the provided components into {@link DataObject} instances.
     *
     * <p>Some components that would implicitly upload a file, for instance {@link Thumbnail},
     * will reference the file using a URI with this format {@code attachment://filename}.
     * The {@code filename} refers to a {@link FileUpload} provided by {@link #getFileUploads(Collection)},
     * with a corresponding {@link FileUpload#getName() name}.
     *
     * @param  components
     *         The components to serialized into {@link DataObject DataObjects}
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return {@link List} of {@link DataObject}
     */
    @NotNull
    public List<DataObject> serializeAll(@NotNull Collection<? extends Component> components) {
        Checks.noneNull(components, "Components");
        return components.stream()
                .map(SerializableData.class::cast)
                .map(SerializableData::toData)
                .collect(Collectors.toList());
    }

    /**
     * Returns the implicit {@link FileUpload} instances used by {@link #serialize(Component)}.
     *
     * @param  component
     *         The component to take the {@link FileUpload FileUploads} from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The implicit {@link FileUpload} instances for the provided component
     */
    @NotNull
    public List<FileUpload> getFileUploads(@NotNull Component component) {
        Checks.notNull(component, "Component");
        return MessageUtil.getIndirectFiles(Collections.singletonList(component));
    }

    /**
     * Returns the implicit {@link FileUpload} instances used by {@link #serializeAll(Collection)}.
     *
     * @param  components
     *         The components to take the {@link FileUpload FileUploads} from
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The implicit {@link FileUpload} instances for the provided components
     */
    @NotNull
    public List<FileUpload> getFileUploads(@NotNull Collection<? extends Component> components) {
        Checks.noneNull(components, "Components");
        return MessageUtil.getIndirectFiles(components);
    }
}
