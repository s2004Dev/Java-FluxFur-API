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

package lonter.jfa.api.events.guild;

import lonter.jfa.api.JFA;
import lonter.jfa.api.audit.AuditLogEntry;
import lonter.jfa.api.entities.Guild;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that an {@link AuditLogEntry} was added to a {@link Guild}.
 *
 * <p>This never provides a {@link AuditLogEntry#getUser() responsible user} instance.
 * You can use {@link AuditLogEntry#getUserIdLong()} instead.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_MODERATION GUILD_MODERATION} intent to be enabled.
 */
public class GuildAuditLogEntryCreateEvent extends GenericGuildEvent {
    private final AuditLogEntry entry;

    public GuildAuditLogEntryCreateEvent(@NotNull JFA api, long responseNumber, @NotNull AuditLogEntry entry) {
        super(api, responseNumber, entry.getGuild());
        this.entry = entry;
    }

    /**
     * The {@link AuditLogEntry} that was added to the {@link Guild}
     *
     * @return The added entry
     */
    @NotNull
    public AuditLogEntry getEntry() {
        return entry;
    }
}
