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
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.handle.GuildSetupController;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that JFA finished loading all entities.
 * <br>Before this event was fired all entity related functions were not guaranteed to work as expected.
 *
 * <p>Can be used to indicate when JFA finished populating internal objects and is ready to be used.
 * When this is fired all <b>available</b> entities are cached and accessible.
 */
public class ReadyEvent extends GenericSessionEvent {
    private final int availableGuilds;
    private final int unavailableGuilds;

    public ReadyEvent(@NotNull JFA api) {
        super(api, SessionState.READY);
        this.availableGuilds = (int) getJFA().getGuildCache().size();
        GuildSetupController setupController = ((JFAImpl) getJFA()).getGuildSetupController();
        this.unavailableGuilds = setupController
                        .getSetupNodes(GuildSetupController.Status.UNAVAILABLE)
                        .size()
                + setupController.getUnavailableGuilds().size();
    }

    /**
     * Number of available guilds for this session.
     * <br>When fluxer fails to connect guilds for our gateway session they will not be in cache here yet
     * but instead will fire a {@link lonter.jfa.api.events.guild.GuildReadyEvent GuildReadyEvent} later.
     *
     * @return Number of available guilds for this session
     *
     * @see    #getGuildTotalCount()
     * @see    #getGuildUnavailableCount()
     */
    public int getGuildAvailableCount() {
        return availableGuilds;
    }

    /**
     * Number of guilds currently not available to this session
     * <br>Fluxer failed to connect these guilds to our gateway and we had to discard them for now.
     * These might become available again later and will then fire a {@link lonter.jfa.api.events.guild.GuildReadyEvent GuildReadyEvent}.
     *
     * @return Number of currently unavailable guilds
     */
    public int getGuildUnavailableCount() {
        return unavailableGuilds;
    }

    /**
     * Sum of both {@link #getGuildAvailableCount()} and {@link #getGuildUnavailableCount()}.
     *
     * @return Total numbers of guilds known to this JFA session
     */
    public int getGuildTotalCount() {
        return getGuildAvailableCount() + getGuildUnavailableCount();
    }
}
