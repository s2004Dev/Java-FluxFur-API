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

package lonter.jfa.api.events;

import lonter.jfa.api.JFA;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.handle.SocketHandler;
import lonter.jfa.internal.utils.EntityString;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Top-level event type
 * <br>All events JFA fires are derived from this class.
 *
 * <p>Can be used to check if an Object is a JFA event in {@link lonter.jfa.api.hooks.EventListener EventListener} implementations to distinguish what event is being fired.
 * <br>Adapter implementation: {@link lonter.jfa.api.hooks.ListenerAdapter ListenerAdapter}
 */
public abstract class Event implements GenericEvent {
    protected final JFA api;
    protected final long responseNumber;
    protected final DataObject rawData;

    /**
     * Creates a new Event from the given JFA instance
     *
     * @param api
     *        Current JFA instance
     * @param responseNumber
     *        The sequence number for this event
     *
     * @see   #Event(lonter.jfa.api.JFA)
     */
    public Event(@NotNull JFA api, long responseNumber) {
        this.api = api;
        this.responseNumber = responseNumber;
        this.rawData = api instanceof JFAImpl && ((JFAImpl) api).isEventPassthrough()
                ? SocketHandler.CURRENT_EVENT.get()
                : null;
    }

    /**
     * Creates a new Event from the given JFA instance
     * <br>Uses the current {@link lonter.jfa.api.JFA#getResponseTotal()} as sequence
     *
     * @param api
     *        Current JFA instance
     */
    public Event(@NotNull JFA api) {
        this(api, api.getResponseTotal());
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @Override
    public long getResponseNumber() {
        return responseNumber;
    }

    @Nullable
    @Override
    public DataObject getRawData() {
        if (api instanceof JFAImpl) {
            if (!((JFAImpl) api).isEventPassthrough()) {
                throw new IllegalStateException(
                        "Event passthrough is not enabled, see JFABuilder#setEventPassthrough(boolean)");
            }
        }

        return rawData;
    }

    @Override
    public String toString() {
        if (this instanceof UpdateEvent<?, ?>) {
            UpdateEvent<?, ?> event = (UpdateEvent<?, ?>) this;
            return new EntityString(this)
                    .setType(event.getPropertyIdentifier())
                    .addMetadata(null, event.getOldValue() + " -> " + event.getNewValue())
                    .toString();
        } else {
            return new EntityString(this).toString();
        }
    }
}
