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

package lonter.jfa.api.events.guild.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that the owner of a {@link lonter.jfa.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when an owner of a guild changes and retrieve the old one
 *
 * <p>Identifier: {@code owner}
 */
public class GuildUpdateOwnerEvent extends GenericGuildUpdateEvent<Member> {
    public static final String IDENTIFIER = "owner";
    private final long prevId, nextId;

    public GuildUpdateOwnerEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull Guild guild,
            @Nullable Member oldOwner,
            long prevId,
            long nextId) {
        super(api, responseNumber, guild, oldOwner, guild.getOwner(), IDENTIFIER);
        this.prevId = prevId;
        this.nextId = nextId;
    }

    /**
     * The new owner user id
     *
     * @return The new owner id
     */
    public long getNewOwnerIdLong() {
        return nextId;
    }

    /**
     * The new owner user id
     *
     * @return The new owner id
     */
    @NotNull
    public String getNewOwnerId() {
        return Long.toUnsignedString(nextId);
    }

    /**
     * The previous owner user id
     *
     * @return The previous owner id
     */
    public long getOldOwnerIdLong() {
        return prevId;
    }

    /**
     * The previous owner user id
     *
     * @return The previous owner id
     */
    @NotNull
    public String getOldOwnerId() {
        return Long.toUnsignedString(prevId);
    }

    /**
     * The old owner
     *
     * @return The old owner
     */
    @Nullable
    public Member getOldOwner() {
        return getOldValue();
    }

    /**
     * The new owner
     *
     * @return The new owner
     */
    @Nullable
    public Member getNewOwner() {
        return getNewValue();
    }
}
