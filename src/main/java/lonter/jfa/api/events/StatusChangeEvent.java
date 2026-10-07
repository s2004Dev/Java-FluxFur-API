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
 * Indicates that our {@link lonter.jfa.api.JFA.Status Status} changed. (Example: SHUTTING_DOWN {@literal ->} SHUTDOWN)
 *
 * <br>Can be used to detect internal status changes. Possibly to log or forward on user's end.
 *
 * <p>Identifier: {@code status}
 */
public class StatusChangeEvent extends Event implements UpdateEvent<JFA, JFA.Status> {
    public static final String IDENTIFIER = "status";

    protected final JFA.Status newStatus;
    protected final JFA.Status oldStatus;

    public StatusChangeEvent(@NotNull JFA api, @NotNull JFA.Status newStatus, @NotNull JFA.Status oldStatus) {
        super(api);
        this.newStatus = newStatus;
        this.oldStatus = oldStatus;
    }

    /**
     * The status that we changed to
     *
     * @return The new status
     */
    @NotNull
    public JFA.Status getNewStatus() {
        return newStatus;
    }

    /**
     * The previous status
     *
     * @return The previous status
     */
    @NotNull
    public JFA.Status getOldStatus() {
        return oldStatus;
    }

    @NotNull
    @Override
    public String getPropertyIdentifier() {
        return IDENTIFIER;
    }

    @NotNull
    @Override
    public JFA getEntity() {
        return getJFA();
    }

    @NotNull
    @Override
    public JFA.Status getOldValue() {
        return oldStatus;
    }

    @NotNull
    @Override
    public JFA.Status getNewValue() {
        return newStatus;
    }
}
