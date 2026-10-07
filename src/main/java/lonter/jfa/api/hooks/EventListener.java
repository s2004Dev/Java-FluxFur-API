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

package lonter.jfa.api.hooks;

import lonter.jfa.api.events.GenericEvent;

import org.jetbrains.annotations.NotNull;

/**
 * JFA pushes {@link lonter.jfa.api.events.GenericEvent GenericEvents} to the registered EventListeners.
 *
 * <p>Register an EventListener with either a {@link lonter.jfa.api.JFA JFA} object
 * <br>or the {@link lonter.jfa.api.JFABuilder JFABuilder}.
 *
 * <p><b>Examples: </b>
 * <br>
 * <code>
 *     JFA jfa = {@link lonter.jfa.api.JFABuilder JFABuilder}.createDefault("token").{@link lonter.jfa.api.JFABuilder#addEventListeners(Object...) addEventListeners(listeners)}.{@link lonter.jfa.api.JFABuilder#build() build()};<br>
 *     {@link lonter.jfa.api.JFA#addEventListener(Object...) jfa.addEventListener(listeners)};
 * </code>
 *
 * @see lonter.jfa.api.hooks.ListenerAdapter
 * @see lonter.jfa.api.hooks.InterfacedEventManager
 */
@FunctionalInterface
public interface EventListener {
    /**
     * Handles any {@link lonter.jfa.api.events.GenericEvent GenericEvent}.
     *
     * <p>To get specific events with Methods like {@code onMessageReceived(MessageReceivedEvent event)}
     * take a look at: {@link lonter.jfa.api.hooks.ListenerAdapter ListenerAdapter}
     *
     * @param  event
     *         The Event to handle.
     */
    void onEvent(@NotNull GenericEvent event);
}
