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

package lonter.jfa.api.components.textinput;

import lonter.jfa.api.components.attribute.ICustomId;
import lonter.jfa.api.components.label.LabelChildComponent;
import lonter.jfa.internal.components.textinput.TextInputImpl;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Fluxer Text input component
 *
 * <p>Must be used inside {@link lonter.jfa.api.components.label.Label Labels} only!
 */
public interface TextInput extends ICustomId, LabelChildComponent {
    /**
     * The maximum length a TextInput value can have. ({@value})
     */
    int MAX_VALUE_LENGTH = 4000;

    /**
     * The maximum length a TextInput custom id can have. ({@value})
     */
    int MAX_ID_LENGTH = 100;

    /**
     * The maximum length a TextInput placeholder can have. ({@value})
     */
    int MAX_PLACEHOLDER_LENGTH = 100;

    /**
     * The {@link TextInputStyle} of this TextInput component.
     *
     * @return The style of this TextInput component.
     */
    @NotNull
    TextInputStyle getStyle();

    /**
     * The custom id of this TextInput component.
     *
     * <p>This is used to uniquely identify the TextInput. Similar to {@link lonter.jfa.api.components.buttons.Button Buttons}.
     *
     * @return The custom id of this component.
     */
    @NotNull
    @Override
    String getCustomId();

    /**
     * The minimum amount of characters that must be written to submit the Modal.
     *
     * <p><b>This is -1 if no length has been set!</b>
     *
     * @return The minimum length of this TextInput component or -1
     */
    int getMinLength();

    /**
     * The maximum amount of characters that can be written to submit the Modal.
     *
     * <p><b>This is -1 if no length has been set!</b>
     *
     * @return The maximum length of this TextInput component or -1
     */
    int getMaxLength();

    /**
     * Whether this TextInput is required to be non-empty.
     *
     * <p>This attribute is completely separate from the length range,
     * for example, you can have an optional text input with the range set to {@code [2 ; 5]},
     * meaning you accept either no input, or, at least 2 characters but at most 5.
     *
     * @return True if this TextInput is required to be used.
     */
    boolean isRequired();

    /**
     * The pre-defined value of this TextInput component.
     * <br>If this is not null, sending a Modal with this component will pre-populate the field with this String.
     *
     * <p><b>This is null if no pre-defined value has been set!</b>
     *
     * @return The value of this TextInput component or null.
     */
    @Nullable
    String getValue();

    /**
     * The placeholder of this TextInput component.
     * <br>This is a short hint that describes the expected value of the TextInput field.
     *
     * <p><b>This is null if no placeholder has been set!</b>
     *
     * @return The placeholder of this TextInput component or null.
     */
    @Nullable
    String getPlaceHolder();

    @NotNull
    @Override
    TextInput withUniqueId(int uniqueId);

    @NotNull
    @Override
    default Type getType() {
        return Type.TEXT_INPUT;
    }

    /**
     * Creates a new TextInput Builder.
     *
     * @param  id
     *         The custom id
     * @param  style
     *         The {@link TextInputStyle TextInputStyle}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If id is null or blank</li>
     *             <li>If style is null or {@link TextInputStyle#UNKNOWN UNKNOWN}</li>
     *             <li>If id is longer than {@value #MAX_ID_LENGTH} characters</li>
     *         </ul>
     *
     * @return The new TextInput Builder.
     */
    @NotNull
    static TextInput.Builder create(@NotNull String id, @NotNull TextInputStyle style) {
        return new Builder(id, style);
    }

    /**
     * Creates a new TextInput.
     * <br>This is a shortcut for {@code TextInput.create(id, style).build()}.
     *
     * @param  id
     *         The custom id
     * @param  style
     *         The {@link TextInputStyle TextInputStyle}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If id is null or blank</li>
     *             <li>If style is null or {@link TextInputStyle#UNKNOWN UNKNOWN}</li>
     *             <li>If id is longer than {@value #MAX_ID_LENGTH} characters</li>
     *         </ul>
     *
     * @return The new TextInput instance.
     */
    @NotNull
    static TextInput of(@NotNull String id, @NotNull TextInputStyle style) {
        return TextInput.create(id, style).build();
    }

    /**
     * Builder for {@link TextInput TextInputs}
     */
    class Builder {
        private String customId;
        private int uniqueId = -1;
        private String value;
        private String placeholder;
        private int minLength = -1;
        private int maxLength = -1;
        private TextInputStyle style;
        private boolean required = true;

        protected Builder(String customId, TextInputStyle style) {
            setCustomId(customId);
            setStyle(style);
        }

        /**
         * Sets the custom ID for this TextInput
         * <br>This can be used to uniquely identify it, or pass data to other handlers.
         *
         * @param  customId
         *         The custom ID to set
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If {@code customId} is null or blank</li>
         *             <li>If {@code customId} is longer than {@value #MAX_ID_LENGTH} characters</li>
         *         </ul>
         *
         * @return The same Builder for chaining convenience.
         */
        @NotNull
        public Builder setCustomId(@NotNull String customId) {
            Checks.notBlank(customId, "Custom ID");
            Checks.notLonger(customId, MAX_ID_LENGTH, "Custom ID");
            this.customId = customId;
            return this;
        }

        /**
         * Sets the unique ID for this TextInput
         * <br>This is used to uniquely identify it.
         *
         * @param  uniqueId
         *         The unique id to set
         *
         * @throws IllegalArgumentException
         *         If the id is negative
         *
         * @return The same Builder for chaining convenience.
         */
        @NotNull
        public Builder setUniqueId(int uniqueId) {
            Checks.positive(uniqueId, "Unique ID");
            this.uniqueId = uniqueId;
            return this;
        }

        /**
         * Sets the style for this TextInput
         * <br>Possible values are:
         * <ul>
         *     <li>{@link TextInputStyle#SHORT}</li>
         *     <li>{@link TextInputStyle#PARAGRAPH}</li>
         * </ul>
         *
         * @param  style
         *         The style to set
         *
         * @throws IllegalArgumentException
         *         If style is null or {@link TextInputStyle#UNKNOWN UNKNOWN}
         *
         * @return The same Builder for chaining convenience.
         */
        @NotNull
        public Builder setStyle(@NotNull TextInputStyle style) {
            Checks.notNull(style, "Style");
            Checks.check(style != TextInputStyle.UNKNOWN, "TextInputStyle cannot be UNKNOWN!");
            this.style = style;
            return this;
        }

        /**
         * Sets whether the user is required to write in this TextInput. Default is true.
         *
         * <p>This attribute is completely separate from the length range,
         * for example, you can have an optional text input with the range set to {@code [2 ; 5]},
         * meaning you accept either no input, or, at least 2 characters but at most 5.
         *
         * @param  required
         *         If this TextInput should be required
         *
         * @return The same builder instance for chaining
         */
        @NotNull
        public Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        /**
         * Sets the minimum length of this input field. Default is -1 (No minimum length).
         *
         * <p><b>This has to be between 0 and {@value #MAX_VALUE_LENGTH}, or -1 for no minimum length</b>
         *
         * @param  minLength
         *         The minimum amount of characters that need to be written, or -1
         *
         * @throws IllegalArgumentException
         *         If minLength is not -1 and is negative or greater than {@value #MAX_VALUE_LENGTH}
         *
         * @return The same builder instance for chaining
         */
        @NotNull
        public Builder setMinLength(int minLength) {
            if (minLength != -1) {
                Checks.notNegative(minLength, "Minimum length");
                Checks.check(
                        minLength <= MAX_VALUE_LENGTH,
                        "Minimum length cannot be longer than %d characters!",
                        MAX_VALUE_LENGTH);
            }

            this.minLength = minLength;
            return this;
        }

        /**
         * Sets the maximum length of this input field. Default is -1 (No maximum length).
         *
         * <p><b>This has to be between 1 and {@value #MAX_VALUE_LENGTH}, or -1 for no maximum length</b>
         *
         * @param  maxLength
         *         The maximum amount of characters that need to be written, or -1
         *
         * @throws IllegalArgumentException
         *         If maxLength is not -1 and is smaller than 1 or greater than {@value #MAX_VALUE_LENGTH}
         *
         * @return The same builder instance for chaining
         */
        @NotNull
        public Builder setMaxLength(int maxLength) {
            if (maxLength != -1) {
                Checks.check(maxLength >= 1, "Maximum length cannot be smaller than 1 character!");
                Checks.check(
                        maxLength <= MAX_VALUE_LENGTH,
                        "Maximum length cannot be longer than %d characters!",
                        MAX_VALUE_LENGTH);
            }

            this.maxLength = maxLength;
            return this;
        }

        /**
         * Sets the minimum and maximum required length on this TextInput component
         *
         * @param  min
         *         Minimum length of the text input, or -1 for none
         * @param  max
         *         Maximum length of the text input, or -1 for none
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If min is not -1 and is negative or greater than {@link #MAX_VALUE_LENGTH}</li>
         *             <li>If max is not -1 and is smaller than 1, smaller than min or greater than {@link #MAX_VALUE_LENGTH}</li>
         *         </ul>
         *
         * @return The same builder instance for chaining
         */
        @NotNull
        public Builder setRequiredRange(int min, int max) {
            if (min != -1 && max != -1 && min > max) {
                throw new IllegalArgumentException("minimum cannot be greater than maximum!");
            }

            setMinLength(min);
            setMaxLength(max);
            return this;
        }

        /**
         * Sets a pre-populated text for this TextInput field.
         * <br>If this is not null, sending a Modal with this component will pre-populate the TextInput field with the specified String.
         *
         * @param  value
         *         Pre-Populated text
         *
         * @return The same builder instance for chaining
         */
        @NotNull
        public Builder setValue(@Nullable String value) {
            if (value != null) {
                Checks.notLonger(value, MAX_VALUE_LENGTH, "Value");
                Checks.notBlank(value, "Value");
            }

            this.value = value;
            return this;
        }

        /**
         * Sets a placeholder for this TextInput field.
         * <br>This is a short hint that describes the expected value of the input field.
         *
         * @param  placeholder
         *         The placeholder
         *
         * @throws IllegalArgumentException
         *         If the provided placeholder is longer than {@link #MAX_PLACEHOLDER_LENGTH} characters
         *
         * @return The same builder instance for chaining
         */
        @NotNull
        public Builder setPlaceholder(@Nullable String placeholder) {
            if (placeholder != null) {
                Checks.notLonger(placeholder, MAX_PLACEHOLDER_LENGTH, "Placeholder");
                Checks.notBlank(placeholder, "Placeholder");
            }
            this.placeholder = placeholder;
            return this;
        }

        /**
         * The minimum length. This is -1 if none has been set.
         *
         * @return Minimum length or -1
         */
        public int getMinLength() {
            return minLength;
        }

        /**
         * The maximum length. This is -1 if none has been set.
         *
         * @return Maximum length or -1
         */
        public int getMaxLength() {
            return maxLength;
        }

        /**
         * The custom id
         *
         * @return Custom id
         */
        @NotNull
        public String getCustomId() {
            return customId;
        }

        /**
         * The numeric unique ID
         *
         * @return Unique ID
         */
        public int getUniqueId() {
            return uniqueId;
        }

        /**
         * The {@link TextInputStyle TextInputStyle}
         *
         * @return The TextInputStyle
         */
        @NotNull
        public TextInputStyle getStyle() {
            return style;
        }

        /**
         * The placeholder of this TextInput
         * <br>This is the short hint that describes the expected value of the TextInput field.
         *
         * @return Placeholder
         */
        @Nullable
        public String getPlaceholder() {
            return placeholder;
        }

        /**
         * The String value of this TextInput
         *
         * @return Value
         */
        @Nullable
        public String getValue() {
            return value;
        }

        /**
         * Whether this TextInput is required.
         * <br>If this is True, the user must populate this TextInput field before they can submit the Modal.
         *
         * <p>This attribute is completely separate from the length range,
         * for example, you can have an optional text input with the range set to {@code [2 ; 5]},
         * meaning you accept either no input, or, at least 2 characters but at most 5.
         *
         * @return True if this TextInput is required
         *
         * @see    TextInput#isRequired()
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Builds a new TextInput from this Builder
         *
         * @throws IllegalStateException
         *         If maxLength is smaller than minLength
         *
         * @return the TextInput instance
         */
        @NotNull
        public TextInput build() {
            if (maxLength < minLength && maxLength != -1) {
                throw new IllegalStateException("maxLength cannot be smaller than minLength!");
            }

            return new TextInputImpl(customId, uniqueId, style, minLength, maxLength, required, value, placeholder);
        }
    }
}
