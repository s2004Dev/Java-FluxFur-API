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

package lonter.jfa.api.events.guild.override;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.IPermissionHolder;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.unions.IPermissionContainerUnion;
import lonter.jfa.api.events.guild.GenericGuildEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a {@link PermissionOverride} for a {@link GuildChannel GuildChannel} was created, deleted, or updated.
 * <br>Every guild channel override event is a subclass of this event and can be casted
 *
 * <p>Can be used to detect that any guild channel override event was fired
 */
public class GenericPermissionOverrideEvent extends GenericGuildEvent {
    protected final IPermissionContainer channel;
    protected final PermissionOverride override;

    public GenericPermissionOverrideEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull IPermissionContainer channel,
            @NotNull PermissionOverride override) {
        super(api, responseNumber, channel.getGuild());
        this.channel = channel;
        this.override = override;
    }

    /**
     * The {@link ChannelType} of the {@link #getChannel() GuildChannel} this override belongs to.
     *
     * @return The {@link ChannelType}
     */
    @NotNull
    public ChannelType getChannelType() {
        return channel.getType();
    }

    /**
     * The {@link IPermissionContainer guild channel} this override belongs to.
     *
     * @return The {@link IPermissionContainer channel}
     */
    @NotNull
    public IPermissionContainerUnion getChannel() {
        return (IPermissionContainerUnion) channel;
    }

    /**
     * The affected {@link PermissionOverride} that was updated.
     *
     * @return The override
     */
    @NotNull
    public PermissionOverride getPermissionOverride() {
        return override;
    }

    /**
     * Whether this override was for a role.
     * <br>This means {@link #getRole()} is likely not null.
     *
     * @return True, if this override is for a role
     */
    public boolean isRoleOverride() {
        return override.isRoleOverride();
    }

    /**
     * Whether this override was for a member.
     * <br>Note that {@link #getMember()} might still be null if the member isn't cached or there is a fluxer inconsistency.
     *
     * @return True, if this override is for a member
     */
    public boolean isMemberOverride() {
        return override.isMemberOverride();
    }

    /**
     * The {@link IPermissionHolder} for the override.
     * <br>This can be a {@link Member} or {@link Role}. If the role or member are not cached then this will be null.
     *
     * @return Possibly-null permission holder
     */
    @Nullable
    public IPermissionHolder getPermissionHolder() {
        return isMemberOverride() ? override.getMember() : override.getRole();
    }

    /**
     * The {@link Member} for the override.
     * <br>This can be null if the member is not cached or there is a fluxer inconsistency.
     *
     * @return Possibly-null member
     */
    @Nullable
    public Member getMember() {
        return override.getMember();
    }

    /**
     * The {@link Role} for the override.
     *
     * @return Possibly-null role
     */
    @Nullable
    public Role getRole() {
        return override.getRole();
    }
}
