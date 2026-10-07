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
import lonter.jfa.api.requests.CloseCode;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that JFA has fully disconnected from Fluxer and will not attempt to reconnect again.
 * <br>At this stage all internal cache is invalid!
 */
public class ShutdownEvent extends GenericSessionEvent {
    protected final OffsetDateTime shutdownTime;
    protected final int code;

    public ShutdownEvent(@NotNull JFA api, @NotNull OffsetDateTime shutdownTime, int code) {
        super(api, SessionState.SHUTDOWN);
        this.shutdownTime = shutdownTime;
        this.code = code;
    }

    /**
     * Time of WebSocket disconnect
     *
     * @return {@link java.time.OffsetDateTime OffsetDateTime} representing
     *         the point in time when the connection was dropped.
     */
    @NotNull
    public OffsetDateTime getTimeShutdown() {
        return shutdownTime;
    }

    /**
     * Possibly-null {@link lonter.jfa.api.requests.CloseCode CloseCode}
     * representing the meaning for this ShutdownEvent.
     * <br>The raw close code can be retrieved from {@link #getCode()}
     * <br>If this is {@code null}, JFA does not know what the meaning for the connection loss was.
     *
     * @return Possibly-null {@link lonter.jfa.api.requests.CloseCode CloseCode}
     */
    @Nullable
    public CloseCode getCloseCode() {
        return CloseCode.from(code);
    }

    /**
     * The server close code that was in the disconnect close frame
     * of this JFA instance.
     *
     * @return int close code of the Server Close-Frame
     */
    public int getCode() {
        return code;
    }
}
