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

package lonter.jfa.api.entities.channel.attribute;

import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.concrete.Category;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.concrete.VoiceChannel;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link GuildChannel} that is capable of containing members.
 *
 * <p>Implementations interpret this meaning as best applies to them:
 *
 * <p>For example,
 * <ul>
 *   <li>{@link TextChannel TextChannels} implement this as the {@link lonter.jfa.api.entities.Member members} that have {@link lonter.jfa.api.Permission#VIEW_CHANNEL}</li>
 *   <li>{@link VoiceChannel VoiceChannels} implement this as what {@link lonter.jfa.api.entities.Member members} are currently connected to the channel.</li>
 * </ul>
 *
 * @see IMemberContainer#getMembers()
 */
public interface IMemberContainer extends GuildChannel {
    /**
     * A List of all {@link lonter.jfa.api.entities.Member Members} that are in this GuildChannel
     * <br>For {@link TextChannel TextChannels},
     * this returns all Members with the {@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} Permission.
     * <br>For {@link VoiceChannel VoiceChannels},
     * this returns all Members that joined that VoiceChannel.
     * <br>For {@link Category Categories},
     * this returns all Members who are in its child channels.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return An immutable List of {@link lonter.jfa.api.entities.Member Members} that are in this GuildChannel.
     */
    @NotNull
    @Unmodifiable
    List<Member> getMembers();
}
