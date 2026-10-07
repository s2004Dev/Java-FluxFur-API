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

import gnu.trove.map.TLongObjectMap;
import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.*;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.exceptions.HierarchyException;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.RoleManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.utils.cache.CacheFlag;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import lonter.jfa.internal.entities.mixin.RoleMixin;
import lonter.jfa.internal.managers.RoleManagerImpl;
import lonter.jfa.internal.requests.restaction.AuditableRestActionImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.PermissionUtil;
import lonter.jfa.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import java.util.EnumSet;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoleImpl implements Role, RoleMixin<RoleImpl> {
    private final long id;
    private final JFAImpl api;
    private Guild guild;

    private RoleTagsImpl tags;
    private String name;
    private boolean managed;
    private boolean hoisted;
    private boolean mentionable;
    private long rawPermissions;

    private int primaryColor;
    private int secondaryColor = Role.DEFAULT_COLOR_RAW;
    private int tertiaryColor = Role.DEFAULT_COLOR_RAW;

    private int rawPosition;
    private int frozenPosition = Integer.MIN_VALUE; // this is used exclusively for delete events
    private RoleIcon icon;

    public RoleImpl(long id, Guild guild) {
        this.id = id;
        this.api = (JFAImpl) guild.getJFA();
        this.guild = guild;
        this.tags = api.isCacheFlagSet(CacheFlag.ROLE_TAGS) ? new RoleTagsImpl() : null;
    }

    @Override
    public boolean isDetached() {
        return false;
    }

    @Override
    public int getPosition() {
        if (frozenPosition > Integer.MIN_VALUE) {
            return frozenPosition;
        }
        Guild guild = getGuild();
        if (equals(guild.getPublicRole())) {
            return -1;
        }

        // Subtract 1 to get into 0-index, and 1 to disregard the everyone role.
        int i = guild.getRoles().size() - 2;
        for (Role r : guild.getRoles()) {
            if (equals(r)) {
                return i;
            }
            i--;
        }
        throw new IllegalStateException(
                "Somehow when determining position we never found the role in the Guild's roles? wtf?");
    }

    @Override
    public int getPositionRaw() {
        return rawPosition;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isManaged() {
        return managed;
    }

    @Override
    public boolean isHoisted() {
        return hoisted;
    }

    @Override
    public boolean isMentionable() {
        return mentionable;
    }

    @Override
    public long getPermissionsRaw() {
        return rawPermissions;
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissions() {
        return Permission.getPermissions(rawPermissions);
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissions(@NotNull GuildChannel channel) {
        return Permission.getPermissions(PermissionUtil.getEffectivePermission(channel.getPermissionContainer(), this));
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissionsExplicit() {
        return getPermissions();
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissionsExplicit(@NotNull GuildChannel channel) {
        return Permission.getPermissions(PermissionUtil.getExplicitPermission(channel.getPermissionContainer(), this));
    }

    @NotNull
    @Override
    public RoleColors getColors() {
        return new RoleColors(this.primaryColor, this.secondaryColor, this.tertiaryColor);
    }

    @Override
    public boolean isPublicRole() {
        return this.equals(this.getGuild().getPublicRole());
    }

    @Override
    public boolean hasPermission(@NotNull Permission... permissions) {
        long effectivePerms = rawPermissions | getGuild().getPublicRole().getPermissionsRaw();
        for (Permission perm : permissions) {
            long rawValue = perm.getRawValue();
            if ((effectivePerms & rawValue) != rawValue) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull GuildChannel channel, @NotNull Permission... permissions) {
        long effectivePerms = PermissionUtil.getEffectivePermission(channel.getPermissionContainer(), this);
        for (Permission perm : permissions) {
            long rawValue = perm.getRawValue();
            if ((effectivePerms & rawValue) != rawValue) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canSync(@NotNull IPermissionContainer targetChannel, @NotNull IPermissionContainer syncSource) {
        Checks.notNull(targetChannel, "Channel");
        Checks.notNull(syncSource, "Channel");
        Checks.check(targetChannel.getGuild().equals(getGuild()), "Channels must be from the same guild!");
        Checks.check(syncSource.getGuild().equals(getGuild()), "Channels must be from the same guild!");
        long rolePerms = PermissionUtil.getEffectivePermission(targetChannel, this);
        if ((rolePerms & Permission.MANAGE_PERMISSIONS.getRawValue()) == 0) {
            return false; // Role can't manage permissions at all!
        }
        long channelPermissions = PermissionUtil.getExplicitPermission(targetChannel, this, false);
        // If the role has ADMINISTRATOR or MANAGE_PERMISSIONS
        // then it can also set any other permission on the channel
        boolean hasLocalAdmin = ((rolePerms & Permission.ADMINISTRATOR.getRawValue())
                        | (channelPermissions & Permission.MANAGE_PERMISSIONS.getRawValue()))
                != 0;
        if (hasLocalAdmin) {
            return true;
        }

        TLongObjectMap<PermissionOverride> existingOverrides =
                ((IPermissionContainerMixin<?>) targetChannel).getPermissionOverrideMap();
        for (PermissionOverride override : syncSource.getPermissionOverrides()) {
            PermissionOverride existing = existingOverrides.get(override.getIdLong());
            long allow = override.getAllowedRaw();
            long deny = override.getDeniedRaw();
            if (existing != null) {
                allow ^= existing.getAllowedRaw();
                deny ^= existing.getDeniedRaw();
            }
            // If any permissions changed that the role doesn't have in the channel,
            // the role can't sync it :(
            if (((allow | deny) & ~rolePerms) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canSync(@NotNull IPermissionContainer channel) {
        Checks.notNull(channel, "Channel");
        Checks.check(channel.getGuild().equals(getGuild()), "Channels must be from the same guild!");
        long rolePerms = PermissionUtil.getEffectivePermission(channel, this);
        if ((rolePerms & Permission.MANAGE_PERMISSIONS.getRawValue()) == 0) {
            return false; // Role can't manage permissions at all!
        }
        long channelPermissions = PermissionUtil.getExplicitPermission(channel, this, false);
        // If the role has ADMINISTRATOR or MANAGE_PERMISSIONS
        // then it can also set any other permission on the channel
        return ((rolePerms & Permission.ADMINISTRATOR.getRawValue())
                        | (channelPermissions & Permission.MANAGE_PERMISSIONS.getRawValue()))
                != 0;
    }

    @Override
    public boolean canInteract(@NotNull Role role) {
        return PermissionUtil.canInteract(this, role);
    }

    @NotNull
    @Override
    public Guild getGuild() {
        Guild realGuild = api.getGuildById(guild.getIdLong());
        if (realGuild != null) {
            guild = realGuild;
        }
        return guild;
    }

    @NotNull
    @Override
    public RoleManager getManager() {
        return new RoleManagerImpl(this);
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> delete() {
        Guild guild = getGuild();
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            throw new InsufficientPermissionException(guild, Permission.MANAGE_ROLES);
        }
        if (!PermissionUtil.canInteract(guild.getSelfMember(), this)) {
            throw new HierarchyException("Can't delete role >= highest self-role");
        }
        if (managed) {
            throw new UnsupportedOperationException("Cannot delete a Role that is managed. ");
        }

        Route.CompiledRoute route = Route.Roles.DELETE_ROLE.compile(guild.getId(), getId());
        return new AuditableRestActionImpl<>(getJFA(), route);
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @NotNull
    @Override
    public RoleTags getTags() {
        return tags == null ? RoleTagsImpl.EMPTY : tags;
    }

    @Nullable
    @Override
    public RoleIcon getIcon() {
        return icon;
    }

    @NotNull
    @Override
    public String getAsMention() {
        return isPublicRole() ? "@everyone" : "<@&" + getId() + '>';
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RoleImpl)) {
            return false;
        }
        RoleImpl oRole = (RoleImpl) o;
        return this.getIdLong() == oRole.getIdLong();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return new EntityString(this).setName(getName()).toString();
    }

    // -- Setters --

    @Override
    public RoleImpl setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public RoleImpl setPrimaryColor(int color) {
        this.primaryColor = color;
        return this;
    }

    @Override
    public RoleImpl setSecondaryColor(int color) {
        this.secondaryColor = color;
        return this;
    }

    @Override
    public RoleImpl setTertiaryColor(int color) {
        this.tertiaryColor = color;
        return this;
    }

    @Override
    public RoleImpl setManaged(boolean managed) {
        this.managed = managed;
        return this;
    }

    @Override
    public RoleImpl setHoisted(boolean hoisted) {
        this.hoisted = hoisted;
        return this;
    }

    @Override
    public RoleImpl setMentionable(boolean mentionable) {
        this.mentionable = mentionable;
        return this;
    }

    @Override
    public RoleImpl setRawPermissions(long rawPermissions) {
        this.rawPermissions = rawPermissions;
        return this;
    }

    @Override
    public RoleImpl setRawPosition(int rawPosition) {
        SortedSnowflakeCacheViewImpl<Role> roleCache =
                (SortedSnowflakeCacheViewImpl<Role>) getGuild().getRoleCache();
        roleCache.clearCachedLists();
        this.rawPosition = rawPosition;
        return this;
    }

    @Override
    public RoleImpl setTags(DataObject tags) {
        if (this.tags == null) {
            return this;
        }
        this.tags = new RoleTagsImpl(tags);
        return this;
    }

    @Override
    public RoleImpl setIcon(RoleIcon icon) {
        this.icon = icon;
        return this;
    }

    public void freezePosition() {
        this.frozenPosition = getPosition();
    }

    public static class RoleTagsImpl implements RoleTags {
        public static final RoleTags EMPTY = new RoleTagsImpl();
        private final long botId;
        private final long integrationId;
        private final long subscriptionListingId;
        private final boolean premiumSubscriber;
        private final boolean availableForPurchase;
        private final boolean isGuildConnections;

        public RoleTagsImpl() {
            this.botId = 0L;
            this.integrationId = 0L;
            this.subscriptionListingId = 0L;
            this.premiumSubscriber = false;
            this.availableForPurchase = false;
            this.isGuildConnections = false;
        }

        public RoleTagsImpl(DataObject tags) {
            this.botId = tags.getUnsignedLong("bot_id", 0L);
            this.integrationId = tags.getUnsignedLong("integration_id", 0L);
            this.subscriptionListingId = tags.getUnsignedLong("subscription_listing_id", 0L);
            this.premiumSubscriber = tags.hasKey("premium_subscriber");
            this.availableForPurchase = tags.hasKey("available_for_purchase");
            this.isGuildConnections = tags.hasKey("guild_connections");
        }

        @Override
        public boolean isBot() {
            return botId != 0;
        }

        @Override
        public long getBotIdLong() {
            return botId;
        }

        @Override
        public boolean isBoost() {
            return premiumSubscriber;
        }

        @Override
        public boolean isIntegration() {
            return integrationId != 0;
        }

        @Override
        public long getIntegrationIdLong() {
            return integrationId;
        }

        @Override
        public long getSubscriptionIdLong() {
            return subscriptionListingId;
        }

        @Override
        public boolean isAvailableForPurchase() {
            return availableForPurchase;
        }

        @Override
        public boolean isLinkedRole() {
            return isGuildConnections;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    botId,
                    integrationId,
                    premiumSubscriber,
                    availableForPurchase,
                    subscriptionListingId,
                    isGuildConnections);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof RoleTagsImpl)) {
                return false;
            }
            RoleTagsImpl other = (RoleTagsImpl) obj;
            return botId == other.botId
                    && integrationId == other.integrationId
                    && premiumSubscriber == other.premiumSubscriber
                    && availableForPurchase == other.availableForPurchase
                    && subscriptionListingId == other.subscriptionListingId
                    && isGuildConnections == other.isGuildConnections;
        }

        @Override
        public String toString() {
            return new EntityString(this)
                    .addMetadata("bot", getBotId())
                    .addMetadata("integration", getIntegrationId())
                    .addMetadata("subscriptionListing", getSubscriptionId())
                    .addMetadata("isBoost", isBoost())
                    .addMetadata("isAvailableForPurchase", isAvailableForPurchase())
                    .addMetadata("isGuildConnections", isLinkedRole())
                    .toString();
        }
    }
}
