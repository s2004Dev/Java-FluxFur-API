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

package lonter.jfa.api.components.textdisplay;

import lonter.jfa.api.components.Component;
import lonter.jfa.api.components.MessageTopLevelComponent;
import lonter.jfa.api.components.ModalTopLevelComponent;
import lonter.jfa.api.components.container.ContainerChildComponent;
import lonter.jfa.api.components.section.SectionContentComponent;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.utils.messages.MessageRequest;
import lonter.jfa.internal.components.textdisplay.TextDisplayImpl;
import lonter.jfa.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * A component to display text, supports Markdown.
 *
 * <p>This component has no content length limit, however,
 * you are still limited to the {@value Message#MAX_CONTENT_LENGTH_COMPONENT_V2} total characters,
 * as imposed by {@linkplain MessageRequest#useComponentsV2() components V2}.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} needs to be enabled!
 */
public interface TextDisplay
        extends Component,
                MessageTopLevelComponent,
                ModalTopLevelComponent,
                ContainerChildComponent,
                SectionContentComponent {
    /**
     * Constructs a new {@link TextDisplay} from the given content.
     *
     * @param  content
     *         The content of the text display
     *
     * @throws IllegalArgumentException
     *         If the content is {@code null}, blank or empty
     *
     * @return The new {@link TextDisplay}
     */
    @NotNull
    static TextDisplay of(@NotNull String content) {
        Checks.notBlank(content, "Content");
        return new TextDisplayImpl(content);
    }

    /**
     * Constructs a new {@link TextDisplay} from the given format string and arguments.
     *
     * @param  format
     *         The content to format with the following arguments
     * @param  args
     *         The arguments to format the content with,
     *         if there are more arguments than format specifiers, the extra arguments are ignored.
     *         The number of arguments is variable and may be zero.
     *         The behaviour on a {@code null} argument depends on the <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax" target="_blank">conversion</a>.
     *
     * @throws IllegalArgumentException
     *         If the format string is {@code null}, or the resulting content is blank
     * @throws java.util.IllegalFormatException
     *         If the format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#detail" target="_blank">Details</a> section of the formatter class specification.
     *
     * @return The new {@link TextDisplay}
     */
    @NotNull
    static TextDisplay ofFormat(@NotNull String format, @NotNull Object... args) {
        Checks.notNull(format, "Format string");
        Checks.notNull(args, "Format args"); // Null array elements are allowed
        return of(String.format(format, args));
    }

    @NotNull
    @Override
    @CheckReturnValue
    TextDisplay withUniqueId(int uniqueId);

    /**
     * Creates a new {@link TextDisplay} with the specified content.
     *
     * <p>While there are no per-component limit,
     * you are still limited to the {@value Message#MAX_CONTENT_LENGTH_COMPONENT_V2} total character limit.
     *
     * @param  content
     *         The new content
     *
     * @return The new {@link TextDisplay}
     */
    @NotNull
    @CheckReturnValue
    TextDisplay withContent(@NotNull String content);

    /**
     * The content of this {@link TextDisplay}.
     *
     * @return The content
     */
    @NotNull
    String getContent();
}
