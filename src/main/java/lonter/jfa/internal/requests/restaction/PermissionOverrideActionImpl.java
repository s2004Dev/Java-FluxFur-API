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

package lonter.jfa.internal.requests.restaction;

import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.IPermissionHolder;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.PermissionOverrideAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.PermissionOverrideImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.PermissionUtil;
import okhttp3.RequestBody;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public class PermissionOverrideActionImpl extends AuditableRestActionImpl<PermissionOverride>
        implements PermissionOverrideAction {
    private boolean isOverride = true;
    private boolean allowSet = false;
    private boolean denySet = false;

    private long allow = 0;
    private long deny = 0;
    private final IPermissionContainerMixin<?> channel;
    private final IPermissionHolder permissionHolder;
    private final boolean isRole;
    private final long id;

    public PermissionOverrideActionImpl(PermissionOverride override) {
        super(
                override.getJFA(),
                Route.Channels.MODIFY_PERM_OVERRIDE.compile(
                        override.getChannel().getId(), override.getId()));
        this.channel = (IPermissionContainerMixin<?>) override.getChannel();
        this.permissionHolder = override.getPermissionHolder();
        this.isRole = override.isRoleOverride();
        this.id = override.getIdLong();
    }

    public PermissionOverrideActionImpl(JFA api, GuildChannel channel, IPermissionHolder permissionHolder) {
        super(api, Route.Channels.CREATE_PERM_OVERRIDE.compile(channel.getId(), permissionHolder.getId()));
        this.channel = (IPermissionContainerMixin<?>) channel;
        this.permissionHolder = permissionHolder;
        this.isRole = permissionHolder instanceof Role;
        this.id = permissionHolder.getIdLong();
    }

    // Whether to keep original value of the current override or not
    // by default we override the value
    public PermissionOverrideActionImpl setOverride(boolean override) {
        isOverride = override;
        return this;
    }

    @Override
    protected BooleanSupplier finalizeChecks() {
        return () -> {
            Member selfMember = getGuild().getSelfMember();
            Checks.checkAccess(selfMember, channel);
            if (!selfMember.hasPermission(channel, Permission.MANAGE_PERMISSIONS)) {
                throw new InsufficientPermissionException(channel, Permission.MANAGE_PERMISSIONS);
            }
            return true;
        };
    }

    @NotNull
    @Override
    public PermissionOverrideActionImpl setCheck(BooleanSupplier checks) {
        return (PermissionOverrideActionImpl) super.setCheck(checks);
    }

    @NotNull
    @Override
    public PermissionOverrideActionImpl timeout(long timeout, @NotNull TimeUnit unit) {
        return (PermissionOverrideActionImpl) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    public PermissionOverrideActionImpl deadline(long timestamp) {
        return (PermissionOverrideActionImpl) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public PermissionOverrideAction resetAllow() {
        allow = getOriginalAllow();
        allowSet = false;
        return this;
    }

    @NotNull
    @Override
    public PermissionOverrideAction resetDeny() {
        deny = getOriginalDeny();
        denySet = false;
        return this;
    }

    @NotNull
    @Override
    public IPermissionContainer getChannel() {
        return channel;
    }

    @Override
    public Role getRole() {
        return isRole() ? (Role) permissionHolder : null;
    }

    @Override
    public Member getMember() {
        return isMember() ? (Member) permissionHolder : null;
    }

    @Override
    public long getAllowed() {
        return getCurrentAllow();
    }

    @Override
    public long getDenied() {
        return getCurrentDeny();
    }

    @Override
    public long getInherited() {
        return ~getAllowed() & ~getDenied();
    }

    @Override
    public boolean isMember() {
        return !isRole;
    }

    @Override
    public boolean isRole() {
        return isRole;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public PermissionOverrideActionImpl setAllowed(long allowBits) {
        checkPermissions(getOriginalAllow() ^ allowBits);
        this.allow = allowBits;
        this.deny = getCurrentDeny() & ~allowBits;
        allowSet = denySet = true;
        return this;
    }

    @NotNull
    @Override
    public PermissionOverrideAction grant(long allowBits) {
        return setAllowed(getCurrentAllow() | allowBits);
    }

    @NotNull
    @Override
    @CheckReturnValue
    public PermissionOverrideActionImpl setDenied(long denyBits) {
        checkPermissions(getOriginalDeny() ^ denyBits);
        this.deny = denyBits;
        this.allow = getCurrentAllow() & ~denyBits;
        allowSet = denySet = true;
        return this;
    }

    @NotNull
    @Override
    public PermissionOverrideAction deny(long denyBits) {
        return setDenied(getCurrentDeny() | denyBits);
    }

    @NotNull
    @Override
    public PermissionOverrideAction clear(long inheritedBits) {
        return setAllowed(getCurrentAllow() & ~inheritedBits).setDenied(getCurrentDeny() & ~inheritedBits);
    }

    protected void checkPermissions(long changed) {
        Member selfMember = getGuild().getSelfMember();
        if (changed != 0 && !selfMember.hasPermission(Permission.ADMINISTRATOR)) {
            long channelPermissions = PermissionUtil.getExplicitPermission(channel, selfMember, false);
            if ((channelPermissions & Permission.MANAGE_PERMISSIONS.getRawValue()) == 0) {
                // This implies we can only set permissions the bot also has in the channel
                long botPerms = PermissionUtil.getEffectivePermission(channel, selfMember);
                EnumSet<Permission> missing = Permission.getPermissions(changed & ~botPerms);
                if (!missing.isEmpty()) {
                    throw new InsufficientPermissionException(
                            channel,
                            Permission.MANAGE_PERMISSIONS,
                            "You must have Permission.MANAGE_PERMISSIONS on the channel explicitly in order to set permissions you don't already have!");
                }
            }
        }
    }

    @NotNull
    @Override
    @CheckReturnValue
    public PermissionOverrideActionImpl setPermissions(long allowBits, long denyBits) {
        return setAllowed(allowBits).setDenied(denyBits);
    }

    private long getCurrentAllow() {
        if (allowSet) {
            return allow;
        }
        return isOverride ? 0 : getOriginalAllow();
    }

    private long getCurrentDeny() {
        if (denySet) {
            return deny;
        }
        return isOverride ? 0 : getOriginalDeny();
    }

    private long getOriginalDeny() {
        PermissionOverride override = channel.getPermissionOverrideMap().get(id);
        return override == null ? 0 : override.getDeniedRaw();
    }

    private long getOriginalAllow() {
        PermissionOverride override = channel.getPermissionOverrideMap().get(id);
        return override == null ? 0 : override.getAllowedRaw();
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject object = DataObject.empty();
        object.put("type", isRole() ? 0 : 1);
        object.put("allow", getCurrentAllow());
        object.put("deny", getCurrentDeny());
        reset();
        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<PermissionOverride> request) {
        DataObject object = (DataObject) request.getRawBody();
        PermissionOverrideImpl override = new PermissionOverrideImpl(channel, id, isRole());
        override.setAllow(object.getLong("allow"));
        override.setDeny(object.getLong("deny"));
        // This is added by the event later
        // ((AbstractChannelImpl<?,?>) channel).getOverrideMap().put(id, override);
        request.onSuccess(override);
    }
}
