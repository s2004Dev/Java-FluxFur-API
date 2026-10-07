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

package lonter.jfa.api.events.interaction.command;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.events.guild.GenericGuildEvent;
import lonter.jfa.api.interactions.commands.privileges.IntegrationPrivilege;
import lonter.jfa.api.interactions.commands.privileges.PrivilegeTargetType;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the privileges of an integration or its commands changed.
 *
 * <p>Can be used to get affected {@link Guild} and the new {@link IntegrationPrivilege IntegrationPrivileges}
 */
public abstract class GenericPrivilegeUpdateEvent extends GenericGuildEvent {
    private final long targetId;
    private final long applicationId;
    private final List<IntegrationPrivilege> privileges;

    public GenericPrivilegeUpdateEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull Guild guild,
            long targetId,
            long applicationId,
            @NotNull List<IntegrationPrivilege> privileges) {
        super(api, responseNumber, guild);
        this.targetId = targetId;
        this.applicationId = applicationId;
        this.privileges = Collections.unmodifiableList(privileges);
    }

    /**
     * The target {@link PrivilegeTargetType Type}.
     *
     * <p>This can either be:
     * <ul>
     *     <li>{@link PrivilegeTargetType#INTEGRATION INTEGRATION} - If the privileges have been changed on the integration-level.</li>
     *     <li>{@link PrivilegeTargetType#COMMAND COMMAND} - If the privileges have been changed on a command.</li>
     * </ul>
     *
     * @return The target type.
     */
    @NotNull
    public abstract PrivilegeTargetType getTargetType();

    /**
     * The target-id.
     *
     * <p>This can either be the id of an integration, or of a command.
     *
     * @return The target-id.
     *
     * @see #getTargetType()
     */
    public long getTargetIdLong() {
        return targetId;
    }

    /**
     * The target-id.
     *
     * <p>This can either be the id of an integration, or of a command.
     *
     * @return The target-id.
     *
     * @see #getTargetType()
     */
    @NotNull
    public String getTargetId() {
        return Long.toUnsignedString(targetId);
    }

    /**
     * The id of the application of which privileges have been changed.
     *
     * @return id of the application of which privileges have been changed.
     */
    public long getApplicationIdLong() {
        return applicationId;
    }

    /**
     * The id of the application of which privileges have been changed.
     *
     * @return id of the application of which privileges have been changed.
     */
    @NotNull
    public String getApplicationId() {
        return Long.toUnsignedString(applicationId);
    }

    /**
     * The list of new {@link IntegrationPrivilege IntegrationPrivileges}.
     *
     * @return Unmodifiable list containing the new IntegrationPrivileges.
     */
    @NotNull
    public List<IntegrationPrivilege> getPrivileges() {
        return privileges;
    }
}
