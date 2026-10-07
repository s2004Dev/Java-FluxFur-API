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

package lonter.jfa.internal.managers;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.PermOverrideManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public class PermOverrideManagerImpl extends ManagerBase<PermOverrideManager> implements PermOverrideManager {
    protected final boolean role;
    protected PermissionOverride override;

    protected long allowed;
    protected long denied;

    /**
     * Creates a new PermOverrideManager instance
     *
     * @param override
     *        The {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride} to manage
     */
    public PermOverrideManagerImpl(PermissionOverride override) {
        super(
                override.getJFA(),
                Route.Channels.MODIFY_PERM_OVERRIDE.compile(
                        override.getChannel().getId(), override.getId()));
        this.override = override;
        this.role = override.isRoleOverride();
        this.allowed = override.getAllowedRaw();
        this.denied = override.getDeniedRaw();
        if (isPermissionChecksEnabled()) {
            checkPermissions();
        }
    }

    private void setupValues() {
        if (!shouldUpdate(ALLOWED)) {
            this.allowed = getPermissionOverride().getAllowedRaw();
        }
        if (!shouldUpdate(DENIED)) {
            this.denied = getPermissionOverride().getDeniedRaw();
        }
    }

    @NotNull
    @Override
    public PermissionOverride getPermissionOverride() {
        IPermissionContainerMixin<?> channel = (IPermissionContainerMixin<?>) override.getChannel();
        PermissionOverride realOverride = channel.getPermissionOverrideMap().get(override.getIdLong());
        if (realOverride != null) {
            override = realOverride;
        }
        return override;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl reset(long fields) {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl reset(@NotNull long... fields) {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl reset() {
        super.reset();
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl grant(long permissions) {
        if (permissions == 0) {
            return this;
        }
        setupValues();
        this.allowed |= permissions;
        this.denied &= ~permissions;
        this.set |= PERMISSIONS;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl deny(long permissions) {
        if (permissions == 0) {
            return this;
        }
        setupValues();
        this.denied |= permissions;
        this.allowed &= ~permissions;
        this.set |= PERMISSIONS;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public PermOverrideManagerImpl clear(long permissions) {
        setupValues();
        if ((allowed & permissions) != 0) {
            this.allowed &= ~permissions;
            this.set |= ALLOWED;
        }

        if ((denied & permissions) != 0) {
            this.denied &= ~permissions;
            this.set |= DENIED;
        }

        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        String targetId = override.getId();
        // setup missing values here
        setupValues();
        RequestBody data = getRequestBody(DataObject.empty()
                .put("id", targetId)
                .put("type", role ? "role" : "member")
                .put("allow", this.allowed)
                .put("deny", this.denied));
        reset();
        return data;
    }

    @Override
    protected boolean checkPermissions() {
        Member selfMember = getGuild().getSelfMember();
        IPermissionContainer channel = getChannel();
        Checks.checkAccess(selfMember, channel);
        if (!selfMember.hasPermission(channel, Permission.MANAGE_PERMISSIONS)) {
            throw new InsufficientPermissionException(channel, Permission.MANAGE_PERMISSIONS);
        }
        return super.checkPermissions();
    }
}
