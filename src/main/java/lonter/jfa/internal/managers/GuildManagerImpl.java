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
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Icon;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.concrete.VoiceChannel;
import lonter.jfa.api.entities.guild.SystemChannelFlag;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.GuildManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;
import okhttp3.RequestBody;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildManagerImpl extends ManagerBase<GuildManager> implements GuildManager {
    protected Guild guild;

    protected String name;
    protected Icon icon, splash, banner;
    protected String afkChannel, systemChannel, rulesChannel, communityUpdatesChannel, safetyAlertsChannel;
    protected String description;
    protected int afkTimeout;
    protected int notificationLevel;
    protected int explicitContentLevel;
    protected int verificationLevel;
    protected boolean boostProgressBarEnabled;
    protected Set<String> features;
    protected Set<SystemChannelFlag> systemChannelFlags;

    public GuildManagerImpl(Guild guild) {
        super(guild.getJFA(), Route.Guilds.MODIFY_GUILD.compile(guild.getId()));
        this.guild = guild;
        if (isPermissionChecksEnabled()) {
            checkPermissions();
        }
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
    @CheckReturnValue
    public GuildManagerImpl reset(long fields) {
        super.reset(fields);
        if ((fields & NAME) == NAME) {
            this.name = null;
        }
        if ((fields & ICON) == ICON) {
            this.icon = null;
        }
        if ((fields & SPLASH) == SPLASH) {
            this.splash = null;
        }
        if ((fields & AFK_CHANNEL) == AFK_CHANNEL) {
            this.afkChannel = null;
        }
        if ((fields & SYSTEM_CHANNEL) == SYSTEM_CHANNEL) {
            this.systemChannel = null;
        }
        if ((fields & RULES_CHANNEL) == RULES_CHANNEL) {
            this.rulesChannel = null;
        }
        if ((fields & COMMUNITY_UPDATES_CHANNEL) == COMMUNITY_UPDATES_CHANNEL) {
            this.communityUpdatesChannel = null;
        }
        if ((fields & SAFETY_ALERTS_CHANNEL) == SAFETY_ALERTS_CHANNEL) {
            this.safetyAlertsChannel = null;
        }
        if ((fields & DESCRIPTION) == DESCRIPTION) {
            this.description = null;
        }
        if ((fields & BANNER) == BANNER) {
            this.banner = null;
        }
        if ((fields & FEATURES) == FEATURES) {
            this.features = null;
        }
        if ((fields & SYSTEM_CHANNEL_FLAGS) == SYSTEM_CHANNEL_FLAGS) {
            this.systemChannelFlags = null;
        }
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl reset(@NotNull long... fields) {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl reset() {
        super.reset();
        this.name = null;
        this.icon = null;
        this.splash = null;
        this.description = null;
        this.banner = null;
        this.afkChannel = null;
        this.systemChannel = null;
        this.features = null;
        this.systemChannelFlags = null;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setName(@NotNull String name) {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setIcon(Icon icon) {
        this.icon = icon;
        set |= ICON;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setSplash(Icon splash) {
        checkFeature("INVITE_SPLASH");
        this.splash = splash;
        set |= SPLASH;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setAfkChannel(VoiceChannel afkChannel) {
        Checks.check(
                afkChannel == null || afkChannel.getGuild().equals(getGuild()), "Channel must be from the same guild");
        this.afkChannel = afkChannel == null ? null : afkChannel.getId();
        set |= AFK_CHANNEL;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setSystemChannel(TextChannel systemChannel) {
        Checks.check(
                systemChannel == null || systemChannel.getGuild().equals(getGuild()),
                "Channel must be from the same guild");
        this.systemChannel = systemChannel == null ? null : systemChannel.getId();
        set |= SYSTEM_CHANNEL;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setRulesChannel(TextChannel rulesChannel) {
        Checks.check(
                rulesChannel == null || rulesChannel.getGuild().equals(getGuild()),
                "Channel must be from the same guild");
        this.rulesChannel = rulesChannel == null ? null : rulesChannel.getId();
        set |= RULES_CHANNEL;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setCommunityUpdatesChannel(TextChannel communityUpdatesChannel) {
        Checks.check(
                communityUpdatesChannel == null
                        || communityUpdatesChannel.getGuild().equals(getGuild()),
                "Channel must be from the same guild");
        this.communityUpdatesChannel = communityUpdatesChannel == null ? null : communityUpdatesChannel.getId();
        set |= COMMUNITY_UPDATES_CHANNEL;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setSafetyAlertsChannel(TextChannel safetyAlertsChannel) {
        Checks.check(
                safetyAlertsChannel == null || safetyAlertsChannel.getGuild().equals(getGuild()),
                "Channel must be from the same guild");
        this.safetyAlertsChannel = safetyAlertsChannel == null ? null : safetyAlertsChannel.getId();
        set |= SAFETY_ALERTS_CHANNEL;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setAfkTimeout(@NotNull Guild.Timeout timeout) {
        Checks.notNull(timeout, "Timeout");
        this.afkTimeout = timeout.getSeconds();
        set |= AFK_TIMEOUT;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setVerificationLevel(@NotNull Guild.VerificationLevel level) {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.VerificationLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.verificationLevel = level.getKey();
        set |= VERIFICATION_LEVEL;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setDefaultNotificationLevel(@NotNull Guild.NotificationLevel level) {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.NotificationLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.notificationLevel = level.getKey();
        set |= NOTIFICATION_LEVEL;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setExplicitContentLevel(@NotNull Guild.ExplicitContentLevel level) {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.ExplicitContentLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.explicitContentLevel = level.getKey();
        set |= EXPLICIT_CONTENT_LEVEL;
        return this;
    }

    @NotNull
    @Override
    public GuildManager setBanner(@Nullable Icon banner) {
        checkFeature("BANNER");
        this.banner = banner;
        set |= BANNER;
        return this;
    }

    @NotNull
    @Override
    public GuildManager setDescription(@Nullable String description) {
        checkFeature("VERIFIED");
        this.description = description;
        set |= DESCRIPTION;
        return this;
    }

    @NotNull
    @Override
    public GuildManager setBoostProgressBarEnabled(boolean enabled) {
        this.boostProgressBarEnabled = enabled;
        set |= BOOST_PROGRESS_BAR_ENABLED;
        return this;
    }

    @NotNull
    @Override
    public GuildManager setFeatures(@NotNull Collection<String> features) {
        Checks.noneNull(features, "Features");
        this.features = features.stream().map(String::toUpperCase).collect(Collectors.toSet());
        set |= FEATURES;
        return this;
    }

    @NotNull
    @Override
    public GuildManager addFeatures(@NotNull Collection<String> features) {
        return updateFeatures(features, feature -> this.features.add(feature));
    }

    @NotNull
    @Override
    public GuildManager removeFeatures(@NotNull Collection<String> features) {
        return updateFeatures(features, feature -> this.features.remove(feature));
    }

    private GuildManager updateFeatures(Collection<String> changed, Consumer<String> op) {
        Checks.noneNull(changed, "Features");
        if (this.features == null) {
            this.features = new HashSet<>(getGuild().getFeatures());
        }
        changed.stream().map(String::toUpperCase).forEach(op);
        set |= FEATURES;
        return this;
    }

    @NotNull
    @Override
    public GuildManager setSystemChannelFlags(@NotNull Collection<SystemChannelFlag> flags) {
        Checks.noneNull(flags, "System channel flag");
        this.systemChannelFlags = Helpers.copyEnumSet(SystemChannelFlag.class, flags);
        set |= SYSTEM_CHANNEL_FLAGS;
        return this;
    }

    @NotNull
    @Override
    public GuildManager enableSystemChannelFlags(@NotNull Collection<SystemChannelFlag> flags) {
        return updateSystemChannelFlags(flags, Set::addAll);
    }

    @NotNull
    @Override
    public GuildManager disableSystemChannelFlags(@NotNull Collection<SystemChannelFlag> flags) {
        return updateSystemChannelFlags(flags, Set::removeAll);
    }

    private GuildManager updateSystemChannelFlags(
            Collection<SystemChannelFlag> flags,
            BiConsumer<Set<SystemChannelFlag>, Collection<SystemChannelFlag>> bulkUpdateOp) {
        Checks.noneNull(flags, "System channel flag");
        if (this.systemChannelFlags == null) {
            this.systemChannelFlags =
                    Helpers.copyEnumSet(SystemChannelFlag.class, getGuild().getSystemChannelFlags());
        }
        bulkUpdateOp.accept(this.systemChannelFlags, flags);
        set |= SYSTEM_CHANNEL_FLAGS;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject body = DataObject.empty().put("name", getGuild().getName());
        if (shouldUpdate(NAME)) {
            body.put("name", name);
        }
        if (shouldUpdate(AFK_TIMEOUT)) {
            body.put("afk_timeout", afkTimeout);
        }
        if (shouldUpdate(ICON)) {
            body.put("icon", icon == null ? null : icon.getEncoding());
        }
        if (shouldUpdate(SPLASH)) {
            body.put("splash", splash == null ? null : splash.getEncoding());
        }
        if (shouldUpdate(AFK_CHANNEL)) {
            body.put("afk_channel_id", afkChannel);
        }
        if (shouldUpdate(SYSTEM_CHANNEL)) {
            body.put("system_channel_id", systemChannel);
        }
        if (shouldUpdate(RULES_CHANNEL)) {
            body.put("rules_channel_id", rulesChannel);
        }
        if (shouldUpdate(COMMUNITY_UPDATES_CHANNEL)) {
            body.put("public_updates_channel_id", communityUpdatesChannel);
        }
        if (shouldUpdate(SAFETY_ALERTS_CHANNEL)) {
            body.put("safety_alerts_channel_id", safetyAlertsChannel);
        }
        if (shouldUpdate(VERIFICATION_LEVEL)) {
            body.put("verification_level", verificationLevel);
        }
        if (shouldUpdate(NOTIFICATION_LEVEL)) {
            body.put("default_message_notifications", notificationLevel);
        }
        if (shouldUpdate(EXPLICIT_CONTENT_LEVEL)) {
            body.put("explicit_content_filter", explicitContentLevel);
        }
        if (shouldUpdate(BANNER)) {
            body.put("banner", banner == null ? null : banner.getEncoding());
        }
        if (shouldUpdate(DESCRIPTION)) {
            body.put("description", description);
        }
        if (shouldUpdate(BOOST_PROGRESS_BAR_ENABLED)) {
            body.put("premium_progress_bar_enabled", boostProgressBarEnabled);
        }
        if (shouldUpdate(FEATURES)) {
            body.put("features", features);
        }
        if (shouldUpdate(SYSTEM_CHANNEL_FLAGS)) {
            body.put("system_channel_flags", SystemChannelFlag.getRaw(systemChannelFlags));
        }

        reset();
        return getRequestBody(body);
    }

    @Override
    protected boolean checkPermissions() {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            throw new InsufficientPermissionException(getGuild(), Permission.MANAGE_SERVER);
        }
        return super.checkPermissions();
    }

    private void checkFeature(String feature) {
        if (!getGuild().getFeatures().contains(feature)) {
            throw new IllegalStateException("This guild doesn't have the " + feature + " feature enabled");
        }
    }
}
