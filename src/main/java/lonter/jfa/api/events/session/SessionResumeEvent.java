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

package lonter.jfa.api.events.session;

import lonter.jfa.api.JFA;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that JFA successfully resumed its connection to the gateway.
 * <br>All Objects are still in place and events are replayed.
 *
 * <p>Can be used to detect the continuation of event flow stopped by the {@link SessionDisconnectEvent}.
 */
public class SessionResumeEvent extends GenericSessionEvent {
    public SessionResumeEvent(@NotNull JFA api) {
        super(api, SessionState.RESUMED);
    }
}
