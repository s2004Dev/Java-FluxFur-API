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

package lonter.jfa.internal.handle;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.concrete.VoiceChannel;
import lonter.jfa.api.entities.guild.SecurityIncidentActions;
import lonter.jfa.api.entities.guild.SecurityIncidentDetections;
import lonter.jfa.api.entities.guild.SystemChannelFlag;
import lonter.jfa.api.events.guild.update.*;
import lonter.jfa.api.interactions.FluxerLocale;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.requests.WebSocketClient;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class GuildUpdateHandler extends SocketHandler {

    public GuildUpdateHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long id = content.getLong("id");
        if (getJFA().getGuildSetupController().isLocked(id)) {
            return id;
        }

        GuildImpl guild = (GuildImpl) getJFA().getGuildById(id);
        if (guild == null) {
            EventCache.LOG.debug("Caching GUILD_UPDATE for guild with id: {}", id);
            getJFA().getEventCache().cache(EventCache.Type.GUILD, id, responseNumber, allContent, this::handle);
            return null;
        }

        // When member limits aren't initialized we don't fire an update event for them
        int maxMembers = content.getInt("max_members", 0);
        int maxPresences = content.getInt("max_presences", 5000);
        if (guild.getMaxMembers() == 0) {
            // Initialize member limits to avoid unwanted update events
            guild.setMaxPresences(maxPresences);
            guild.setMaxMembers(maxMembers);
        }

        long ownerId = content.getLong("owner_id");
        int boostCount = content.getInt("premium_subscription_count", 0);
        int boostTier = content.getInt("premium_tier", 0);
        String description = content.getString("description", null);
        String vanityCode = content.getString("vanity_url_code", null);
        String bannerId = content.getString("banner", null);
        String name = content.getString("name");
        String iconId = content.getString("icon", null);
        String splashId = content.getString("splash", null);
        SecurityIncidentActions securityIncidentActions = content.optObject("incidents_data")
                .map(api.getEntityBuilder()::createSecurityIncidentsActions)
                .orElse(SecurityIncidentActions.disabled());
        SecurityIncidentDetections securityIncidentDetections = content.optObject("incidents_data")
                .map(api.getEntityBuilder()::createSecurityIncidentsDetections)
                .orElse(SecurityIncidentDetections.EMPTY);
        Guild.VerificationLevel verificationLevel =
                Guild.VerificationLevel.fromKey(content.getInt("verification_level"));
        Guild.NotificationLevel notificationLevel =
                Guild.NotificationLevel.fromKey(content.getInt("default_message_notifications"));
        Guild.MFALevel mfaLevel = Guild.MFALevel.fromKey(content.getInt("mfa_level"));
        Guild.NSFWLevel nsfwLevel = Guild.NSFWLevel.fromKey(content.getInt("nsfw_level", -1));
        Guild.ExplicitContentLevel explicitContentLevel =
                Guild.ExplicitContentLevel.fromKey(content.getInt("explicit_content_filter"));
        Guild.Timeout afkTimeout = Guild.Timeout.fromKey(content.getInt("afk_timeout"));
        FluxerLocale locale = FluxerLocale.from(content.getString("preferred_locale", "en-US"));
        VoiceChannel afkChannel = content.isNull("afk_channel_id")
                ? null
                : guild.getChannelById(VoiceChannel.class, content.getLong("afk_channel_id"));
        TextChannel systemChannel = content.isNull("system_channel_id")
                ? null
                : guild.getChannelById(TextChannel.class, content.getLong("system_channel_id"));
        TextChannel rulesChannel = content.isNull("rules_channel_id")
                ? null
                : guild.getChannelById(TextChannel.class, content.getLong("rules_channel_id"));
        TextChannel communityUpdatesChannel = content.isNull("public_updates_channel_id")
                ? null
                : guild.getChannelById(TextChannel.class, content.getLong("public_updates_channel_id"));
        TextChannel safetyAlertsChannel = content.isNull("safety_alerts_channel_id")
                ? null
                : guild.getChannelById(TextChannel.class, content.getLong("safety_alerts_channel_id"));
        Set<String> features;

        int systemChannelFlagBitmask = content.getInt("system_channel_flags", 0);

        if (!content.isNull("features")) {
            DataArray featureArr = content.getArray("features");
            features =
                    featureArr.stream(DataArray::getString).map(String::intern).collect(Collectors.toSet());
        } else {
            features = Collections.emptySet();
        }

        if (ownerId != guild.getOwnerIdLong()) {
            long oldOwnerId = guild.getOwnerIdLong();
            Member oldOwner = guild.getOwner();
            Member newOwner = guild.getMembersView().get(ownerId);
            if (newOwner == null) {
                WebSocketClient.LOG.debug(
                        "Received {} with owner not in cache. UserId: {} GuildId: {}",
                        allContent.get("t"),
                        ownerId,
                        id);
            }
            guild.setOwner(newOwner);
            guild.setOwnerId(ownerId);
            getJFA().handleEvent(
                            new GuildUpdateOwnerEvent(getJFA(), responseNumber, guild, oldOwner, oldOwnerId, ownerId));
        }
        if (systemChannelFlagBitmask != guild.getSystemChannelFlagsRaw()) {
            Set<SystemChannelFlag> oldSystemChannelFlags = guild.getSystemChannelFlags();
            Set<SystemChannelFlag> systemChannelFlags =
                    Collections.unmodifiableSet(SystemChannelFlag.getFlags(systemChannelFlagBitmask));
            guild.setSystemChannelFlags(systemChannelFlagBitmask);
            getJFA().handleEvent(new GuildUpdateSystemChannelFlagsEvent(
                    getJFA(), responseNumber, guild, oldSystemChannelFlags, systemChannelFlags));
        }
        if (!Objects.equals(description, guild.getDescription())) {
            String oldDescription = guild.getDescription();
            guild.setDescription(description);
            getJFA().handleEvent(new GuildUpdateDescriptionEvent(getJFA(), responseNumber, guild, oldDescription));
        }
        if (!Objects.equals(bannerId, guild.getBannerId())) {
            String oldBanner = guild.getBannerId();
            guild.setBannerId(bannerId);
            getJFA().handleEvent(new GuildUpdateBannerEvent(getJFA(), responseNumber, guild, oldBanner));
        }
        if (!Objects.equals(vanityCode, guild.getVanityCode())) {
            String oldCode = guild.getVanityCode();
            guild.setVanityCode(vanityCode);
            getJFA().handleEvent(new GuildUpdateVanityCodeEvent(getJFA(), responseNumber, guild, oldCode));
        }
        if (maxMembers != guild.getMaxMembers()) {
            int oldMax = guild.getMaxMembers();
            guild.setMaxMembers(maxMembers);
            getJFA().handleEvent(new GuildUpdateMaxMembersEvent(getJFA(), responseNumber, guild, oldMax));
        }
        if (maxPresences != guild.getMaxPresences()) {
            int oldMax = guild.getMaxPresences();
            guild.setMaxPresences(maxPresences);
            getJFA().handleEvent(new GuildUpdateMaxPresencesEvent(getJFA(), responseNumber, guild, oldMax));
        }
        if (boostCount != guild.getBoostCount()) {
            int oldCount = guild.getBoostCount();
            guild.setBoostCount(boostCount);
            getJFA().handleEvent(new GuildUpdateBoostCountEvent(getJFA(), responseNumber, guild, oldCount));
        }
        if (Guild.BoostTier.fromKey(boostTier) != guild.getBoostTier()) {
            Guild.BoostTier oldTier = guild.getBoostTier();
            guild.setBoostTier(boostTier);
            getJFA().handleEvent(new GuildUpdateBoostTierEvent(getJFA(), responseNumber, guild, oldTier));
        }
        if (!Objects.equals(name, guild.getName())) {
            String oldName = guild.getName();
            guild.setName(name);
            getJFA().handleEvent(new GuildUpdateNameEvent(getJFA(), responseNumber, guild, oldName));
        }
        if (!Objects.equals(iconId, guild.getIconId())) {
            String oldIconId = guild.getIconId();
            guild.setIconId(iconId);
            getJFA().handleEvent(new GuildUpdateIconEvent(getJFA(), responseNumber, guild, oldIconId));
        }
        if (!features.equals(guild.getFeatures())) {
            Set<String> oldFeatures = guild.getFeatures();
            guild.setFeatures(features);
            getJFA().handleEvent(new GuildUpdateFeaturesEvent(getJFA(), responseNumber, guild, oldFeatures));
        }
        if (!Objects.equals(splashId, guild.getSplashId())) {
            String oldSplashId = guild.getSplashId();
            guild.setSplashId(splashId);
            getJFA().handleEvent(new GuildUpdateSplashEvent(getJFA(), responseNumber, guild, oldSplashId));
        }
        if (!Objects.equals(verificationLevel, guild.getVerificationLevel())) {
            Guild.VerificationLevel oldVerificationLevel = guild.getVerificationLevel();
            guild.setVerificationLevel(verificationLevel);
            getJFA().handleEvent(new GuildUpdateVerificationLevelEvent(
                    getJFA(), responseNumber, guild, oldVerificationLevel));
        }
        if (!Objects.equals(notificationLevel, guild.getDefaultNotificationLevel())) {
            Guild.NotificationLevel oldNotificationLevel = guild.getDefaultNotificationLevel();
            guild.setDefaultNotificationLevel(notificationLevel);
            getJFA().handleEvent(new GuildUpdateNotificationLevelEvent(
                    getJFA(), responseNumber, guild, oldNotificationLevel));
        }
        if (!Objects.equals(mfaLevel, guild.getRequiredMFALevel())) {
            Guild.MFALevel oldMfaLevel = guild.getRequiredMFALevel();
            guild.setRequiredMFALevel(mfaLevel);
            getJFA().handleEvent(new GuildUpdateMFALevelEvent(getJFA(), responseNumber, guild, oldMfaLevel));
        }
        if (!Objects.equals(explicitContentLevel, guild.getExplicitContentLevel())) {
            Guild.ExplicitContentLevel oldExplicitContentLevel = guild.getExplicitContentLevel();
            guild.setExplicitContentLevel(explicitContentLevel);
            getJFA().handleEvent(new GuildUpdateExplicitContentLevelEvent(
                    getJFA(), responseNumber, guild, oldExplicitContentLevel));
        }
        if (!Objects.equals(afkTimeout, guild.getAfkTimeout())) {
            Guild.Timeout oldAfkTimeout = guild.getAfkTimeout();
            guild.setAfkTimeout(afkTimeout);
            getJFA().handleEvent(new GuildUpdateAfkTimeoutEvent(getJFA(), responseNumber, guild, oldAfkTimeout));
        }
        if (!Objects.equals(locale, guild.getLocale())) {
            FluxerLocale oldLocale = guild.getLocale();
            guild.setLocale(locale);
            getJFA().handleEvent(new GuildUpdateLocaleEvent(getJFA(), responseNumber, guild, oldLocale));
        }
        if (!Objects.equals(afkChannel, guild.getAfkChannel())) {
            VoiceChannel oldAfkChannel = guild.getAfkChannel();
            guild.setAfkChannel(afkChannel);
            getJFA().handleEvent(new GuildUpdateAfkChannelEvent(getJFA(), responseNumber, guild, oldAfkChannel));
        }
        if (!Objects.equals(systemChannel, guild.getSystemChannel())) {
            TextChannel oldSystemChannel = guild.getSystemChannel();
            guild.setSystemChannel(systemChannel);
            getJFA().handleEvent(new GuildUpdateSystemChannelEvent(getJFA(), responseNumber, guild, oldSystemChannel));
        }
        if (!Objects.equals(rulesChannel, guild.getRulesChannel())) {
            TextChannel oldRulesChannel = guild.getRulesChannel();
            guild.setRulesChannel(rulesChannel);
            getJFA().handleEvent(new GuildUpdateRulesChannelEvent(getJFA(), responseNumber, guild, oldRulesChannel));
        }
        if (!Objects.equals(communityUpdatesChannel, guild.getCommunityUpdatesChannel())) {
            TextChannel oldCommunityUpdatesChannel = guild.getCommunityUpdatesChannel();
            guild.setCommunityUpdatesChannel(communityUpdatesChannel);
            getJFA().handleEvent(new GuildUpdateCommunityUpdatesChannelEvent(
                    getJFA(), responseNumber, guild, oldCommunityUpdatesChannel));
        }
        if (!Objects.equals(safetyAlertsChannel, guild.getSafetyAlertsChannel())) {
            TextChannel oldSafetyAlertsChannel = guild.getSafetyAlertsChannel();
            guild.setSafetyAlertsChannel(safetyAlertsChannel);
            getJFA().handleEvent(new GuildUpdateSafetyAlertsChannelEvent(
                    getJFA(), responseNumber, guild, oldSafetyAlertsChannel));
        }
        if (!Objects.equals(securityIncidentActions, guild.getSecurityIncidentActions())) {
            SecurityIncidentActions oldIncidentActions = guild.getSecurityIncidentActions();
            guild.setSecurityIncidentActions(securityIncidentActions);
            api.handleEvent(
                    new GuildUpdateSecurityIncidentActionsEvent(getJFA(), responseNumber, guild, oldIncidentActions));
        }
        if (!Objects.equals(securityIncidentDetections, guild.getSecurityIncidentDetections())) {
            SecurityIncidentDetections oldIncidentDetections = guild.getSecurityIncidentDetections();
            guild.setSecurityIncidentDetections(securityIncidentDetections);
            api.handleEvent(new GuildUpdateSecurityIncidentDetectionsEvent(
                    getJFA(), responseNumber, guild, oldIncidentDetections));
        }
        if (content.hasKey("nsfw_level") && nsfwLevel != guild.getNSFWLevel()) {
            Guild.NSFWLevel oldNSFWLevel = guild.getNSFWLevel();
            guild.setNSFWLevel(nsfwLevel);
            getJFA().handleEvent(new GuildUpdateNSFWLevelEvent(getJFA(), responseNumber, guild, oldNSFWLevel));
        }
        return null;
    }
}
