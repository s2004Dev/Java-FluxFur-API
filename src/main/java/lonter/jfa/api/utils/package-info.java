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

/**
 * Package which contains all utilities for the JFA library.
 * These are used by JFA itself and can also be useful for the library user!
 *
 * <p>List of utilities:
 * <ul>
 *     <li>{@link lonter.jfa.api.utils.MiscUtil MiscUtil}
 *     <br>Various operations that don't have specific utility classes yet, mostly internals that are accessible from JFA entities</li>
 *
 *     <li>{@link lonter.jfa.api.utils.WidgetUtil WidgetUtil}
 *     <br>This is not bound to a JFA instance and can view the {@link lonter.jfa.api.entities.Widget Widget}
 *         for a specified Guild. (by id)</li>
 *
 *     <li>{@link lonter.jfa.api.utils.MarkdownSanitizer MarkdownSanitizer}
 *     <br>Parser for Fluxer markdown that can either escape or strip markdown from a string</li>
 *
 *     <li>{@link lonter.jfa.api.utils.SessionController SessionController}
 *     <br>Special handler for session (re-)connects and global rate-limits</li>
 *
 *     <li>{@link lonter.jfa.api.utils.TimeUtil TimeUtil}
 *     <br>Useful time conversion methods related to Fluxer</li>
 * </ul>
 */
package lonter.jfa.api.utils;
