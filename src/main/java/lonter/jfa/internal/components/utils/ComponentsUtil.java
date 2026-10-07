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

package lonter.jfa.internal.components.utils;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.IComponentUnion;
import lonter.jfa.api.components.ResolvedMedia;
import lonter.jfa.api.components.UnknownComponent;
import lonter.jfa.api.components.actionrow.ActionRow;
import lonter.jfa.api.components.replacer.ComponentReplacer;
import lonter.jfa.api.components.replacer.IReplaceable;
import lonter.jfa.api.components.textdisplay.TextDisplay;
import lonter.jfa.api.components.utils.ComponentIterator;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentsUtil {
    /**
     * Checks the component has the target union type, and isn't an {@link UnknownComponent},
     * throws {@link IllegalArgumentException} otherwise.
     */
    public static <T extends IComponentUnion> T safeUnionCast(
            String componentCategory, Component component, Class<T> toUnionClass) {
        if (toUnionClass.isInstance(component)) {
            T union = toUnionClass.cast(component);
            Checks.check(!union.isUnknownComponent(), "Cannot provide UnknownComponent");
            return union;
        }

        String cleanedClassName = component.getClass().getSimpleName().replace("Impl", "");
        throw new IllegalArgumentException(Helpers.format(
                "Cannot convert " + componentCategory + " of type %s to %s!",
                cleanedClassName,
                toUnionClass.getSimpleName()));
    }

    /**
     * Checks the component has the target union type, and allows unknown components,
     * throws {@link IllegalArgumentException} otherwise.
     * <br>This should only be used for reading purposes,
     * use {@link #membersToUnion(Collection, Class)} to verify components to be sent.
     */
    public static <T extends Component> T safeUnionCastWithUnknownType(
            String componentCategory, Component component, Class<T> toUnionClass) {
        if (toUnionClass.isInstance(component)) {
            return toUnionClass.cast(component);
        }

        String cleanedClassName = component.getClass().getSimpleName().replace("Impl", "");
        throw new IllegalArgumentException(Helpers.format(
                "Cannot convert " + componentCategory + " of type %s to %s!",
                cleanedClassName,
                toUnionClass.getSimpleName()));
    }

    /**
     * Checks all the components has the target union type, and isn't an {@link UnknownComponent},
     * throws {@link IllegalArgumentException} otherwise.
     */
    public static <TUnion extends IComponentUnion> List<TUnion> membersToUnion(
            Collection<? extends Component> members, Class<TUnion> clazz) {
        return members.stream().map(c -> safeUnionCast("component", c, clazz)).collect(Collectors.toList());
    }

    /**
     * Retains all components extending the provided union type, keeps unknown components,
     * throws {@link IllegalArgumentException} on invalid types.
     * <br>This should only be used for reading purposes,
     * use {@link #membersToUnion(Collection, Class)} to verify components to be sent.
     */
    public static <T extends Component> List<T> membersToUnionWithUnknownType(
            Collection<? extends Component> members, Class<T> clazz) {
        return members.stream()
                .map(c -> safeUnionCastWithUnknownType("component", c, clazz))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <R, E extends Component> R doReplace(
            // This isn't '? extends E' as users are not required to return unions
            Class<? extends Component> expectedChildrenType,
            Iterable<E> children,
            ComponentReplacer replacer,
            Function<List<E>, R> finisher) {
        List<E> newComponents = new ArrayList<>();
        for (E component : children) {
            Component newComponent = replacer.apply(component);
            if (newComponent == null) {
                continue;
            }
            // If it returned a different component, then use it and don't try to recurse
            if (newComponent != component) {
                Checks.checkComponentType(expectedChildrenType, component, newComponent);
            } else if (component instanceof IReplaceable) {
                newComponent = ((IReplaceable) component).replace(replacer);
                Checks.checkComponentType(expectedChildrenType, component, newComponent);
            }
            newComponents.add((E) newComponent);
        }

        return finisher.apply(newComponents);
    }

    public static long getComponentTreeSize(@NotNull Collection<? extends Component> tree) {
        return ComponentIterator.createStream(tree).count();
    }

    @NotNull
    public static List<? extends Component> getIllegalV1Components(
            @NotNull Collection<? extends Component> components) {
        return components.stream().filter(c -> !(c instanceof ActionRow)).collect(Collectors.toList());
    }

    public static boolean hasIllegalV1Components(@NotNull Collection<? extends Component> components) {
        return !getIllegalV1Components(components).isEmpty();
    }

    public static long getComponentTreeTextContentLength(@NotNull Collection<? extends Component> components) {
        return ComponentIterator.createStream(components)
                .mapToInt(c -> {
                    if (c instanceof TextDisplay) {
                        return ((TextDisplay) c).getContent().length();
                    }
                    return 0;
                })
                .sum();
    }

    public static Stream<FileUpload> getFilesFromMedia(@Nullable ResolvedMedia media) {
        if (media != null && media.getAttachmentId() != null) {
            // Retain or reupload the entire file
            String fileName = Helpers.getLastPathSegment(media.getUrl());
            return Stream.of(media.getProxy().downloadAsFileUpload(fileName));
        } else {
            // External URL or user-managed attachment
            return Stream.empty();
        }
    }

    public static String getMediaUrl(@Nullable ResolvedMedia media, @Nullable String url) {
        if (media != null && media.getAttachmentId() != null) {
            // Retain or reupload the entire file, both cases uses attachment://
            return "attachment://" + Helpers.getLastPathSegment(media.getUrl());
        } else {
            // User-managed attachment
            return url;
        }
    }
}
