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

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for JFA's EventManager system.
 * <br>This should be registered in the {@link lonter.jfa.api.JFABuilder JFABuilder}
 *
 * <p>JFA provides 2 implementations:
 * <ul>
 *     <li>{@link lonter.jfa.api.hooks.InterfacedEventManager InterfacedEventManager}
 *     <br>Simple implementation that allows {@link lonter.jfa.api.hooks.EventListener EventListener}
 *         instances as listeners.</li>
 *
 *     <li>{@link lonter.jfa.api.hooks.AnnotatedEventManager AnnotatedEventManager}
 *     <br>An implementation that accepts any object and uses the {@link lonter.jfa.api.hooks.SubscribeEvent SubscribeEvent}
 *         annotation to handle events.</li>
 * </ul>
 *
 * <p>The default event manager is {@link lonter.jfa.api.hooks.InterfacedEventManager InterfacedEventManager}
 * <br>Use {@link lonter.jfa.api.JFABuilder#setEventManager(IEventManager) JFABuilder.setEventManager(IEventManager)}
 * to set the preferred event manager implementation.
 * <br>You can only use one implementation per JFA instance!
 *
 * @see lonter.jfa.api.hooks.InterfacedEventManager
 * @see lonter.jfa.api.hooks.AnnotatedEventManager
 */
public interface IEventManager {
    /**
     * Registers the specified listener
     * <br>Accepted types may be specified by implementations
     *
     * @param listener
     *        A listener object
     *
     * @throws java.lang.UnsupportedOperationException
     *         If the implementation does not support this method
     */
    void register(@NotNull Object listener);

    /**
     * Removes the specified listener
     *
     * @param listener
     *        The listener object to remove
     *
     * @throws java.lang.UnsupportedOperationException
     *         If the implementation does not support this method
     */
    void unregister(@NotNull Object listener);

    /**
     * Handles the provided {@link lonter.jfa.api.events.GenericEvent GenericEvent}.
     * <br>How this is handled is specified by the implementation.
     *
     * <p>An implementation should not throw exceptions.
     *
     * @param event
     *        The event to handle
     */
    void handle(@NotNull GenericEvent event);

    /**
     * The currently registered listeners
     *
     * @throws java.lang.UnsupportedOperationException
     *         If the implementation does not support this method
     *
     * @return A list of listeners that have already been registered
     */
    @NotNull
    List<Object> getRegisteredListeners();
}
