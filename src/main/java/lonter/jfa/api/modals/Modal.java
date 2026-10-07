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

package lonter.jfa.api.modals;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.ModalTopLevelComponent;
import lonter.jfa.api.components.ModalTopLevelComponentUnion;
import lonter.jfa.api.components.attribute.IDisableable;
import lonter.jfa.api.components.label.Label;
import lonter.jfa.api.components.tree.ComponentTree;
import lonter.jfa.api.components.tree.ModalComponentTree;
import lonter.jfa.api.events.interaction.ModalInteractionEvent;
import lonter.jfa.api.interactions.modals.ModalInteraction;
import lonter.jfa.api.utils.data.SerializableData;
import lonter.jfa.internal.components.utils.ComponentsUtil;
import lonter.jfa.internal.modals.ModalImpl;
import lonter.jfa.internal.utils.Checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Fluxer Modal
 *
 * <p>Replying to an interaction with a modal will open an interactive popout on the User's Fluxer client.
 * This is similar to the ban modal where you can input a ban reason.
 *
 * <p><b>Example</b><br>
 * {@snippet lang="java":
 * public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event)
 * {
 *     if (event.getName().equals("modmail"))
 *     {
 *         TextInput subject = TextInput.create("subject", TextInputStyle.SHORT)
 *                 .setPlaceholder("Subject of this ticket")
 *                 .setMinLength(10)
 *                 .setMaxLength(100) // or setRequiredRange(10, 100)
 *                 .build();
 *
 *         TextInput body = TextInput.create("body", TextInputStyle.PARAGRAPH)
 *                 .setPlaceholder("Your concerns go here")
 *                 .setMinLength(30)
 *                 .setMaxLength(1000)
 *                 .build();
 *
 *         Modal modal = Modal.create("modmail", "Modmail")
 *                 .addComponents(Label.of("Subject", subject), Label.of("Body", body))
 *                 .build();
 *
 *         event.replyModal(modal).queue();
 *     }
 * }}
 *
 * @see    ModalInteractionEvent
 */
public interface Modal extends SerializableData {
    /**
     * The maximum amount of components a Modal can have. ({@value})
     */
    int MAX_COMPONENTS = 5;

    /**
     * The maximum length a modal custom id can have. ({@value})
     */
    int MAX_ID_LENGTH = 100;

    /**
     * The maximum length a modal title can have. ({@value})
     */
    int MAX_TITLE_LENGTH = 45;

    /**
     * The custom id of this modal
     *
     * @return The custom id of this modal
     *
     * @see    ModalInteraction#getModalId()
     */
    @NotNull
    String getId();

    /**
     * The title of this modal
     *
     * @return The title of this modal
     */
    @NotNull
    String getTitle();

    /**
     * A List of {@link ModalTopLevelComponent components} that this modal contains.
     *
     * @return List of ModalTopLevelComponentUnions
     */
    @NotNull
    List<ModalTopLevelComponentUnion> getComponents();

    /**
     * A {@link ModalComponentTree} constructed from {@link #getComponents()}.
     *
     * @return {@link ModalComponentTree}
     */
    @NotNull
    default ModalComponentTree getComponentTree() {
        return ModalComponentTree.of(getComponents());
    }

    /**
     * Creates a new preconfigured {@link Modal.Builder} with the same settings used for this modal.
     * <br>This can be useful to create an updated version of this modal without needing to rebuild it from scratch.
     *
     * @return The {@link Modal.Builder} used to create the modal
     */
    @NotNull
    default Modal.Builder createCopy() {
        List<ModalTopLevelComponent> c =
                getComponents().stream().map(c2 -> (ModalTopLevelComponent) c2).collect(Collectors.toList());
        return new Builder(getId(), getTitle()).addComponents(c);
    }

    /**
     * Creates a new Modal. You must add at least one component to a modal before building it.
     *
     * @param  customId
     *         The custom id for this modal
     * @param  title
     *         The title for this modal
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided customId or title are null, empty, or blank</li>
     *             <li>If the provided customId is longer than {@value MAX_ID_LENGTH} characters</li>
     *             <li>If the provided title is longer than {@value #MAX_TITLE_LENGTH} characters</li>
     *         </ul>
     *
     * @return {@link Builder Builder} instance to customize this modal further
     */
    @NotNull
    @CheckReturnValue
    static Modal.Builder create(@NotNull String customId, @NotNull String title) {
        return new Modal.Builder(customId, title);
    }

    /**
     * A preconfigured builder for the creation of modals.
     */
    class Builder {
        private final List<ModalTopLevelComponentUnion> components = new ArrayList<>(MAX_COMPONENTS);
        private String id;
        private String title;

        protected Builder(@NotNull String customId, @NotNull String title) {
            setId(customId);
            setTitle(title);
        }

        /**
         * Sets the custom id for this modal.
         *
         * @param  customId
         *         Custom id
         *
         * @throws IllegalArgumentException
         *         If the provided id is null, blank, or is longer than {@value #MAX_ID_LENGTH} characters.
         *
         * @return The same builder instance for chaining
         */
        @NotNull
        public Builder setId(@NotNull String customId) {
            Checks.notBlank(customId, "ID");
            Checks.notLonger(customId, MAX_ID_LENGTH, "ID");
            this.id = customId;
            return this;
        }

        /**
         * Sets the title for this modal.
         *
         * @param  title
         *         The title
         *
         * @throws IllegalArgumentException
         *         If the provided title is null, blank or longer than {@value #MAX_TITLE_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @NotNull
        public Builder setTitle(@NotNull String title) {
            Checks.notBlank(title, "Title");
            Checks.notLonger(title, MAX_TITLE_LENGTH, "Title");
            this.title = title;
            return this;
        }

        /**
         * Adds {@link ModalTopLevelComponent components} to this modal
         *
         * @param  components
         *         {@link ModalTopLevelComponent Components} to add to the modal, up to {@value MAX_COMPONENTS} total
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If {@code null} is provided</li>
         *             <li>If any of the provided components are not {@linkplain Component.Type#isModalCompatible() compatible with modals}</li>
         *         </ul>
         *
         * @return The same builder instance for chaining
         *
         * @see    Component#isModalCompatible()
         * @see    ModalComponentTree
         */
        @NotNull
        public Builder addComponents(@NotNull ModalTopLevelComponent... components) {
            Checks.noneNull(components, "Components");
            return addComponents(Arrays.asList(components));
        }

        /**
         * Adds {@link ModalTopLevelComponent components} to this modal
         *
         * @param  components
         *         {@link ModalTopLevelComponent Components} to add to the modal, up to {@value MAX_COMPONENTS} total
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If {@code null} is provided</li>
         *             <li>If any of the provided components are not {@linkplain Component.Type#isModalCompatible() compatible with modals}</li>
         *         </ul>
         *
         * @return The same builder instance for chaining
         *
         * @see    Component#isModalCompatible()
         */
        @NotNull
        public Builder addComponents(@NotNull Collection<? extends ModalTopLevelComponent> components) {
            Checks.noneNull(components, "Components");
            Checks.checkComponents(
                    "Some components are incompatible with Modals", components, Component::isModalCompatible);

            this.components.addAll(membersToUnion(components));
            return this;
        }

        /**
         * Adds the provided {@link ComponentTree} of {@link ModalTopLevelComponent ModalTopLevelComponents} to this modal
         *
         * @param  tree
         *         The {@link ComponentTree} to add,
         *         containing up to {@value #MAX_COMPONENTS} V1 components.
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If {@code null} is provided</li>
         *             <li>If any of the provided components are not {@linkplain Component.Type#isModalCompatible() compatible with modals}</li>
         *         </ul>
         *
         * @return The same builder instance for chaining
         *
         * @see    Component#isModalCompatible()
         */
        @NotNull
        public Builder addComponents(@NotNull ComponentTree<? extends ModalTopLevelComponent> tree) {
            Checks.notNull(tree, "ModalComponentTree");
            return addComponents(tree.getComponents());
        }

        /**
         * Returns a modifiable list of all components
         *
         * @return A modifiable list of all components
         */
        @NotNull
        public List<ModalTopLevelComponentUnion> getComponents() {
            return components;
        }

        /**
         * Returns the title
         *
         * @return the title
         */
        @NotNull
        public String getTitle() {
            return title;
        }

        /**
         * Returns the custom id
         *
         * @return the id
         */
        @NotNull
        public String getId() {
            return id;
        }

        /**
         * Builds and returns the {@link Modal}
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If no components are added</li>
         *             <li>If more than {@value MAX_COMPONENTS} component layouts are added</li>
         *             <li>If any components are disabled</li>
         *         </ul>
         *
         * @return A Modal
         */
        @NotNull
        public Modal build() {
            Checks.check(!components.isEmpty(), "Cannot make a modal without components!");
            Checks.check(components.size() <= MAX_COMPONENTS, "Cannot make a modal with more than 5 components!");
            Checks.checkComponents("Components cannot be disabled in Modals", components, Builder::componentIsEnabled);
            return new ModalImpl(id, title, components);
        }

        private static Collection<ModalTopLevelComponentUnion> membersToUnion(
                Collection<? extends ModalTopLevelComponent> members) {
            return ComponentsUtil.membersToUnion(members, ModalTopLevelComponentUnion.class);
        }

        /**
         * Verifies Disabled Component state for a Modal,
         * based on the <a href="https://fluxer.com/developers/docs/components/reference">Component Reference</a>
         *
         * @param component Expected to be a {@link ModalTopLevelComponentUnion} Component
         *
         * @return false iff the Component is a {@link Label}, and the Child Component of the Label is Disabled.
         */
        private static boolean componentIsEnabled(Component component) {
            return !(component instanceof IDisableable) || ((IDisableable) component).isEnabled();
        }
    }
}
