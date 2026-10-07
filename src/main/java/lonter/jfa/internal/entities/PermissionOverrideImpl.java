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

package lonter.jfa.internal.entities;

import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.*;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.unions.IPermissionContainerUnion;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.requests.restaction.PermissionOverrideAction;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.requests.restaction.AuditableRestActionImpl;
import lonter.jfa.internal.requests.restaction.PermissionOverrideActionImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;

import java.util.EnumSet;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class PermissionOverrideImpl implements PermissionOverride {
    private final long id;
    private final boolean isRole;
    private final JFAImpl api;
    private IPermissionContainer channel;

    private long allow;
    private long deny;

    public PermissionOverrideImpl(IPermissionContainer channel, long id, boolean isRole) {
        this.isRole = isRole;
        this.api = (JFAImpl) channel.getJFA();
        this.channel = channel;
        this.id = id;
    }

    @Override
    public long getAllowedRaw() {
        return allow;
    }

    @Override
    public long getInheritRaw() {
        return ~(allow | deny);
    }

    @Override
    public long getDeniedRaw() {
        return deny;
    }

    @NotNull
    @Override
    public EnumSet<Permission> getAllowed() {
        return Permission.getPermissions(allow);
    }

    @NotNull
    @Override
    public EnumSet<Permission> getInherit() {
        return Permission.getPermissions(getInheritRaw());
    }

    @NotNull
    @Override
    public EnumSet<Permission> getDenied() {
        return Permission.getPermissions(deny);
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @Override
    public IPermissionHolder getPermissionHolder() {
        return isRole ? getRole() : getMember();
    }

    @Override
    public Member getMember() {
        return getGuild().getMemberById(id);
    }

    @Override
    public Role getRole() {
        return getGuild().getRoleById(id);
    }

    @NotNull
    @Override
    public IPermissionContainerUnion getChannel() {
        IPermissionContainer realChannel = api.getChannelById(IPermissionContainer.class, channel.getIdLong());
        if (realChannel != null) {
            channel = realChannel;
        }

        return (IPermissionContainerUnion) channel;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return getChannel().getGuild();
    }

    @Override
    public boolean isMemberOverride() {
        return !isRole;
    }

    @Override
    public boolean isRoleOverride() {
        return isRole;
    }

    @NotNull
    @Override
    public PermissionOverrideAction getManager() {
        checkPermissions();
        return new PermissionOverrideActionImpl(this).setOverride(false);
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> delete() {
        checkPermissions();

        Route.CompiledRoute route = Route.Channels.DELETE_PERM_OVERRIDE.compile(this.channel.getId(), getId());
        return new AuditableRestActionImpl<>(getJFA(), route);
    }

    @Override
    public long getIdLong() {
        return id;
    }

    public PermissionOverrideImpl setAllow(long allow) {
        this.allow = allow;
        return this;
    }

    public PermissionOverrideImpl setDeny(long deny) {
        this.deny = deny;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PermissionOverrideImpl)) {
            return false;
        }
        PermissionOverrideImpl oPerm = (PermissionOverrideImpl) o;
        return id == oPerm.id && this.channel.getIdLong() == oPerm.channel.getIdLong();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, channel.getIdLong());
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .setType(isMemberOverride() ? "MEMBER" : "ROLE")
                .addMetadata("channel", channel)
                .toString();
    }

    private void checkPermissions() {
        Member selfMember = getGuild().getSelfMember();
        IPermissionContainer channel = getChannel();
        Checks.checkAccess(selfMember, channel);
        if (!selfMember.hasPermission(channel, Permission.MANAGE_PERMISSIONS)) {
            throw new InsufficientPermissionException(channel, Permission.MANAGE_PERMISSIONS);
        }
    }
}
