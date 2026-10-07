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
 * The core events that are fired by this library, informing
 * the end-user about the state of the current JFA instance.
 *
 * <p>This package contains all implementations of {@link lonter.jfa.api.events.Event Event}.
 * <br>These are specific depending on the event that has been received by the gateway connection.
 *
 * <p>All events are forwarded by an {@link lonter.jfa.api.hooks.IEventManager IEventManager} implementation.
 * <br>Some events are specific for JFA internal events such as the {@link lonter.jfa.api.events.session.ReadyEvent ReadyEvent}
 * which is only fired when JFA finishes to setup its internal cache.
 */
package lonter.jfa.api.events;
