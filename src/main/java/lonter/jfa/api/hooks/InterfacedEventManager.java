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
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.utils.JFALogger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jetbrains.annotations.NotNull;

/**
 * An {@link lonter.jfa.api.hooks.IEventManager IEventManager} implementation
 * that uses the {@link lonter.jfa.api.hooks.EventListener EventListener} interface for
 * event listeners.
 *
 * <p>This only accepts listeners that implement {@link lonter.jfa.api.hooks.EventListener EventListener}
 * <br>An adapter implementation is {@link lonter.jfa.api.hooks.ListenerAdapter ListenerAdapter} which
 * provides methods for each individual {@link lonter.jfa.api.events.Event}.
 *
 * <p><b>This is the default IEventManager used by JFA</b>
 *
 * @see lonter.jfa.api.hooks.AnnotatedEventManager
 * @see lonter.jfa.api.hooks.IEventManager
 */
public class InterfacedEventManager implements IEventManager {
    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

    public InterfacedEventManager() {}

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException
     *         If the provided listener does not implement {@link lonter.jfa.api.hooks.EventListener EventListener}
     */
    @Override
    public void register(@NotNull Object listener) {
        if (!(listener instanceof EventListener)) {
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        listeners.add((EventListener) listener);
    }

    @Override
    public void unregister(@NotNull Object listener) {
        if (!(listener instanceof EventListener)) {
            //noinspection ConstantConditions
            JFALogger.getLog(getClass())
                    .warn(
                            "Trying to remove a listener that does not implement EventListener: {}",
                            listener == null ? "null" : listener.getClass().getName());
        }

        //noinspection SuspiciousMethodCalls
        listeners.remove(listener);
    }

    @NotNull
    @Override
    @Unmodifiable
    public List<Object> getRegisteredListeners() {
        return Collections.unmodifiableList(new ArrayList<>(listeners));
    }

    @Override
    public void handle(@NotNull GenericEvent event) {
        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Throwable throwable) {
                JFAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                }
            }
        }
    }
}
