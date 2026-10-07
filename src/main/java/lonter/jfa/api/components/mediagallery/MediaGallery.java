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

package lonter.jfa.api.components.mediagallery;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.MessageTopLevelComponent;
import lonter.jfa.api.components.container.ContainerChildComponent;
import lonter.jfa.api.utils.messages.MessageRequest;
import lonter.jfa.internal.components.mediagallery.MediaGalleryImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Component which displays a group of images, videos, GIFs or WEBPs into a gallery grid.
 *
 * <p>This can contain up to {@value #MAX_ITEMS} {@link MediaGalleryItem}.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} needs to be enabled!
 */
public interface MediaGallery extends Component, MessageTopLevelComponent, ContainerChildComponent {
    /**
     * How many {@link MediaGalleryItem} can be in a media gallery. ({@value})
     */
    int MAX_ITEMS = 10;

    /**
     * Constructs a new {@link MediaGallery} from the given items.
     *
     * @param  items
     *         The items to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value #MAX_ITEMS} items are provided</li>
     *         </ul>
     *
     * @return The new {@link MediaGallery}
     */
    @NotNull
    static MediaGallery of(@NotNull Collection<? extends MediaGalleryItem> items) {
        return MediaGalleryImpl.validated(items);
    }

    /**
     * Constructs a new {@link MediaGallery} from the given items.
     *
     * @param  item
     *         The item to add
     * @param  items
     *         Additional items to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If more than {@value #MAX_ITEMS} items are provided</li>
     *         </ul>
     *
     * @return The new {@link MediaGallery}
     */
    @NotNull
    static MediaGallery of(@NotNull MediaGalleryItem item, @NotNull MediaGalleryItem... items) {
        Checks.notNull(item, "Item");
        Checks.noneNull(items, "Items");
        return of(Helpers.mergeVararg(item, items));
    }

    @NotNull
    @Override
    @CheckReturnValue
    MediaGallery withUniqueId(int uniqueId);

    /**
     * Creates a new {@link MediaGallery} with the specified items.
     *
     * @param  items
     *         The new items
     *
     * @throws IllegalArgumentException
     *         If the provided items are {@code null} or contains {@code null}
     *
     * @return The new {@link MediaGallery}
     */
    @NotNull
    @CheckReturnValue
    MediaGallery withItems(@NotNull Collection<? extends MediaGalleryItem> items);

    /**
     * Creates a new {@link MediaGallery} with the specified items.
     *
     * @param  item
     *         The first new items
     * @param  items
     *         Additional new items
     *
     * @throws IllegalArgumentException
     *         If the provided items are {@code null} or contains {@code null}
     *
     * @return The new {@link MediaGallery}
     */
    @NotNull
    @CheckReturnValue
    default MediaGallery withItems(@NotNull MediaGalleryItem item, @NotNull MediaGalleryItem... items) {
        Checks.notNull(item, "Item");
        Checks.notNull(items, "Items");
        return withItems(Helpers.mergeVararg(item, items));
    }

    /**
     * Returns an immutable list with the items contained by this media gallery.
     *
     * @return {@link List} of {@link MediaGalleryItem} in this media gallery
     */
    @NotNull
    @Unmodifiable
    List<MediaGalleryItem> getItems();
}
