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

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that JFA encountered a Throwable that could not be forwarded to another end-user frontend.
 * <br>For instance this is fired for events in internal WebSocket handling or audio threads.
 * This includes {@link java.lang.Error Errors} and {@link com.neovisionaries.ws.client.WebSocketException WebSocketExceptions}
 *
 * <p>It is not recommended to simply use this and print each event as some throwables were already logged
 * by JFA. See {@link #isLogged()}.
 */
public class ExceptionEvent extends Event {
    protected final Throwable throwable;
    protected final boolean logged;

    public ExceptionEvent(@NotNull JFA api, @NotNull Throwable throwable, boolean logged) {
        super(api);
        this.throwable = throwable;
        this.logged = logged;
    }

    /**
     * Whether this Throwable was already printed using the JFA logging system
     *
     * @return True, if this throwable was already logged
     */
    public boolean isLogged() {
        return logged;
    }

    /**
     * The cause Throwable for this event
     *
     * @return The cause
     */
    @NotNull
    public Throwable getCause() {
        return throwable;
    }
}
